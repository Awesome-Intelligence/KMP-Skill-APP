package com.androidkit.app.data.agent

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.ModerationResult
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.streaming.StreamFrame
import ai.koog.prompt.streaming.toMessageResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

/**
 * 流式 executor：把"整段返回"换成"边收边发"。
 *
 * agent 那边一行没动（还是调 execute()，还是 Koog 的默认 loop：工具调度、多轮历史、
 * 审批闸门都在原地）—— 只是这里把 execute() 改成走 delegate 的 executeStreaming()，
 * 收到文本增量就立刻回调一次，最后用 Koog 官方的 toMessageResponse()
 * 把所有 frame 拼回一条完整的 Message.Assistant，agent 该看到的照样看得到。
 *
 * 为什么不走 Koog 的节点 DSL（nodeLLMRequestStreaming）：那条路能流式，
 * 但要把整个 agent 图（工具循环的边、多轮历史的接法）用 DSL 重写一遍，
 * 风险和改动量都大一个数量级。包 executor 效果一样。
 */
internal class StreamingExecutor(
    private val delegate: PromptExecutor,
    private val onText: suspend (String) -> Unit,
) : PromptExecutor() {

    override suspend fun execute(
        prompt: Prompt,
        model: LLModel,
        tools: List<ToolDescriptor>,
    ): Message.Assistant {
        val frames = ArrayList<StreamFrame>()
        var streamedDelta = false

        // 边收边发：必须在 collect 里就回调。先 toList() 再遍历等于把整条流攒完才发，
        // 打字机效果会退化成「等十几秒，然后整段一次糊上来」。
        delegate.executeStreaming(prompt, model, tools).collect { frame ->
            frames += frame
            if (frame is StreamFrame.TextDelta && frame.text.isNotEmpty()) {
                streamedDelta = true
                onText(frame.text)
            }
        }

        // provider 不给增量帧、只给完整文本时兜底发一次
        if (!streamedDelta) {
            frames.filterIsInstance<StreamFrame.TextComplete>().lastOrNull()?.text
                ?.takeIf { it.isNotEmpty() }
                ?.let { onText(it) }
        }

        return frames.toMessageResponse()
    }

    override fun executeStreaming(
        prompt: Prompt,
        model: LLModel,
        tools: List<ToolDescriptor>,
    ): Flow<StreamFrame> = delegate.executeStreaming(prompt, model, tools)

    override suspend fun moderate(prompt: Prompt, model: LLModel): ModerationResult =
        delegate.moderate(prompt, model)

    override fun close() = delegate.close()
}
