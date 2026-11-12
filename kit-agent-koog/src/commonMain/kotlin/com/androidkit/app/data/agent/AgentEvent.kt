package com.androidkit.app.data.agent

import kotlinx.serialization.json.JsonObject

/** 本地 agent 的事件（给 UI 消费；进程内对象，不走线，不需要序列化） */
sealed class AgentEvent {
    /**
     * 模型的一段文本 —— **流式的增量块**，不是整段。
     *
     * 同一轮里会来很多次（模型边想边说）。UI 把它**追加**进当前气泡，
     * 拼起来才是完整回复（打字机效果）。
     */
    data class Text(val text: String) : AgentEvent()

    /** 模型要求执行一个工具；UI 可借此展示进度 */
    data class ToolUse(val name: String, val input: JsonObject) : AgentEvent()

    /**
     * 工具执行完了。
     *
     * UI 拿它把折叠条上的那一步标成"完成"，并把结果文案显示出来 ——
     * 过程折叠成一行，展开才看得到细节。
     */
    data class ToolDone(val name: String, val result: String) : AgentEvent()

    /** 工具没跑成：被用户拒绝、参数不对、抛异常…… [message] 是给用户看的一句话。 */
    data class ToolFailed(val name: String, val message: String) : AgentEvent()

    /**
     * 本轮 send 结束，[text] 是模型的最终回复。
     *
     * UI 注意：最终回复的内容**已经**由 [Text] 事件发过了，这条只用来标记"说完了"，
     * 别再追加一遍，否则会显示成两段。
     */
    data class Result(val text: String) : AgentEvent()

    /** 出错 */
    data class Error(val message: String) : AgentEvent()
}
