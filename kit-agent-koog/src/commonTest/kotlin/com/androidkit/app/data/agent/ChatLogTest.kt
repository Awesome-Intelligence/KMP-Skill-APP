package com.androidkit.app.data.agent

import com.androidkit.app.ui.agent.ChatMessage
import com.androidkit.app.ui.agent.ChatStepStatus
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * 事件 → 消息的映射 —— UI 只认消息不认事件，映射错了气泡就长歪。
 */
class ChatLogTest {

    private val log = ChatLog()

    @Test
    fun 用户消息_单独一条() {
        log.user("我今天有几件事？")
        assertEquals(listOf(ChatMessage.User("我今天有几件事？")), log.messages.value)
    }

    @Test
    fun 文本增量_追加到同一条助手消息() {
        log.onEvent(AgentEvent.Text("你"))
        log.onEvent(AgentEvent.Text("有 3 件"))
        log.onEvent(AgentEvent.Text("待办"))

        val messages = log.messages.value
        assertEquals(1, messages.size)
        assertEquals("你有 3 件待办", assertIs<ChatMessage.Assistant>(messages.single()).text)
    }

    @Test
    fun 工具调用_变成一条步骤_跑完标完成() {
        val args = JsonObject(mapOf("id" to JsonPrimitive("t1")))
        log.onEvent(AgentEvent.ToolUse("delete_todo", args))
        log.onEvent(AgentEvent.ToolDone("delete_todo", "删掉了：t1"))

        val assistant = assertIs<ChatMessage.Assistant>(log.messages.value.single())
        val step = assistant.steps.single()
        assertEquals("delete_todo", step.tool)
        assertEquals(ChatStepStatus.Done, step.status)
        assertEquals("删掉了：t1", step.result)
    }

    @Test
    fun 工具失败_标失败并带原因() {
        log.onEvent(AgentEvent.ToolUse("delete_todo", JsonObject(emptyMap())))
        log.onEvent(AgentEvent.ToolFailed("delete_todo", "用户拒绝了工具 delete_todo 的执行"))

        val step = assertIs<ChatMessage.Assistant>(log.messages.value.single()).steps.single()
        assertEquals(ChatStepStatus.Failed, step.status)
        assertTrue(step.result!!.contains("拒绝"))
    }

    @Test
    fun 出错_单独一条错误消息() {
        log.onEvent(AgentEvent.Error("网络断了"))
        assertEquals(listOf(ChatMessage.Error("网络断了")), log.messages.value)
    }

    @Test
    fun Result_只在没收到正文时兜底补上() {
        log.onEvent(AgentEvent.Result("完整回复"))
        assertEquals("完整回复", assertIs<ChatMessage.Assistant>(log.messages.value.single()).text)

        val second = ChatLog()
        second.onEvent(AgentEvent.Text("增量"))
        second.onEvent(AgentEvent.Result("完整回复"))
        assertEquals("增量", assertIs<ChatMessage.Assistant>(second.messages.value.single()).text)
    }
}
