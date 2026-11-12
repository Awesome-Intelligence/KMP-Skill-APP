package com.androidkit.app.data.agent.gateway

import com.androidkit.app.data.agent.AgentEvent
import com.androidkit.app.data.agent.SkillDefinition
import com.androidkit.app.data.agent.ToolApprover
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GatewayAgentRuntimeTest {

    private val skill = SkillDefinition(
        id = "chat",
        name = "对话",
        description = "",
        systemPrompt = "你是测试助手",
    )

    /**
     * 假传输层：记录调用参数，按脚本回帧。
     * 这样协议映射、sessionId 续接、错误兜底都能测，不用起 HTTP。
     */
    private class FakeInvoker(private val frames: List<GatewayFrame>) : GatewayInvoker {
        data class Call(val skillId: String, val input: String, val sessionId: String?)

        val calls = mutableListOf<Call>()

        override suspend fun invoke(skillId: String, input: String, sessionId: String?): Flow<GatewayFrame> = flow {
            calls += Call(skillId, input, sessionId)
            frames.forEach { emit(it) }
        }
    }

    @Test
    fun 事件映射_并把skillId和输入透传下去() = runBlocking {
        val invoker = FakeInvoker(
            listOf(
                GatewayFrame.Start("s-1"),
                GatewayFrame.Tool("now"),
                GatewayFrame.Text("你好"),
                GatewayFrame.Result(JsonPrimitive("完成")),
            )
        )
        val session = GatewayAgentRuntime(invoker).session(skill, ToolApprover.AllowAll)

        val events = session.send("嗨").toList()

        val call = invoker.calls.single()
        assertEquals("chat", call.skillId)
        assertEquals("嗨", call.input)
        assertNull(call.sessionId, "第一轮还没有 sessionId")
        assertEquals(listOf("你好"), events.filterIsInstance<AgentEvent.Text>().map { it.text })
        assertEquals(listOf("now"), events.filterIsInstance<AgentEvent.ToolUse>().map { it.name })
        assertEquals(listOf("完成"), events.filterIsInstance<AgentEvent.Result>().map { it.text })
    }

    @Test
    fun 第二轮send_自动带上第一轮的sessionId() = runBlocking {
        val invoker = FakeInvoker(listOf(GatewayFrame.Start("s-1"), GatewayFrame.Result(JsonPrimitive("ok"))))
        val session = GatewayAgentRuntime(invoker).session(skill, ToolApprover.AllowAll)

        session.send("第一句").toList()
        session.send("第二句").toList()

        assertEquals(listOf(null, "s-1"), invoker.calls.map { it.sessionId })
    }

    @Test
    fun 结果对象_优先取text字段() = runBlocking {
        val output = buildJsonObject {
            put("text", "答案")
            put("confidence", 0.9)
        }
        val invoker = FakeInvoker(listOf(GatewayFrame.Result(output)))
        val events = GatewayAgentRuntime(invoker).session(skill, ToolApprover.AllowAll).send("问").toList()

        assertEquals("答案", events.filterIsInstance<AgentEvent.Result>().single().text)
    }

    @Test
    fun 结果对象没有text_退化成JSON文本() = runBlocking {
        val output = buildJsonObject { put("summary", "摘要") }
        val invoker = FakeInvoker(listOf(GatewayFrame.Result(output)))
        val events = GatewayAgentRuntime(invoker).session(skill, ToolApprover.AllowAll).send("问").toList()

        val text = events.filterIsInstance<AgentEvent.Result>().single().text
        assertTrue(text.contains("\"summary\""), "没 text 字段时应给紧凑 JSON，实际=$text")
    }

    @Test
    fun 错误帧_只发Error不发Result() = runBlocking {
        val invoker = FakeInvoker(listOf(GatewayFrame.Start("s-1"), GatewayFrame.Failure("skill 不存在")))
        val events = GatewayAgentRuntime(invoker).session(skill, ToolApprover.AllowAll).send("嗨").toList()

        assertEquals(listOf("skill 不存在"), events.filterIsInstance<AgentEvent.Error>().map { it.message })
        assertTrue(events.none { it is AgentEvent.Result })
    }

    /** 连接被掐断时，UI 必须收到终态，否则发送按钮会一直转圈。 */
    @Test
    fun 流提前结束_补一个连接中断的Error() = runBlocking {
        val invoker = FakeInvoker(listOf(GatewayFrame.Start("s-1"), GatewayFrame.Text("说到一半")))
        val events = GatewayAgentRuntime(invoker).session(skill, ToolApprover.AllowAll).send("嗨").toList()

        assertTrue(events.none { it is AgentEvent.Result })
        assertTrue(events.last() is AgentEvent.Error)
    }

    @Test
    fun 传输层抛异常_变成Error而不是崩掉收集方() = runBlocking {
        val invoker = GatewayInvoker { _, _, _ -> throw IllegalStateException("断网") }
        val events = GatewayAgentRuntime(invoker).session(skill, ToolApprover.AllowAll).send("嗨").toList()

        assertEquals(listOf("断网"), events.filterIsInstance<AgentEvent.Error>().map { it.message })
    }
}
