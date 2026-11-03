package com.androidkit.app.data.agent

import ai.koog.agents.chatMemory.feature.ChatMemory
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistryBuilder
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.MessagePart
import ai.koog.prompt.params.LLMParams
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Koog 引擎：app 的 agent 运行时。agent loop（模型 → 工具 → 模型…）交给 Koog 跑，
 * 工具和技能还是 kit 自己那套。
 *
 * @param apiKey Anthropic API key；上架分发的 app 别内嵌真 key，
 *   把 baseUrl 指向自己的转发代理（代理只转发 /v1/messages，key 留在代理上）
 */
class KoogAgentRuntime(
    apiKey: String,
    baseUrl: String = "https://api.anthropic.com",
) : AgentRuntime {

    // Koog 自带模型用官方版本串；skill 里写了新模型 id 时自己补一条（见 resolveModel）
    private val modelVersions = AnthropicClientSettings().modelVersionsMap.toMutableMap()

    private val executor: PromptExecutor = MultiLLMPromptExecutor(
        mapOf(
            LLMProvider.Anthropic to AnthropicLLMClient(
                apiKey = apiKey,
                settings = AnthropicClientSettings(modelVersionsMap = modelVersions, baseUrl = baseUrl),
                httpClientFactory = KtorKoogHttpClient.Factory(),
            )
        )
    )

    override fun session(skill: SkillDefinition, approver: ToolApprover): AgentSession =
        KoogAgentSession(skill, executor, resolveModel(skill.model, modelVersions), approver)
}

/**
 * 一个 Koog 会话：agent 和 session 各建一次，多轮 send 复用同一份对话历史
 * （历史由 Koog 的 ChatMemory feature 管，按 session 的 runId 存；我们不往 prompt 里塞）。
 */
internal class KoogAgentSession(
    private val skill: SkillDefinition,
    executor: PromptExecutor,
    model: LLModel,
    approver: ToolApprover,
) : AgentSession {

    init {
        // 闸门注入工具：危险工具执行前先过 approver
        skill.tools.forEach { it.bind(approver) }
    }

    /** 事件出口。Koog 的事件钩子在 agent 跑的时候触发，这里转给当前这次 send 的消费者。 */
    private var sink: (suspend (AgentEvent) -> Unit)? = null

    /** 同一个 session 的 send 串行执行 —— sink 只有一份，并发 send 会串台。 */
    private val mutex = Mutex()

    private val agent = AIAgent(
        promptExecutor = executor,
        agentConfig = AIAgentConfig(
            prompt(skill.id, LLMParams(maxTokens = skill.maxTokens)) { system(skill.systemPrompt) },
            model,
            skill.maxTurns,
        ),
        toolRegistry = ToolRegistryBuilder().tools(skill.tools).build(),
        installFeatures = {
            // ChatMemory：多轮对话历史的官方实现，按 session 的 runId 存取
            install(ChatMemory.Feature) { windowSize(24) }
            handleEvents {
                onLLMCallCompleted { ctx ->
                    ctx.response?.parts?.forEach { part ->
                        when (part) {
                            is MessagePart.Text ->
                                if (part.text.isNotBlank()) sink?.invoke(AgentEvent.Text(part.text))
                            is MessagePart.Tool.Call ->
                                // 模型偶尔会吐非法 JSON，解析失败也别把整轮带崩
                                sink?.invoke(AgentEvent.ToolUse(part.tool, part.argsJson))
                            else -> {}
                        }
                    }
                }
            }
        },
    )

    /** 一次建、多次用：多轮对话历史由 Koog 的 ChatMemory feature 按 session 存取。 */
    private val session = agent.createSession()

    override fun send(input: String): Flow<AgentEvent> = channelFlow {
        mutex.withLock {
            sink = { event -> send(event) }
            try {
                val output = session.run(input)
                send(AgentEvent.Result(output))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                send(AgentEvent.Error(e.message ?: "agent 执行失败"))
            } finally {
                sink = null
            }
        }
    }
}

/**
 * 模型 id → Koog 的 LLModel。
 *
 * Koog 1.3.0 的模型表只到 `claude-opus-5`，不认识 `claude-opus-5-5` 这类新 id。
 * 这时就近找"同族最接近的已知模型"当模板，逐级剥版本后缀：
 * `claude-opus-5-5` → `claude-opus-5` → `claude-opus`。
 *
 * 模板只用来抄上下文/输出上限和能力集，id 换成新的那个。
 * 千万别写死老数字 —— Opus 5.5 是 1M 上下文 / 128K 输出，
 * 声明小了 Koog 会提前截历史，声明大了直接被 API 拒。
 */
internal fun resolveModel(id: String, modelVersions: MutableMap<LLModel, String>): LLModel {
    AnthropicModels.models.firstOrNull { it.id == id }?.let { model ->
        // 自定义模型是全局注册的，别处可能已经注册过；本 runtime 的版本表里还没有就补一条
        if (modelVersions[model] == null) modelVersions[model] = model.id
        return model
    }
    val template = closestKnownModel(id)
    val model = LLModel(
        provider = LLMProvider.Anthropic,
        id = id,
        // Opus 5.x 把 temperature/top_p 从 API 里删了，带过去直接 400 —— 这个能力别声明
        capabilities = (template?.capabilities ?: DEFAULT_CAPABILITIES) - LLMCapability.Temperature,
        contextLength = template?.contextLength ?: DEFAULT_CONTEXT_LENGTH,
        maxOutputTokens = template?.maxOutputTokens ?: DEFAULT_MAX_OUTPUT_TOKENS,
    )
    AnthropicModels.addCustomModel(model)
    modelVersions[model] = model.id
    return model
}

/** 逐级剥版本后缀找同族已知模型。找不到同族就返回 null（走保守默认值）。 */
private fun closestKnownModel(id: String): LLModel? {
    var candidate = id
    while (true) {
        AnthropicModels.models.firstOrNull { it.id == candidate }?.let { return it }
        val cut = candidate.lastIndexOf('-')
        if (cut <= 0) return null
        candidate = candidate.substring(0, cut)
    }
}

/** 连族谱都对不上的 id（自定义代理模型之类）用这套保守值：宁可少截历史，也不要撑爆上下文。 */
private val DEFAULT_CAPABILITIES = listOf(
    LLMCapability.Completion,
    LLMCapability.Tools,
    LLMCapability.ToolChoice,
    LLMCapability.Thinking,
    LLMCapability.PromptCaching,
)
private const val DEFAULT_CONTEXT_LENGTH = 200_000L
private const val DEFAULT_MAX_OUTPUT_TOKENS = 64_000L
