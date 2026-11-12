package com.androidkit.app.data.agent

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeAgentRuntimeTest {

    private val skill = SkillDefinition(
        id = "test",
        name = "测试",
        description = "",
        systemPrompt = "你是测试助手",
    )

    @Test
    fun 文本_按块吐_最后给整段() = runBlocking {
        val runtime = FakeAgentRuntime(listOf(FakeReply.Text("你好世界", chunkSize = 2)))
        val events = runtime.session(skill, ToolApprover.AllowAll).send("嗨").toList()

        assertEquals(listOf("你好", "世界"), events.filterIsInstance<AgentEvent.Text>().map { it.text })
        // Result 是完整那段，只发一次 —— UI 追加增量后再追加它就会显示两段
        assertEquals(listOf("你好世界"), events.filterIsInstance<AgentEvent.Result>().map { it.text })
        assertEquals(listOf("嗨"), runtime.inputs)
    }

    @Test
    fun 多轮_按顺序消费_用完重复最后一条() = runBlocking {
        val runtime = FakeAgentRuntime(
            listOf(FakeReply.Text("第一轮"), FakeReply.Text("第二轮")),
        )
        val session = runtime.session(skill, ToolApprover.AllowAll)

        assertEquals("第一轮", session.send("a").toList().filterIsInstance<AgentEvent.Result>().single().text)
        assertEquals("第二轮", session.send("b").toList().filterIsInstance<AgentEvent.Result>().single().text)
        assertEquals("第二轮", session.send("c").toList().filterIsInstance<AgentEvent.Result>().single().text)
        assertEquals(listOf("a", "b", "c"), runtime.inputs)
    }

    @Test
    fun 工具调用_先ToolUse再继续回答() = runBlocking {
        val args = JsonObject(mapOf("id" to JsonPrimitive("t1")))
        val runtime = FakeAgentRuntime(
            listOf(FakeReply.ToolCall("delete_todo", args, then = "删好了")),
        )
        val events = runtime.session(skill, ToolApprover.AllowAll).send("删掉 t1").toList()

        val tool = events.filterIsInstance<AgentEvent.ToolUse>().single()
        assertEquals("delete_todo", tool.name)
        assertEquals("t1", tool.input["id"]?.jsonPrimitive?.content)
        assertEquals("删好了", events.filterIsInstance<AgentEvent.Result>().single().text)
        // 事件顺序：工具调用发生在回答之前
        assertTrue(events.indexOfFirst { it is AgentEvent.ToolUse } < events.indexOfFirst { it is AgentEvent.Result })
    }

    @Test
    fun 失败_只发Error不发Result() = runBlocking {
        val events = FakeAgentRuntime(listOf(FakeReply.Failure("鉴权失败")))
            .session(skill, ToolApprover.AllowAll)
            .send("hi")
            .toList()

        assertEquals(listOf("鉴权失败"), events.filterIsInstance<AgentEvent.Error>().map { it.message })
        assertTrue(events.none { it is AgentEvent.Result })
    }

    @Test
    fun 空文本_不产生空增量块() = runBlocking {
        val events = FakeAgentRuntime(listOf(FakeReply.Text("")))
            .session(skill, ToolApprover.AllowAll)
            .send("hi")
            .toList()

        assertTrue(events.none { it is AgentEvent.Text })
        assertEquals("", events.filterIsInstance<AgentEvent.Result>().single().text)
    }
}
