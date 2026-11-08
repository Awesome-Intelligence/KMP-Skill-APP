package com.androidkit.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androidkit.app.res.Res
import com.androidkit.app.res.bitmap_a11y_number_value
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.PixelMotion
import com.androidkit.app.ui.theme.ThemeStyle
import com.androidkit.app.ui.theme.quantize
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

/**
 * 5×7 点阵字形：每字符 7 行、每行 5 bit（bit4 = 最左列）。只收数字与 '-''.'，
 * 正文文字走像素字体（ThemeStyle.Bitmap 的 Typography），不在这里画。
 */
internal val DOT_GLYPHS: Map<Char, IntArray> = mapOf(
    '0' to intArrayOf(14, 17, 17, 17, 17, 17, 14),
    '1' to intArrayOf(4, 12, 4, 4, 4, 4, 14),
    '2' to intArrayOf(14, 17, 1, 2, 4, 8, 31),
    '3' to intArrayOf(14, 17, 1, 6, 1, 17, 14),
    '4' to intArrayOf(2, 6, 10, 18, 31, 2, 2),
    '5' to intArrayOf(31, 16, 30, 1, 1, 17, 14),
    '6' to intArrayOf(6, 8, 16, 30, 17, 17, 14),
    '7' to intArrayOf(31, 1, 2, 4, 8, 8, 8),
    '8' to intArrayOf(14, 17, 17, 14, 17, 17, 14),
    '9' to intArrayOf(14, 17, 17, 15, 1, 2, 12),
    '-' to intArrayOf(0, 0, 0, 31, 0, 0, 0),
    '.' to intArrayOf(0, 0, 0, 0, 0, 6, 6),
)

/** 字符 → 字形（未收录返回 null，绘制时留空占位） */
internal fun glyphOf(ch: Char): IntArray? = DOT_GLYPHS[ch]

/**
 * 单位翻滚进度：每个字符在自己的时间窗里滚 [rollSteps] 帧，
 * 个位（indexFromRight = 0）先滚先停，高位依次延后 1 帧起步 —— 数字从右往左逐位停稳。
 *
 * @param overall 0..1 总进度
 * @return 该字符 0..1 的量化翻滚进度
 */
internal fun staggeredRoll(overall: Float, indexFromRight: Int, digitCount: Int, rollSteps: Int): Float {
    if (rollSteps <= 0) return if (overall >= 1f) 1f else 0f
    val frames = overall * (rollSteps + digitCount - 1)
    val local = (frames - indexFromRight).coerceIn(0f, rollSteps.toFloat())
    return quantize(local / rollSteps, rollSteps)
}

private const val ROLL_STEPS = 3   // 单字符翻滚 3 帧

/**
 * 点阵数字 —— 数字更新时逐位上下翻滚，按帧跳动无平滑。
 *
 * 未收录字符（字母等）留空占位：这个组件只管数字，正文交给像素字体。
 * 减动效（LocalReducedMotion）时直接显示新值（PixelMotion.stepped 已退化）。
 *
 * @param value 要显示的字符串（只处理 0-9 '-' '.'）
 * @param dotSize 单个点阵的边长
 */
@Composable
fun DotMatrixNumber(
    value: String,
    modifier: Modifier = Modifier,
    dotSize: Dp = 4.dp,
    dotGap: Dp = 2.dp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    contentDescription: String? = null,
) {
    val pitch = dotSize + dotGap
    val width = pitch * (value.length * 6 - 1)   // 每字符 5 点宽 + 1 点间距
    val height = pitch * 7
    val desc = contentDescription ?: stringResource(Res.string.bitmap_a11y_number_value, value)

    // 定格值：翻滚完成后才交接成新值，动画期间它充当「滚出的旧值」
    val shown = remember { mutableStateOf(value) }
    val roll = remember { Animatable(1f) }
    val spec = PixelMotion.stepped<Float>(PixelMotion.FRAME_MS * (ROLL_STEPS + value.length))
    LaunchedEffect(value) {
        if (shown.value == value) {
            roll.snapTo(1f)
        } else {
            roll.snapTo(0f)
            roll.animateTo(1f, spec)
            shown.value = value
        }
    }

    Canvas(
        modifier = modifier
            // 固有尺寸：点阵不被外层压缩/拉伸（调用方用 wrapper 控制占位）
            .requiredSize(width = width, height = height)
            .semantics { this.contentDescription = desc }
    ) {
        drawDotText(shown.value, value, roll.value, dotSize.toPx(), pitch.toPx(), color)
    }
}

private fun DrawScope.drawDotText(old: String, new: String, overall: Float, dot: Float, step: Float, color: Color) {
    val digitCount = new.length
    for (i in 0 until digitCount) {
        val oldCh = old.getOrNull(i)
        val newCh = new.getOrNull(i)
        if (oldCh == newCh) {
            drawGlyph(newCh, i * step * 6, 0f, dot, step, color)
            continue
        }
        val p = staggeredRoll(overall, digitCount - 1 - i, digitCount, ROLL_STEPS)
        val shift = p * step * 7   // 一个字形高度的位移
        drawGlyph(oldCh, i * step * 6, -shift, dot, step, color)
        drawGlyph(newCh, i * step * 6, step * 7 - shift, dot, step, color)
    }
}

private fun DrawScope.drawGlyph(ch: Char?, x: Float, y: Float, dot: Float, step: Float, color: Color) {
    val glyph = ch?.let(::glyphOf) ?: return
    for (row in 0 until 7) {
        val bits = glyph[row]
        for (col in 0 until 5) {
            if (bits and (1 shl (4 - col)) != 0) {
                drawRect(
                    color = color,
                    topLeft = Offset(x + col * step, y + row * step),
                    size = Size(dot, dot)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DotMatrixNumberPreview() {
    AppTheme(style = ThemeStyle.Bitmap) {
        DotMatrixNumber(value = "128")
    }
}
