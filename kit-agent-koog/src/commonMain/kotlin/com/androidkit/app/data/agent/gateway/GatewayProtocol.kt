package com.androidkit.app.data.agent.gateway

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/**
 * 网关 SSE 的一帧。
 *
 * 刻意和 UI 的 [com.androidkit.app.data.agent.AgentEvent] 分开：
 * 这一层只认后端的线上协议（见 `backend/README.md`），映射成 AgentEvent 是
 * [GatewayAgentSession] 的事。协议改了只动这里，UI 不用改。
 */
internal sealed interface GatewayFrame {
    /** 会话 id —— 后续 send 要带回去才能接着上一轮（网关会校验归属）。 */
    data class Start(val sessionId: String) : GatewayFrame

    /** 一条**完整**的 assistant 文本消息（不是增量块，别和本地流式混淆）。 */
    data class Text(val text: String) : GatewayFrame

    /** 模型调用了服务端工具。网关只给工具名，不给入参。 */
    data class Tool(val name: String) : GatewayFrame

    /** 已按 skill 的 output schema 校验过的结果。 */
    data class Result(val output: JsonElement) : GatewayFrame

    /** 服务端错误（鉴权、限流、skill 不存在、模型失败…）。 */
    data class Failure(val message: String) : GatewayFrame
}

/**
 * SSE 帧解析 —— 纯函数，不碰网络，方便单测。
 *
 * 后端写的是 `event: <type>` + `data: <json>`，两处的 type 是同一个值；
 * 这里优先看 JSON 里的 `type`，没有再退回 `event:` 行。
 */
internal object GatewaySse {

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(event: String?, data: String): GatewayFrame? {
        if (data.isBlank()) return null
        val obj = runCatching { json.parseToJsonElement(data).jsonObject }.getOrNull() ?: return null
        val type = obj.string("type") ?: event

        return when (type) {
            "start" -> obj.string("sessionId")?.let { GatewayFrame.Start(it) }
            "text" -> obj.string("text")?.let { GatewayFrame.Text(it) }
            "tool" -> obj.string("name")?.let { GatewayFrame.Tool(it) }
            // output 是任意 JSON（skill 自己定 schema），交给上层决定怎么展示
            "result" -> GatewayFrame.Result(obj["output"] ?: JsonNull)
            "error" -> GatewayFrame.Failure(obj.string("message") ?: "网关返回错误但没带 message")
            else -> null
        }
    }

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content
}
