package com.androidkit.app.data.agent

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * 工具返回值。
 *
 * 文本是常态（"删了一条待办"），但工具跑在手机上，**也能把图交回给模型看** ——
 * 截图、图表、渲染结果都算。Anthropic 的 tool_result 支持 image 块，Koog 这条路是通的
 * （见 [SkillTool.encodeResultToParts]）。
 *
 * @property text 给模型看的文字，也是"结果里包含某片段"这类断言的依据。
 */
@Serializable
sealed class ToolOutput {

    /** 给模型看的文字。 */
    abstract val text: String

    /** 纯文本。 */
    @Serializable
    data class Text(override val text: String) : ToolOutput()

    /** 文本 + 一张图（图会作为 image 块一起发给模型）。 */
    @Serializable
    data class TextWithImage(
        override val text: String,
        @Transient val image: ToolImage = ToolImage(),
    ) : ToolOutput()
}

/**
 * 一张图 —— 给模型看的视觉输入。
 *
 * [bytes] 不进 JSON（`@Transient`）：一张截图 base64 能有几百 KB，
 * 而 JSON 那份只是给事件/日志用的；图的本体走 [SkillTool.encodeResultToParts]。
 */
@Serializable
data class ToolImage(
    val format: String = "png",
    val mimeType: String = "image/$format",
    @Transient val bytes: ByteArray = ByteArray(0),
) {
    // data class 对数组用引用相等 —— 两张一样的截图比较会永远不相等。自己写。
    override fun equals(other: Any?): Boolean =
        other is ToolImage &&
            format == other.format &&
            mimeType == other.mimeType &&
            bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = format.hashCode() * 31 + mimeType.hashCode()
}
