package com.androidkit.app.ui.agent

/**
 * 一条聊天消息。
 *
 * 为什么要分类型：用户的话、助手的回复、系统的提示、出错 —— 四种长得不一样
 * （气泡颜色、对齐、能不能折叠都不一样）。早先一个 `Msg(fromUser, text)` 只够"把字显示出来"，
 * 过程（工具调用）就没地方挂 —— 那一层是 agent 界面真正需要的东西。
 */
sealed class ChatMessage {

    /** 用户说的话。 */
    data class User(val text: String) : ChatMessage()

    /**
     * 助手的回复。
     *
     * @property text 正文（流式增量直接追加到尾巴上，就是打字机效果）。
     * @property steps 这一轮里做过的工具调用 —— 折叠成一行，展开才看得到细节。
     */
    data class Assistant(
        val text: String = "",
        val steps: List<ChatStep> = emptyList(),
    ) : ChatMessage()

    /** 系统提示：模式切换、这一轮没有结果之类的中性信息。 */
    data class System(val text: String) : ChatMessage()

    /** 出错。 */
    data class Error(val text: String) : ChatMessage()
}

/** 一次工具调用 —— 折叠条上的一行。 */
data class ChatStep(
    /** 工具名（snake_case，和模型看到的名字一致）。 */
    val tool: String,
    /** 入参摘要（JSON 字符串）。 */
    val args: String = "",
    /** 跑到哪一步了。 */
    val status: ChatStepStatus = ChatStepStatus.Running,
    /** 工具返回的文案（跑完才有）。 */
    val result: String? = null,
)

/** 工具调用的状态。 */
enum class ChatStepStatus {
    /** 模型要调这个工具，正在等结果（也可能正在等用户点头）。 */
    Running,

    /** 跑完了。 */
    Done,

    /** 没跑成。 */
    Failed,
}
