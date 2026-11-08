package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.res.Res
import com.androidkit.app.res.bitmap_a11y_typewriter
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.LocalReducedMotion
import com.androidkit.app.ui.theme.PixelMotion
import com.androidkit.app.ui.theme.ThemeStyle
import com.androidkit.app.ui.theme.extendedColors
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

/**
 * 逐字光标 —— 橙色方块光标先闪 [blinkCount] 次，再逐字吐出文本；
 * 数字高亮（强调色），输出完成光标消失。
 *
 * 文本中途追加（AI 流式输出）不会重新闪光标：光标只在第一次出现时闪，
 * 之后一直跟着已输出的最后一个字走。
 *
 * 光标位置用 onTextLayout 报的 cursorRect 定位（这版 Compose 没有 inline content API），
 * 所以方块永远贴着最后一个字，不受换行影响。
 *
 * @param msPerChar 每个字间隔（默认一帧 60ms）
 */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = Color.Unspecified,
    cursorColor: Color = MaterialTheme.extendedColors.star,
    msPerChar: Int = PixelMotion.FRAME_MS,
    blinkCount: Int = 2,
    contentDescription: String? = null,
) {
    val desc = contentDescription ?: stringResource(Res.string.bitmap_a11y_typewriter, text)

    if (LocalReducedMotion.current) {
        // 减动效：整段直出，无光标
        androidx.compose.material3.Text(
            text = text,
            modifier = modifier.semantics { this.contentDescription = desc },
            style = style,
            color = color,
        )
        return
    }

    val latest by rememberUpdatedState(text)
    var revealed by remember { mutableIntStateOf(0) }
    var cursorOn by remember { mutableStateOf(false) }
    // 光标位置（相对 Text 左上角）；null = 不画
    var cursorOffset by remember { mutableStateOf<Offset?>(null) }
    var cursorSize by remember { mutableStateOf(Size.Zero) }

    LaunchedEffect(Unit) {
        // 先闪 blinkCount 次（方块按帧跳，不用渐变）
        repeat(blinkCount * 2) {
            cursorOn = it % 2 == 0
            delay((PixelMotion.FRAME_MS * 3).toLong())
        }
        cursorOn = true
        // 逐字输出；文本追加时接着往下吐
        while (true) {
            val t = latest
            if (revealed < t.length) {
                cursorOn = true
                revealed += 1
                delay(msPerChar.toLong())
            } else if (t.isNotEmpty()) {
                // 输出完成：光标消失（后续再有新文本再亮回来）
                cursorOn = false
                delay(PixelMotion.FRAME_MS.toLong())
            } else {
                cursorOn = true
                delay(PixelMotion.FRAME_MS.toLong())
            }
        }
    }

    val shown = latest.take(revealed)
    val annotated = buildAnnotatedString {
        appendHighlightedDigits(shown, cursorColor)
    }

    Box(modifier.semantics { this.contentDescription = desc }) {
        androidx.compose.material3.Text(
            text = annotated,
            style = style,
            color = color,
            onTextLayout = { result ->
                if (cursorOn) {
                    val rect = result.getCursorRect(shown.length)
                    cursorOffset = Offset(rect.left, rect.top)
                    cursorSize = Size(rect.width, rect.height)
                } else {
                    cursorOffset = null
                }
            }
        )
        val off = cursorOffset
        if (off != null && cursorSize.width > 0f) {
            val density = LocalDensity.current
            Box(
                Modifier
                    .offset { IntOffset(off.x.toInt(), off.y.toInt()) }
                    .size(
                        width = with(density) { cursorSize.width.toDp() },
                        height = with(density) { cursorSize.height.toDp() }
                    )
                    .background(cursorColor)
            )
        }
    }
}

/**
 * 数字高亮：把数字串染成强调色，其余原样。纯函数，测试用。
 */
internal fun AnnotatedString.Builder.appendHighlightedDigits(text: String, highlight: Color) {
    var i = 0
    while (i < text.length) {
        if (text[i].isDigit()) {
            var j = i
            while (j < text.length && text[j].isDigit()) j++
            pushStyle(SpanStyle(color = highlight, fontWeight = FontWeight.Normal))
            append(text.substring(i, j))
            pop()
            i = j
        } else {
            append(text[i])
            i++
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TypewriterTextPreview() {
    AppTheme(style = ThemeStyle.Bitmap) {
        TypewriterText(text = "待办已添加 3 项，共 128 条")
    }
}
