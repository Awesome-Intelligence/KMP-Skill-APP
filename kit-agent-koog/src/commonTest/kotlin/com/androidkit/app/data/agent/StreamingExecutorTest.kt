package com.androidkit.app.data.agent

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.ModerationResult
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart
import ai.koog.prompt.message.ResponseMetaInfo
import ai.koog.prompt.streaming.StreamFrame
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 验的是「流式」的**时机**，不是顺序。
 *
 * `KoogAgentSessionTest` 只断言事件顺序 —— 那种断言对「先 `toList()` 攒完再遍历发」
 * 的实现同样成立：顺序完全正确，只是用户要等整段回复生成完才看到第一个字。
 * 所以这里用一道闸门：上层没把第一个增量交出去，上游流就不往下走。
 */
class StreamingExecutorTest {

    private val model = LLModel(
        provider = LLMProvider.Anthropic,
        id = "test-model",
        capabilities = emptyList(),
        contextLength = 200_000,
        maxOutputTokens = 8_000,
    )

    private val testPrompt = prompt("test") { system("你是测试助手") }

    @Test
    fun 增量_不等整条流收完就发出去() = runBlocking {
        val firstDeltaSeen = CompletableDeferred<Unit>()

        val delegate = object : PromptExecutor() {
            override suspend fun execute(
                prompt: Prompt,
                model: LLModel,
                tools: List<ToolDescriptor>,
            ): Message.Assistant = error("StreamingExecutor 应该走 executeStreaming")

            override fun executeStreaming(
                prompt: Prompt,
                model: LLModel,
                tools: List<ToolDescriptor>,
            ): Flow<StreamFrame> = flow {
                emit(StreamFrame.TextDelta("你", 0))
                // 只有上层把「你」交出去了才继续吐下一块。
                // 攒完再发的实现在这里会一直等到 withTimeout 取消。
                firstDeltaSeen.await()
                emit(StreamFrame.TextDelta("好", 0))
                emit(StreamFrame.TextComplete("你好", 0))
                emit(StreamFrame.End("stop", ResponseMetaInfo.Empty))
            }

            override suspend fun moderate(prompt: Prompt, model: LLModel): ModerationResult =
                ModerationResult(false, emptyMap())

            override fun close() = Unit
        }

        val emitted = mutableListOf<String>()
        val executor = StreamingExecutor(delegate) { text ->
            emitted += text
            if (text == "你") firstDeltaSeen.complete(Unit)
        }

        val response = withTimeout(5_000) { executor.execute(testPrompt, model, emptyList()) }

        assertEquals(listOf("你", "好"), emitted, "增量不是边收边发，实际=$emitted")
        assertEquals(
            "你好",
            response.parts.filterIsInstance<MessagePart.Text>().joinToString("") { it.text },
            "流完了还得拼回一条完整消息给 agent 用",
        )
    }

    /** provider 只给完整文本、没有增量帧时，兜底发一次，别让 UI 一个字都收不到。 */
    @Test
    fun 没有增量帧_用完整文本兜底() = runBlocking {
        val delegate = object : PromptExecutor() {
            override suspend fun execute(
                prompt: Prompt,
                model: LLModel,
                tools: List<ToolDescriptor>,
            ): Message.Assistant = error("StreamingExecutor 应该走 executeStreaming")

            override fun executeStreaming(
                prompt: Prompt,
                model: LLModel,
                tools: List<ToolDescriptor>,
            ): Flow<StreamFrame> = flow {
                emit(StreamFrame.TextComplete("整段回复", 0))
                emit(StreamFrame.End("stop", ResponseMetaInfo.Empty))
            }

            override suspend fun moderate(prompt: Prompt, model: LLModel): ModerationResult =
                ModerationResult(false, emptyMap())

            override fun close() = Unit
        }

        val emitted = mutableListOf<String>()
        StreamingExecutor(delegate) { emitted += it }
            .execute(testPrompt, model, emptyList())

        assertEquals(listOf("整段回复"), emitted)
    }
}
