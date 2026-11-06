package com.androidkit.app.data.agent

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.ModerationResult
import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart
import ai.koog.prompt.message.ResponseMetaInfo
import ai.koog.prompt.streaming.StreamFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 验的是 KoogAgentSession 的核心承诺：**多轮复用同一个 session**，对话历史由 Koog 的
 * LLM context 自己攒着（不是我们手工往 prompt 里塞）。
 */
class KoogAgentSessionTest {

    private class FakeExecutor(private val chunks: List<String> = listOf("ok")) : PromptExecutor() {
        val prompts = mutableListOf<Prompt>()

        override suspend fun execute(
            prompt: Prompt,
            model: LLModel,
            tools: List<ToolDescriptor>,
        ): Message.Assistant {
            prompts += prompt
            return Message.Assistant(
                listOf<MessagePart.ResponsePart>(MessagePart.Text("ok")),
                ResponseMetaInfo.Empty,
                "stop",
                null,
                null,
            )
        }

        /** runtime 现在走的是流式这条路（execute → executeStreaming），所以假实现也得吐 frame。 */
        override fun executeStreaming(
            prompt: Prompt,
            model: LLModel,
            tools: List<ToolDescriptor>,
        ): Flow<StreamFrame> = flow {
            prompts += prompt
            chunks.forEach { emit(StreamFrame.TextDelta(it, 0)) }
            emit(StreamFrame.TextComplete(chunks.joinToString(""), 0))
            emit(StreamFrame.End("stop", ResponseMetaInfo.Empty))
        }

        override suspend fun moderate(prompt: Prompt, model: LLModel): ModerationResult =
            ModerationResult(false, emptyMap())

        override fun close() = Unit
    }

    @Test
    fun 多轮_send_历史会累积() = runBlocking {
        val executor = FakeExecutor()
        val skill = SkillDefinition(
            id = "test",
            name = "测试",
            description = "",
            systemPrompt = "你是测试助手",
        )
        val model = resolveModel(
            "claude-opus-5-5",
            AnthropicClientSettings().modelVersionsMap.toMutableMap(),
        )
        val session = KoogAgentSession(skill, executor, model, ToolApprover.AllowAll)

        session.send("第一句").collect { }
        session.send("第二句").collect { }

        assertEquals(2, executor.prompts.size)
        val second = executor.prompts[1].messages
        assertTrue(
            second.any { m ->
                m is Message.User && m.parts.any { p ->
                    (p as? MessagePart.Text)?.text == "第一句"
                }
            },
            "第二轮的 prompt 里应该还留着第一轮，实际=$second",
        )
    }

    /**
     * Koog 1.3.0 不认识 claude-opus-5-5，得就地造一条。
     * 造的时候照同族（claude-opus-5）的上限和能力集，别用 200K/64K 那套老数字。
     */
    @Test
    fun 未知模型_照同族模板登记() {
        val versions = AnthropicClientSettings().modelVersionsMap.toMutableMap()
        val model = resolveModel("claude-opus-5-5", versions)

        assertEquals("claude-opus-5-5", model.id)
        assertEquals(1_000_000L, model.contextLength)
        assertEquals(128_000L, model.maxOutputTokens)
        // Opus 5.x 不接受 temperature，发过去直接 400
        assertFalse(model.supports(LLMCapability.Temperature))
        // Thinking 必须留着：Opus 5.5 响应里一定带 thinking 块，Koog 得能解析并原样回传
        assertTrue(model.supports(LLMCapability.Thinking))
        assertTrue(model.supports(LLMCapability.Tools))
    }

    /**
     * 未知模型**不进** Koog 的全局模型表 —— 那表是进程共享的 ArrayList，
     * add 不去重也不加锁，每开一个 session 就多一条一模一样的记录，还污染别的 runtime。
     * 真正发请求用的是 runtime 自己的版本表（client 读 settings.modelVersionsMap）。
     *
     * 用一个专属的探针 id：别的用例要是把 claude-opus-5-5 登记进全局表，
     * resolveModel 就会走"已知模型"分支，这个断言就测不出东西了。
     */
    @Test
    fun 未知模型_不进全局模型表() {
        val probe = "claude-opus-5-5-probe"
        val versions = AnthropicClientSettings().modelVersionsMap.toMutableMap()
        val before = AnthropicModels.models.count { it.id == probe }

        val model = resolveModel(probe, versions)

        assertEquals(
            before,
            AnthropicModels.models.count { it.id == probe },
            "resolveModel 往 Koog 全局模型表里塞东西了",
        )
        // 版本串要进 runtime 自己的表 —— client 就是从这儿取模型名的
        assertEquals(probe, versions[model])
    }

    /** 同一个 id 解析多次，版本表里只留一条（LLModel 是 data class，map 按值去重）。 */
    @Test
    fun 重复解析_不堆重复条目() {
        val versions = AnthropicClientSettings().modelVersionsMap.toMutableMap()

        val first = resolveModel("claude-opus-5-5", versions)
        val second = resolveModel("claude-opus-5-5", versions)

        assertEquals(first, second)
        assertEquals(1, versions.count { it.key.id == "claude-opus-5-5" })
    }

    /**
     * 流式：模型的文本增量**逐块**发出来，UI 追加起来就是打字机效果。
     * 之前是整段返回（一个 Text 事件），所以这条断言专门盯增量。
     */
    @Test
    fun 文本增量_逐块发出来() = runBlocking {
        val executor = FakeExecutor(listOf("你", "好", "！"))
        val skill = SkillDefinition(
            id = "test",
            name = "测试",
            description = "",
            systemPrompt = "你是测试助手",
        )
        val model = resolveModel(
            "claude-opus-5-5",
            AnthropicClientSettings().modelVersionsMap.toMutableMap(),
        )
        val session = KoogAgentSession(skill, executor, model, ToolApprover.AllowAll)

        val events = mutableListOf<AgentEvent>()
        session.send("嗨").collect { events += it }

        assertEquals(
            listOf("你", "好", "！"),
            events.filterIsInstance<AgentEvent.Text>().map { it.text },
            "文本增量没有逐块发出来，实际=$events",
        )
        // 最终回复是完整那段，且只发一次（UI 追加增量后再追加 Result 就重复了）
        val results = events.filterIsInstance<AgentEvent.Result>()
        assertEquals(1, results.size)
        assertEquals("你好！", results.single().text)
    }
}
