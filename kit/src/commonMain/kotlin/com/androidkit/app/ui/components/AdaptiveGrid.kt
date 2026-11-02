package com.androidkit.app.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** M3 断点：中等宽度窗口的下界（dp）。 */
private const val MEDIUM_WIDTH_DP = 600

/**
 * 画廊网格列数：每格不小于 [minSize]，且列数至少 [minColumns]。
 *
 * 为什么不用 GridCells.Adaptive：它只保证"每格不小于 minSize"，
 * 列数 = (可用宽 + 间距) / (minSize + 间距) 向下取整，
 * 窗口一窄（分屏/自由窗口）就掉回 1 列。
 * 这里在 Adaptive 的算法上加一个下限，保证至少 [minColumns] 列。
 *
 * availableSize 由 LazyVerticalGrid 传入，已经扣掉 contentPadding 的水平边距。
 */
class AdaptiveGridCells(
    private val minSize: Dp = 132.dp,
    private val minColumns: Int = 2,
) : GridCells {

    init {
        require(minSize > 0.dp) { "minSize must be positive" }
        require(minColumns > 0) { "minColumns must be positive" }
    }

    override fun Density.calculateCrossAxisCellSizes(
        availableSize: Int,
        spacing: Int,
    ): List<Int> {
        val count = maxOf(
            minColumns,
            (availableSize + spacing) / (minSize.roundToPx() + spacing)
        )
        // 剩余像素分给前几格，保持各格宽度一致
        val withoutSpacing = availableSize - spacing * (count - 1)
        val slot = withoutSpacing / count
        val remainder = withoutSpacing % count
        return List(count) { slot + if (it < remainder) 1 else 0 }
    }

    override fun equals(other: Any?): Boolean =
        other is AdaptiveGridCells && other.minSize == minSize && other.minColumns == minColumns

    override fun hashCode(): Int = 31 * minSize.hashCode() + minColumns
}

/**
 * 网格的边距（margin）与卡间距（gutter）。
 *
 * M3 规范按窗口宽度分档：紧凑档 margin 16dp，中等及以上 margin 24dp。
 * gutter 取 12–16dp —— 比 M3 的 8/16dp 略宽，跟 .claude/GOOGLE_STYLE.md
 * 的"卡片间距 12–16dp"一致（项目规范优先于 M3）。
 */
@Immutable
data class GridSpacing(val margin: Dp, val gutter: Dp)

/**
 * 按窗口宽度（dp）选边距。纯函数，方便单测。
 */
fun gridSpacingFor(widthDp: Int): GridSpacing =
    if (widthDp >= MEDIUM_WIDTH_DP) {
        GridSpacing(margin = 24.dp, gutter = 16.dp)
    } else {
        GridSpacing(margin = 16.dp, gutter = 12.dp)
    }

/**
 * 按当前窗口宽度取网格边距：窗口变宽（大屏/平板/分屏）留白自动放大。
 *
 * ponytail: 原来用 material3-adaptive 的窗口尺寸类 —— 那是 androidx 专属，Compose Multiplatform
 * 没有对应物。改成 BoxWithConstraints 自算宽度，三端通用，断点仍然只定义在一处。
 */
@Composable
fun rememberGridSpacing(): GridSpacing {
    val widthDp = rememberWindowWidthDp()
    return remember(widthDp) { gridSpacingFor(widthDp) }
}

@Composable
private fun rememberWindowWidthDp(): Int {
    var widthDp by remember { mutableIntStateOf(0) }
    BoxWithConstraints {
        widthDp = maxWidth.value.toInt()
    }
    return widthDp
}
