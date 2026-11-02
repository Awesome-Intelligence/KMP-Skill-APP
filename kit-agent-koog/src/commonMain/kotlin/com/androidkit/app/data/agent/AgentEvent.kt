package com.androidkit.app.data.agent

import kotlinx.serialization.json.JsonObject

/** 本地 agent 的事件（给 UI 消费；进程内对象，不走线，不需要序列化） */
sealed class AgentEvent {
    /**
     * 模型的一段文本。
     *
     * 同一轮里可能来多次（模型边想边说）。UI 把它追加进当前气泡。
     */
    data class Text(val text: String) : AgentEvent()

    /** 模型要求执行一个工具；UI 可借此展示进度 */
    data class ToolUse(val name: String, val input: JsonObject) : AgentEvent()

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
