package com.androidkit.app.data.agent

import ai.koog.agents.core.tools.ToolBase
import ai.koog.agents.core.tools.ToolCallMetadata
import ai.koog.agents.core.tools.ToolException
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
 *     override suspend fun doExecute(args: SearchArgs): String =
 *         store.search(args.query, args.limit)
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
) : ToolBase<A, String>(argsType, typeToken<String>(), name, description) {

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

    final override suspend fun execute(args: A, metadata: ToolCallMetadata): String {
        // 危险工具在这里被拦一道：approver 不放行就不执行，拒绝理由回给模型让它自己改道
        if (!approver.approve(name, encodeArgs(args, serializer).toKotlinxJsonObject())) {
            throw ToolException.ValidationFailure("用户拒绝了工具 $name 的执行")
        }
        return doExecute(args)
    }

    /** 工具真正的干活逻辑。 */
    protected abstract suspend fun doExecute(args: A): String

    private val serializer: JSONSerializer = KotlinxSerializer()
}
