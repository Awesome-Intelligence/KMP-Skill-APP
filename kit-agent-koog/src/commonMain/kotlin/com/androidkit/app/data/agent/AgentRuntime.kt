package com.androidkit.app.data.agent

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject

/**
 * agent 引擎的公共契约。UI / 业务代码只认这一层，换引擎不用改 app 代码。
 */
interface AgentRuntime {
    /**
     * 开一个会话。
     *
     * @param approver 工具执行前的确认闸门：危险工具必须在这里被拦一道，让 UI 弹确认。
     */
    fun session(skill: SkillDefinition, approver: ToolApprover): AgentSession
}

/**
 * 一个会话 = 一个 skill + 一段持续累积的对话历史。
 * 多轮对话复用同一个 session；每个 skill 开自己的 session。
 */
interface AgentSession {
    /**
     * 说一句话，跑完 agent loop（模型 → 工具 → 模型…），事件流式吐给 UI。
     * 取消 collect 即中止。
     */
    fun send(input: String): Flow<AgentEvent>
}

/**
 * 工具执行前的确认闸门：返回 false 表示拒绝，工具不会执行。
 * 工具跑在用户自己的手机上，删文件 / 发消息 / 花钱这类动作必须先过这里。
 */
fun interface ToolApprover {
    suspend fun approve(name: String, input: JsonObject): Boolean

    companion object {
        /** 全放行：只给"工具全是只读、无副作用"的 app 用。 */
        val AllowAll: ToolApprover = ToolApprover { _, _ -> true }
    }
}
