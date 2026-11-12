package com.androidkit.app.data.agent

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonObject

/** 默认回复；放顶层 private，避免 `const` 在 JVM 上变成 public static 字段漏进 API 基线。 */
private const val DEFAULT_REPLY_TEXT = "（演示模式）这是一条本地假回复，没有调用模型。"

/**
 * 假 agent 运行时：不联网、不花钱、不需要 API key，按脚本吐事件。
 *
 * 它实现的是和 [KoogAgentRuntime] 同一个 [AgentRuntime] 契约，所以 UI 代码一行都不用改：
 *
 * ```kotlin
 * val runtime: AgentRuntime = if (BuildConfig.ANTHROPIC_API_KEY.isBlank()) {
 *     FakeAgentRuntime(listOf(FakeReply.Text("（演示模式）先随便聊两句。")))
 * } else {
 *     KoogAgentRuntime(BuildConfig.ANTHROPIC_API_KEY)
 * }
 * ```
 *
 * 三个用途：
 * - **预览/无 key 演示**：聊天页在没有 key 的机器上也能跑通
 * - **UI 单测**：不用 mock Koog 的整条链，直接断言气泡怎么长
 * - **复现 bug**：把线上事件序列录成 [FakeReply]，稳定重放
 *
 * 注意：审批闸门（[ToolApprover]）在这里**不生效** —— 闸门挂在 `SkillTool.execute` 上，
 * 而假运行时根本不执行工具。[FakeReply.ToolCall] 只发一条 [AgentEvent.ToolUse] 给 UI 看。
 *
 * @param replies 每轮 `send` 消费一条，用完之后重复最后一条
 * @param chunkDelayMillis 每个文本增量块之间的延迟，用来模拟打字机；0 = 不打字
 */
class FakeAgentRuntime(
    private val replies: List<FakeReply> = listOf(FakeReply.Text(DEFAULT_REPLY_TEXT)),
    private val chunkDelayMillis: Long = 0L,
) : AgentRuntime {

    init {
        require(replies.isNotEmpty()) { "FakeAgentRuntime 至少要有一条 FakeReply" }
    }

    private val _inputs = mutableListOf<String>()

    /** 所有 send 过的输入（按顺序），测试里用来断言 UI 真的把话送进来了。 */
    val inputs: List<String> get() = _inputs

    override fun session(skill: SkillDefinition, approver: ToolApprover): AgentSession =
        FakeAgentSession(
            replies = replies,
            chunkDelayMillis = chunkDelayMillis,
            onInput = { _inputs += it },
        )
}

/** 一轮假回复。 */
sealed interface FakeReply {

    /** 回一段文本；按 [chunkSize] 个字符切块，模拟流式增量。 */
    data class Text(val text: String, val chunkSize: Int = 8) : FakeReply

    /**
     * 先发一条工具调用事件，再（可选）回一段文本。
     *
     * 真正的工具执行不会发生 —— [then] 只是让 UI 看到「调用工具 → 继续回答」的完整事件序列。
     */
    data class ToolCall(
        val name: String,
        val input: JsonObject = JsonObject(emptyMap()),
        val then: String = "",
    ) : FakeReply

    /** 直接出错（鉴权失败、网络断开这类）。 */
    data class Failure(val message: String) : FakeReply
}

/** 一个假会话：无状态服务端，只按顺序消费 [replies]。 */
internal class FakeAgentSession(
    private val replies: List<FakeReply>,
    private val chunkDelayMillis: Long,
    private val onInput: (String) -> Unit,
) : AgentSession {

    private var turn = 0

    // send 的并发由 UI 保证串行；这里不额外加锁，避免把测试替身复杂化
    override fun send(input: String): Flow<AgentEvent> = flow {
        onInput(input)
        val reply = replies[turn.coerceAtMost(replies.lastIndex)]
        turn += 1

        when (reply) {
            is FakeReply.Text -> {
                emitChunks(reply.text, reply.chunkSize)
                emit(AgentEvent.Result(reply.text))
            }

            is FakeReply.ToolCall -> {
                emit(AgentEvent.ToolUse(reply.name, reply.input))
                emit(AgentEvent.ToolDone(reply.name, FAKE_TOOL_RESULT))
                emitChunks(reply.then, DEFAULT_CHUNK_SIZE)
                emit(AgentEvent.Result(reply.then))
            }

            is FakeReply.Failure -> emit(AgentEvent.Error(reply.message))
        }
    }

    private suspend fun FlowCollector<AgentEvent>.emitChunks(
        text: String,
        chunkSize: Int,
    ) {
        if (text.isEmpty()) return
        val size = chunkSize.coerceAtLeast(1)
        text.chunked(size).forEachIndexed { index, chunk ->
            if (index > 0 && chunkDelayMillis > 0) delay(chunkDelayMillis)
            emit(AgentEvent.Text(chunk))
        }
    }

    private companion object {
        const val DEFAULT_CHUNK_SIZE = 8

        /** 假工具的返回文案 —— 让 UI 的"过程折叠"在没有模型时也能看到完成态。 */
        const val FAKE_TOOL_RESULT = "（演示模式）工具执行完毕"
    }
}
