package com.androidkit.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.hoverable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.theme.PixelMotion
import com.androidkit.app.ui.theme.quantize

/**
 * 4×4 Bayer 有序抖动矩阵（值 0..15）。阈值越小越先被点亮。
 * 纯函数，测试用。
 */
internal val BAYER_4: Array<IntArray> = arrayOf(
    intArrayOf(0, 8, 2, 10),
    intArrayOf(12, 4, 14, 6),
    intArrayOf(3, 11, 1, 9),
    intArrayOf(15, 7, 13, 5)
)

/**
 * 抖动网点判定：[level] ∈ 0..4，密度依次 0% / 25% / 50% / 75% / 100%。
 * 纯函数，测试用。
 */
internal fun bayerOn(x: Int, y: Int, level: Int): Boolean =
    level > 0 && BAYER_4[y and 3][x and 3] < level * 4

/**
 * 抖动悬停 —— 悬停时用四档渐变网点盖在内容上代替透明度渐变，移开再逐档退回。
 *
 * 悬停事件只有桌面/网页有；触摸端不触发，等于普通内容（不报错、不空转）。
 *
 * @param cell 网点格子边长（越小网点越细）
 * @param steps 档位数（4 = 四档）
 */
@Composable
fun Modifier.pixelDitherHover(
    color: Color = MaterialTheme.colorScheme.primary,
    cell: Dp = 2.dp,
    steps: Int = 4,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val level = remember { Animatable(0f) }
    val spec = PixelMotion.stepped<Float>(PixelMotion.FRAME_MS * steps)
    LaunchedEffect(hovered) {
        level.animateTo(if (hovered) steps.toFloat() else 0f, spec)
    }
    val lv = quantize(level.value / steps, steps) * steps

    return this
        .hoverable(interaction)
        .drawWithContent {
            drawContent()
            if (lv > 0f) {
                val c = cell.toPx()
                val cols = (size.width / c).toInt() + 1
                val rows = (size.height / c).toInt() + 1
                for (y in 0 until rows) {
                    for (x in 0 until cols) {
                        if (bayerOn(x, y, lv.toInt())) {
                            drawRect(color, topLeft = Offset(x * c, y * c), size = Size(c, c))
                        }
                    }
                }
            }
        }
}

/**
 * 点阵网格背景 —— 冷灰白底上的淡点阵纹理。
 * 挂到页面根容器（Column/Box）上即可。
 *
 * @param spacing 网格间距（点落在格点上）
 */
@Composable
fun Modifier.pixelDotGrid(
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    spacing: Dp = 16.dp,
    dot: Dp = 1.5.dp,
): Modifier = drawBehind {
    val s = spacing.toPx()
    if (s <= 0f) return@drawBehind
    val r = dot.toPx() / 2f
    var y = s / 2f
    while (y < size.height) {
        var x = s / 2f
        while (x < size.width) {
            drawCircle(color, radius = r, center = Offset(x, y))
            x += s
        }
        y += s
    }
}
