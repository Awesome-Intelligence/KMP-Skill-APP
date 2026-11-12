package com.androidkit.app.data.agent

import com.androidkit.app.data.agent.gateway.GatewayAgentRuntime
import io.ktor.client.HttpClient

/**
 * agent 跑在哪 —— 底座支持的两种部署形态。
 *
 * | 模式 | loop 在哪 | 出网的是什么 | 适合 |
 * |---|---|---|---|
 * | [Local] | 手机 | prompt + 工具结果（直连模型） | 数据不出端、个人/内测 |
 * | [Gateway] | 你的服务器 | 只有一句输入 + 结果 | 正式上架、key 不上端、集中管控 |
 *
 * 两种模式实现的是同一个 [AgentRuntime] 契约，换模式不用改 UI：
 *
 * ```kotlin
 * val runtime = createAgentRuntime(
 *     if (gatewayEnabled) {
 *         AgentMode.Gateway(baseUrl, token, httpClient)
 *     } else {
 *         AgentMode.Local(apiKey)
 *     }
 * )
 * val session = runtime.session(skill, approver)
 * ```
 *
 * 注意：网关模式的工具在服务端执行，本地 [ToolApprover] 拦不住 —— 危险工具的白名单
 * 归服务端 `SKILL.md` 的 `tools` 字段管。
 */
sealed interface AgentMode {

    /**
     * 本机 loop（默认）：Koog 直连 Anthropic API，工具在本机执行，数据不出端。
     *
     * @param apiKey 模型 key。**别打进正式包**；上架场景用 [Gateway] 或自建转发代理
     * @param baseUrl 直连地址；指向自己的转发代理时 key 留在代理上
     */
    data class Local(
        val apiKey: String,
        val baseUrl: String = "https://api.anthropic.com",
    ) : AgentMode

    /**
     * 后端 loop：app 只发输入、收事件，模型调用和工具执行都在 `backend/` 网关上。
     *
     * @param baseUrl 网关根地址（`https://host`，不带 `/v1`）
     * @param token 网关 token，从平台安全存储读出后传入（别写进 BuildConfig）
     * @param httpClient Ktor 客户端，引擎按平台挂
     * @param inputField 文本参数在 skill input 对象里的字段名，默认 `input`
     */
    data class Gateway(
        val baseUrl: String,
        val token: String,
        val httpClient: HttpClient,
        val inputField: String = "input",
    ) : AgentMode
}

/** 按 [AgentMode] 造运行时 —— App 只认这一行，不直接 new 具体实现。 */
fun createAgentRuntime(mode: AgentMode): AgentRuntime = when (mode) {
    is AgentMode.Local -> KoogAgentRuntime(mode.apiKey, mode.baseUrl)
    is AgentMode.Gateway -> GatewayAgentRuntime(
        baseUrl = mode.baseUrl,
        token = mode.token,
        httpClient = mode.httpClient,
        inputField = mode.inputField,
    )
}
