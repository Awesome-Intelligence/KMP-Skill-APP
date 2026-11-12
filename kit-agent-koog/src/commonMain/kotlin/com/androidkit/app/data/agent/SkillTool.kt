package com.androidkit.app.data.agent

import ai.koog.agents.core.tools.ToolBase
import ai.koog.agents.core.tools.ToolCallMetadata
import ai.koog.agents.core.tools.ToolException
import ai.koog.prompt.message.AttachmentContent
import ai.koog.prompt.message.AttachmentSource
import ai.koog.prompt.message.MessagePart
import ai.koog.serialization.JSONElement
import ai.koog.serialization.JSONObject
import ai.koog.serialization.JSONPrimitive
import ai.koog.serialization.JSONSerializer
import ai.koog.serialization.TypeToken
import ai.koog.serialization.kotlinx.KotlinxSerializer
import ai.koog.serialization.kotlinx.toKotlinxJsonObject
import ai.koog.serialization.typeToken

/**
 * skill 工具基类：Koog 原生工具 + 执行前的审批闸门。
 *
 * 入参是一个 `@Serializable` 数据类，模型的 `input_schema` 由 Koog 自动生成
 * （kotlinx-schema-generator），**不用手写 JSON Schema，也不用转 Koog descriptor**。
 *
 * ```kotlin
 * @Serializable
 * data class SearchArgs(val query: String, val limit: Int = 10)
 *
 * class SearchTool(private val store: Store) : SkillTool<SearchArgs>(
 *     typeToken<SearchArgs>(), "search", "搜索本地数据",
 * ) {
 *     override suspend fun doExecute(args: SearchArgs): ToolOutput =
 *         ToolOutput.Text(store.search(args.query, args.limit))
 * }
 * ```
 *
 * 工具跑在用户自己的手机上、以用户身份执行 —— 删数据、外发数据这类动作会被
 * [ToolApprover] 拦一道，模型会收到"用户拒绝"并自己改道。
 */
abstract class SkillTool<A : Any>(
    argsType: TypeToken,
    name: String,
    description: String,
) : ToolBase<A, ToolOutput>(argsType, typeToken<ToolOutput>(), name, description) {

    /**
     * 审批闸门，由 [KoogAgentRuntime] 在建 session 时注入。
     *
     * ponytail: 闸门挂在 tool 实例上 —— 同一个 skill 实例同时服务两个 session 时，
     * 后建的 session 会覆盖前一个的 approver。一个 app 一般只有一个 approver，够用。
     */
    internal var approver: ToolApprover = ToolApprover.AllowAll
        internal set

    internal fun bind(approver: ToolApprover) {
        this.approver = approver
    }

    final override suspend fun execute(args: A, metadata: ToolCallMetadata): ToolOutput {
        // 危险工具在这里被拦一道：approver 不放行就不执行，拒绝理由回给模型让它自己改道
        val decision = approver.approve(name, encodeArgs(args, serializer).toKotlinxJsonObject())
        if (decision == ToolApproval.Deny) {
            throw ToolException.ValidationFailure("用户拒绝了工具 $name 的执行")
        }
        return doExecute(args)
    }

    /** 工具真正的干活逻辑。 */
    protected abstract suspend fun doExecute(args: A): ToolOutput

    /**
     * 给模型看的**字符串**形式 —— 只有文本，图不进字符串（图走 [encodeResultToParts]）。
     *
     * 默认实现会把整个结果 JSON 化，模型看到的是 `{"text":"..."}` 这种壳 —— 没用还费 token。
     */
    override fun encodeResultToString(result: ToolOutput, serializer: JSONSerializer): String = result.text

    /** 事件/日志里的 JSON 结果：只放文本（图太大，不进 JSON）。 */
    override fun encodeResult(result: ToolOutput, serializer: JSONSerializer): JSONElement =
        JSONObject(mapOf("text" to JSONPrimitive.of(result.text)))

    /**
     * 真正发给模型的部分：文本 + 图。
     *
     * Anthropic 的 tool_result 允许 image 块，Koog 这条路是通的
     * （客户端把 [MessagePart.Attachment] 映射成 base64 image）。
     * 所以"工具给模型看一张截图"这件事不用绕 vision 消息 —— 结果里直接带图就行。
     */
    override fun encodeResultToParts(
        result: ToolOutput,
        serializer: JSONSerializer,
    ): List<MessagePart.ContentPart> = when (result) {
        is ToolOutput.Text -> listOf(MessagePart.Text(result.text))
        is ToolOutput.TextWithImage -> listOf(
            MessagePart.Text(result.text),
            MessagePart.Attachment(
                source = AttachmentSource.Image(
                    content = AttachmentContent.Binary.Bytes(result.image.bytes),
                    format = result.image.format,
                    mimeType = result.image.mimeType,
                ),
            ),
        )
    }

    /**
     * 抹掉入参类型执行一次 —— 给黄金样例跑通用工具用（样例只存 JSON，不知道 A 是哪个类）。
     *
     * 走的是 [execute]，所以审批闸门照样生效。
     */
    @Suppress("UNCHECKED_CAST")
    internal suspend fun executeUntyped(args: Any?): ToolOutput =
        (this as SkillTool<Any>).execute(args as Any, ToolCallMetadata.EMPTY)

    private val serializer: JSONSerializer = KotlinxSerializer()
}
