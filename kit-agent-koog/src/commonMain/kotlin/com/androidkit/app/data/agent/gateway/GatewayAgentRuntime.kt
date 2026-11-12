package com.androidkit.app.data.agent.gateway

import com.androidkit.app.data.agent.AgentEvent
import com.androidkit.app.data.agent.AgentRuntime
import com.androidkit.app.data.agent.AgentSession
import com.androidkit.app.data.agent.SkillDefinition
import com.androidkit.app.data.agent.ToolApprover
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * **后端模式**的 agent 运行时：agent loop 跑在 `backend/` 网关上，app 只负责收发。
 *
 * 和 [com.androidkit.app.data.agent.KoogAgentRuntime] 实现同一个 [AgentRuntime] 契约，
 * 所以 UI 一行不用改，只换实例：
 *
 * ```kotlin
 * val runtime = createAgentRuntime(
 *     AgentMode.Gateway(
 *         baseUrl = "https://agent.example.com",
 *         token = token,             // 从平台安全存储读出来（Keystore / Keychain）
 *         httpClient = ktorClient,   // 引擎由 App 挂：Android/Desktop OkHttp、iOS Darwin
 *     )
 * )
 * val session = runtime.session(skill, ToolApprover.AllowAll)
 * ```
 *
 * ## 和本地模式的三处差异
 *
 * 1. **skill 有两份定义**：本地是 Kotlin `SkillDefinition`，服务端是 `skills/<id>/SKILL.md`。
 *    这里按 [SkillDefinition.id] 去调 `/v1/skills/<id>/invoke`，两边要自己保持一致。
 * 2. **工具在服务端执行**：所以 [ToolApprover] 在网关模式下**拦不住工具**，本地只会收到
 *    `ToolUse`（且不带入参）。危险工具的白名单由服务端 `SKILL.md` 的 `tools` 字段控制。
 * 3. **文本不是增量**：网关的 `text` 事件是一条完整消息；本地模式是逐块增量。
 *    UI 都是往里追加，所以显示逻辑不用改，但别指望打字机速度。
 *
 * ## 入参协议
 *
 * `AgentSession.send(input: String)` 只有一个字符串，而网关要的是 skill 自己的 JSON 对象。
 * 默认包成 `{"input": "<文本>"}`；skill 的 schema 字段叫别的（如 summarize 的 `text`），
 * 就把 [inputField] 改成那个字段名。
 *
 * @param baseUrl 网关根地址（`https://host`，不带 `/v1`）
 * @param token 网关 token；**别打进 APK**，由 App 从平台安全存储读出后传入
 * @param httpClient Ktor 客户端（引擎按平台挂，超时/重试由 App 配）
 * @param inputField 文本参数在 skill input 对象里的字段名，默认 `input`
 */
class GatewayAgentRuntime internal constructor(
    private val invoker: GatewayInvoker,
) : AgentRuntime {

    constructor(
        baseUrl: String,
        token: String,
        httpClient: HttpClient,
        inputField: String = "input",
    ) : this(KtorGatewayInvoker(baseUrl, token, httpClient, inputField))

    override fun session(skill: SkillDefinition, approver: ToolApprover): AgentSession =
        GatewayAgentSession(skill, invoker)
}

/** 一次 invoke 的传输层接缝（单测注入假实现，不碰网络）。 */
internal fun interface GatewayInvoker {
    suspend fun invoke(skillId: String, input: String, sessionId: String?): Flow<GatewayFrame>
}

/**
 * 一个网关会话：记住 `sessionId` 并串行化 send。
 *
 * 网关按 `sessionId` 做 resume，且会校验归属；所以 id 必须从 `start` 事件拿，不能自己编。
 */
internal class GatewayAgentSession(
    private val skill: SkillDefinition,
    private val invoker: GatewayInvoker,
) : AgentSession {

    private var sessionId: String? = null

    /** 同一个 session 的 send 串行跑：sessionId 只有一份，并发会串台。 */
    private val mutex = Mutex()

    override fun send(input: String): Flow<AgentEvent> = flow {
        mutex.withLock {
            var finished = false
            try {
                invoker.invoke(skill.id, input, sessionId).collect { frame ->
                    when (frame) {
                        is GatewayFrame.Start -> sessionId = frame.sessionId
                        is GatewayFrame.Text -> emit(AgentEvent.Text(frame.text))
                        // 网关只给工具名，没有入参 —— 别假装有
                        is GatewayFrame.Tool -> emit(AgentEvent.ToolUse(frame.name, EMPTY_ARGS))
                        is GatewayFrame.Result -> {
                            emit(AgentEvent.Result(frame.output.gatewayText()))
                            finished = true
                        }
                        is GatewayFrame.Failure -> {
                            emit(AgentEvent.Error(frame.message))
                            finished = true
                        }
                    }
                }
                // 连接被中途掐断（没 result 也没 error）：给 UI 一个明确的终态，
                // 否则按钮会一直转圈
                if (!finished) emit(AgentEvent.Error("网关连接中断：这一轮没有收到 result"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // 已经拿到结果就别再报一个错盖住它
                if (!finished) emit(AgentEvent.Error(e.message ?: "网关请求失败"))
            }
        }
    }

    private companion object {
        val EMPTY_ARGS = JsonObject(emptyMap())
    }
}

/**
 * 把网关的 `result.output` 转成 UI 能显示的一段文本。
 *
 * 约定顺序：`{"text": "…"}` → 取 text；纯字符串 → 原样；其他对象/数组 → 紧凑 JSON。
 * 需要更精细的渲染就自己消费原始输出，别在运行时里塞业务格式。
 */
internal fun JsonElement.gatewayText(): String = when (this) {
    is JsonNull -> ""
    is JsonPrimitive -> content
    is JsonObject -> (this["text"] as? JsonPrimitive)?.content ?: toString()
    else -> toString()
}

/**
 * Ktor 版传输层：POST + 手拆 SSE（不额外引 ktor-client-sse）。
 *
 * 非 2xx 统一转成 [GatewayFrame.Failure]，把状态码和服务端的错误文本带给 UI。
 */
internal class KtorGatewayInvoker(
    baseUrl: String,
    private val token: String,
    private val httpClient: HttpClient,
    private val inputField: String,
) : GatewayInvoker {

    private val endpoint = baseUrl.trimEnd('/') + "/v1/skills"

    override suspend fun invoke(skillId: String, input: String, sessionId: String?): Flow<GatewayFrame> = flow {
        val collector = this
        val url = "$endpoint/${skillId.encodeURLPathPart()}/invoke"
        val body = buildJsonObject {
            put("input", buildJsonObject { put(inputField, input) })
            sessionId?.let { put("sessionId", it) }
        }

        httpClient.preparePost(url) {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            accept(ContentType.Text.EventStream)
            setBody(body.toString())
        }.execute { response ->
            if (!response.status.isSuccess()) {
                // 服务端的 error JSON 在 body 里，能读就读；读不到也不能把错误盖成崩溃
                val detail = runCatching { response.bodyAsText() }
                    .getOrElse { if (it is CancellationException) throw it else null }
                    ?.takeIf { it.isNotBlank() }
                collector.emit(
                    GatewayFrame.Failure(
                        "网关返回 HTTP ${response.status.value}" + (detail?.let { "：$it" } ?: "")
                    )
                )
                return@execute
            }
            collector.readSse(response.bodyAsChannel())
        }
    }

    /** 按 SSE 规范拆帧：`event:` / `data:` 累积到空行，再交给 [GatewaySse]。 */
    private suspend fun FlowCollector<GatewayFrame>.readSse(channel: ByteReadChannel) {
        var event: String? = null
        val data = StringBuilder()

        while (true) {
            val line = channel.readUTF8Line() ?: break
            when {
                line.isEmpty() -> {
                    GatewaySse.parse(event, data.toString())?.let { emit(it) }
                    event = null
                    data.setLength(0)
                }
                line.startsWith("event:") -> event = line.removePrefix("event:").trim()
                line.startsWith("data:") -> {
                    if (data.isNotEmpty()) data.append('\n')
                    data.append(line.removePrefix("data:").removePrefix(" "))
                }
                // id: / retry: / 注释行（以 : 开头）直接忽略
            }
        }

        // 最后一帧没跟空行也补解析一次
        if (data.isNotEmpty()) GatewaySse.parse(event, data.toString())?.let { emit(it) }
    }
}
