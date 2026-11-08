package com.androidkit.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androidkit.app.res.Res
import com.androidkit.app.res.progress_format
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.ThemeStyle
import com.androidkit.app.ui.theme.extendedColors
import com.androidkit.app.ui.theme.quantize
import com.androidkit.app.ui.theme.steppedProgressState
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.floor

/** 完成对勾的 5×7 点阵 */
internal val CHECK_GLYPH: IntArray = intArrayOf(0, 0, 1, 3, 6, 12, 24)

/**
 * 按帧加载进度条 —— 拆成 [cells] 格逐格点亮，百分比同步跳变，完成显示像素对勾。
 *
 * 格子按帧推进（每格一帧），不是连续填充；减动效时直接跳到终态。
 */
@Composable
fun PixelProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    cells: Int = 20,
    barHeight: Dp = 12.dp,
    cellGap: Dp = 2.dp,
    litColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    doneColor: Color = MaterialTheme.extendedColors.success,
    showLabel: Boolean = true,
    contentDescription: String? = null,
) {
    val target = progress.coerceIn(0f, 1f)
    // 进度按帧走：每格一帧，最多 cells 帧
    val stepped = steppedProgressState(target = target, durationMs = cells * 60)
    val lit = floor(quantize(stepped.value, cells) * cells).toInt().coerceIn(0, cells)
    val percent = lit * 100 / cells
    val done = lit >= cells
    val desc = contentDescription ?: stringResource(Res.string.progress_format, percent)

    Row(
        modifier = modifier.semantics { this.contentDescription = desc },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(cellGap)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(barHeight),
            horizontalArrangement = Arrangement.spacedBy(cellGap)
        ) {
            repeat(cells) { i ->
                Canvas(
                    Modifier
                        .weight(1f)
                        .height(barHeight)
                ) {
                    drawRect(
                        color = if (i < lit) litColor else trackColor,
                        size = Size(size.width, size.height)
                    )
                }
            }
        }
        if (showLabel) {
            if (done) {
                PixelCheck(color = doneColor, modifier = Modifier.size(width = 10.dp, height = 14.dp))
            } else {
                androidx.compose.material3.Text(
                    text = stringResource(Res.string.progress_format, percent),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** 像素对勾 —— 5×7 点阵画的完成标记 */
@Composable
fun PixelCheck(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.extendedColors.success,
) {
    Canvas(modifier) {
        val cell = minOf(size.width / 5f, size.height / 7f)
        val ox = (size.width - cell * 5) / 2f
        val oy = (size.height - cell * 7) / 2f
        for (row in 0 until 7) {
            val bits = CHECK_GLYPH[row]
            for (col in 0 until 5) {
                if (bits and (1 shl (4 - col)) != 0) {
                    drawRect(
                        color = color,
                        topLeft = Offset(ox + col * cell, oy + row * cell),
                        size = Size(cell, cell)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PixelProgressBarPreview() {
    AppTheme(style = ThemeStyle.Bitmap) {
        PixelProgressBar(progress = 0.6f)
    }
}
