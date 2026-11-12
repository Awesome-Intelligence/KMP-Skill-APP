package com.androidkit.app.data.agent

import com.androidkit.app.ui.agent.ChatMessage
import com.androidkit.app.ui.agent.ChatStep
import com.androidkit.app.ui.agent.ChatStepStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 一份可渲染的聊天记录 —— 把 agent 事件流变成 UI 能直接画的列表。
 *
 * app 只认这个，不认 [AgentEvent]：事件是**过程**（一次工具调用、一段增量文本），
 * 消息是**结果**（一条能留在屏幕上的气泡）。中间的映射写在这里，app 就不用各写一遍。
 *
 * ```kotlin
 * val log = remember { ChatLog() }
 * val messages by log.messages.collectAsState()
 * // ...
 * log.user(text)
 * session.send(text).collect { log.onEvent(it) }
 * ```
 */
class ChatLog {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())

    /** 当前消息列表。 */
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    /** 用户发出去的话。 */
    fun user(text: String) {
        _messages.value = _messages.value + ChatMessage.User(text)
    }

    /** 吃一个 agent 事件。 */
    fun onEvent(event: AgentEvent) {
        when (event) {
            is AgentEvent.Text -> appendText(event.text)

            is AgentEvent.ToolUse -> startStep(event.name, event.input.toString())

            is AgentEvent.ToolDone -> finishStep(event.name, ChatStepStatus.Done, event.result)

            is AgentEvent.ToolFailed -> finishStep(event.name, ChatStepStatus.Failed, event.message)

            // 正文已经由 Text 事件流式发过了；这里只在"一个字都没收到"时兜底补上
            is AgentEvent.Result -> if (lastAssistant()?.text.isNullOrEmpty()) appendText(event.text)

            is AgentEvent.Error -> _messages.value = _messages.value + ChatMessage.Error(event.message)
        }
    }

    /** 增量文本：追加到最后一条助手消息的尾巴上（没有就先开一条）。 */
    private fun appendText(delta: String) {
        if (delta.isEmpty()) return
        val list = _messages.value
        val last = list.lastOrNull()
        if (last is ChatMessage.Assistant) {
            _messages.value = list.dropLast(1) + last.copy(text = last.text + delta)
        } else {
            _messages.value = list + ChatMessage.Assistant(text = delta)
        }
    }

    /** 工具开始跑：挂一条"进行中"的步骤。 */
    private fun startStep(tool: String, args: String) {
        val list = _messages.value
        val last = list.lastOrNull()
        val assistant = last as? ChatMessage.Assistant ?: ChatMessage.Assistant()
        _messages.value = list.dropLast(if (last is ChatMessage.Assistant) 1 else 0) +
            assistant.copy(steps = assistant.steps + ChatStep(tool = tool, args = args))
    }

    /** 工具跑完（或失败）：把最后一条同名、还在跑的步骤收尾。 */
    private fun finishStep(tool: String, status: ChatStepStatus, detail: String) {
        val list = _messages.value
        val last = list.lastOrNull() as? ChatMessage.Assistant ?: return
        val index = last.steps.indexOfLast { it.tool == tool && it.status == ChatStepStatus.Running }
        if (index < 0) return
        val steps = last.steps.toMutableList()
        steps[index] = steps[index].copy(status = status, result = detail)
        _messages.value = list.dropLast(1) + last.copy(steps = steps)
    }

    private fun lastAssistant(): ChatMessage.Assistant? = _messages.value.lastOrNull() as? ChatMessage.Assistant
}
