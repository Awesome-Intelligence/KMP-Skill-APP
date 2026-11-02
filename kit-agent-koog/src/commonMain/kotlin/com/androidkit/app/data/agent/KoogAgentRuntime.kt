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

/** 模型 id → Koog 的 LLModel。Koog 不认识的 id（比如 claude-opus-5-5）就地登记一个。 */
internal fun resolveModel(id: String, modelVersions: MutableMap<LLModel, String>): LLModel {
    AnthropicModels.models.firstOrNull { it.id == id }?.let { model ->
        // 自定义模型是全局注册的，别处可能已经注册过；本 runtime 的版本表里还没有就补一条
        if (modelVersions[model] == null) modelVersions[model] = model.id
        return model
    }
    val model = LLModel(
        provider = LLMProvider.Anthropic,
        id = id,
        capabilities = listOf(
            LLMCapability.Completion,
            LLMCapability.Tools,
            LLMCapability.ToolChoice,
            LLMCapability.Temperature,
        ),
        contextLength = 200_000L,
        maxOutputTokens = 64_000L,
    )
    AnthropicModels.addCustomModel(model)
    modelVersions[model] = model.id
    return model
}
