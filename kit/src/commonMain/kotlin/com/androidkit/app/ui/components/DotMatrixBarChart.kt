package com.androidkit.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androidkit.app.res.Res
import com.androidkit.app.res.bitmap_a11y_chart
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.PixelMotion
import com.androidkit.app.ui.theme.ThemeStyle
import com.androidkit.app.ui.theme.extendedColors
import com.androidkit.app.ui.theme.quantize
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

/**
 * 值列 → 各列点阵高度（1 行 = 1 个点）。非零值至少 1 个点，封顶 [maxRows]。
 * 纯函数，测试用。
 */
internal fun normalizeColumns(values: List<Int>, maxRows: Int): IntArray {
    if (values.isEmpty()) return IntArray(0)
    val max = maxOf(values.max(), 1)
    return IntArray(values.size) { i ->
        if (values[i] <= 0) 0
        else ((values[i].toDouble() / max * maxRows).roundToInt()).coerceIn(1, maxRows)
    }
}

/**
 * 点阵柱状图 —— 小圆点自下而上、从左到右逐点点亮，最新亮起的点用强调色（橙），
 * 其余已点亮用前景色，未点亮画暗点（保留点阵网格感）。按帧跳动，无平滑过渡。
 *
 * @param values 每列的值（非负；≤0 的列不点亮）
 * @param maxRows 纵向最多多少个点
 * @param dotSize 点直径；columnGap 列间距（行间距 = dotGap）
 */
@Composable
fun DotMatrixBarChart(
    values: List<Int>,
    modifier: Modifier = Modifier,
    maxRows: Int = 8,
    dotSize: Dp = 4.dp,
    dotGap: Dp = 2.dp,
    columnGap: Dp = 8.dp,
    litColor: Color = MaterialTheme.colorScheme.onSurface,
    accentColor: Color = MaterialTheme.extendedColors.star,
    dimColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentDescription: String? = null,
) {
    val heights = remember(values, maxRows) { normalizeColumns(values, maxRows) }
    val total = heights.sum()
    val desc = contentDescription ?: stringResource(Res.string.bitmap_a11y_chart)

    // 逐点点亮：总时长 = 总点数 × 一帧，值变化时从头重放
    val reveal = remember { Animatable(1f) }
    val spec = PixelMotion.stepped<Float>(PixelMotion.FRAME_MS * total.coerceAtLeast(1))
    LaunchedEffect(heights) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, spec)
    }

    val pitchX = dotSize + columnGap
    val pitchY = dotSize + dotGap
    val width = if (heights.isEmpty()) 0.dp else pitchX * heights.size - columnGap
    val height = pitchY * maxRows - dotGap

    Canvas(
        modifier = modifier
            .requiredSize(width = width, height = height)
            .semantics { this.contentDescription = desc }
    ) {
        drawChart(heights, maxRows, total, reveal.value, dotSize.toPx(), pitchX.toPx(), pitchY.toPx(), litColor, accentColor, dimColor)
    }
}

private fun DrawScope.drawChart(
    heights: IntArray,
    maxRows: Int,
    total: Int,
    overall: Float,
    dot: Float,
    pitchX: Float,
    pitchY: Float,
    litColor: Color,
    accentColor: Color,
    dimColor: Color,
) {
    if (total == 0) return
    // 量化后已点亮的点数（点 0..total-1，点亮顺序 = 列主序、列内自下而上）
    val litCount = (quantize(overall, total) * total).toInt()
    var index = 0
    for ((col, h) in heights.withIndex()) {
        for (rowFromBottom in 0 until h) {
            val lit = index < litCount
            val isFrontier = index == litCount - 1
            val color = when {
                isFrontier -> accentColor
                lit -> litColor
                else -> dimColor
            }
            val x = col * pitchX
            val y = (maxRows - 1 - rowFromBottom) * pitchY
            drawRect(color, topLeft = Offset(x, y), size = Size(dot, dot))
            index++
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DotMatrixBarChartPreview() {
    AppTheme(style = ThemeStyle.Bitmap) {
        DotMatrixBarChart(values = listOf(3, 7, 4, 9, 5, 8, 2, 6))
    }
}
