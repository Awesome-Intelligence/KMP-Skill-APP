package com.androidkit.app.data.agent

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.ModerationResult
import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart
import ai.koog.prompt.message.ResponseMetaInfo
import ai.koog.prompt.streaming.StreamFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 验的是 KoogAgentSession 的核心承诺：**多轮复用同一个 session**，对话历史由 Koog 的
 * LLM context 自己攒着（不是我们手工往 prompt 里塞）。
 */
class KoogAgentSessionTest {

    private class FakeExecutor : PromptExecutor() {
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

        override fun executeStreaming(
            prompt: Prompt,
            model: LLModel,
            tools: List<ToolDescriptor>,
        ): Flow<StreamFrame> = emptyFlow()

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
}
