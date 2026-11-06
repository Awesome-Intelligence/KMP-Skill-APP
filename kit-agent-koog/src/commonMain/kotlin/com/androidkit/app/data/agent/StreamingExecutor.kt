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
import kotlinx.coroutines.flow.toList

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
        val frames = delegate.executeStreaming(prompt, model, tools).toList()
        emitText(frames)
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

    /** 文本增量逐块发出去；provider 不给增量帧就整段发一次。 */
    private suspend fun emitText(frames: List<StreamFrame>) {
        val deltas = frames.filterIsInstance<StreamFrame.TextDelta>()
        if (deltas.isNotEmpty()) {
            deltas.forEach { onText(it.text) }
            return
        }
        // ponytail: 兜底 —— 有些 provider 只给完整文本，没有增量帧
        frames.filterIsInstance<StreamFrame.TextComplete>().lastOrNull()?.text
            ?.takeIf { it.isNotEmpty() }
            ?.let { onText(it) }
    }
}
