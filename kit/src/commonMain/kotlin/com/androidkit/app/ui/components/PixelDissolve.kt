package com.androidkit.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.theme.LocalReducedMotion
import com.androidkit.app.ui.theme.PixelMotion
import com.androidkit.app.ui.theme.quantize
import kotlin.random.Random

/**
 * 溶解覆盖顺序：单元格索引的确定性洗牌（同 seed 同序列，转场中途重组不跳变）。
 * 纯函数，测试用。
 */
internal fun dissolveOrder(cols: Int, rows: Int, seed: Int): IntArray {
    val list = (0 until cols * rows).toMutableList()
    list.shuffle(Random(seed))
    return list.toIntArray()
}

private enum class DissolveStage { Idle, Covering, Revealing }

/**
 * 像素溶解转场 —— target 变化时：旧内容被随机逐格覆盖消失（一半时长），
 * 内容交接后新内容逐格显形（另一半），总时长 [durationMs]（默认 400ms）。
 *
 * 覆盖色取 [coverColor]（默认 surface，即页面底色）。转场期间旧内容定格显示。
 * 减动效（LocalReducedMotion）时直接切内容，无溶解。
 *
 * 用法：`PixelDissolve(target = pageKey) { CurrentPage() }`
 * —— 想接 AnimatedContent 的场景自己包一层即可，这个容器自带遮罩节奏。
 */
@Composable
fun PixelDissolve(
    target: Any?,
    modifier: Modifier = Modifier,
    cellSize: Dp = 24.dp,
    durationMs: Int = 400,
    coverColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    var stage by remember { mutableStateOf(DissolveStage.Idle) }
    var activeKey by remember { mutableStateOf(target) }
    // 转场中展示旧内容定格帧；稳态才更新（SideEffect = 组合成功后写，不引发重组循环）
    var stale by remember { mutableStateOf<@Composable () -> Unit>({}) }
    if (stage == DissolveStage.Idle) SideEffect { stale = content }

    val mask = remember { Animatable(0f) }
    val reduced = LocalReducedMotion.current
    val spec = PixelMotion.stepped<Float>(durationMs / 2)
    LaunchedEffect(target) {
        if (target == activeKey) return@LaunchedEffect
        stage = DissolveStage.Covering
        if (reduced) mask.snapTo(1f) else mask.animateTo(1f, spec)
        activeKey = target
        stage = DissolveStage.Revealing
        if (reduced) mask.snapTo(0f) else mask.animateTo(0f, spec)
        stage = DissolveStage.Idle
    }

    BoxWithConstraints(modifier) {
        if (stage == DissolveStage.Covering) stale() else content()
        if (stage == DissolveStage.Idle) return@BoxWithConstraints

        val cols = (maxWidth / cellSize).toInt().coerceAtLeast(1)
        val rows = (maxHeight / cellSize).toInt().coerceAtLeast(1)
        val order = remember(cols, rows, activeKey) { dissolveOrder(cols, rows, activeKey.hashCode()) }
        val cellW = maxWidth / cols
        val cellH = maxHeight / rows
        val frames = (durationMs / 2 + PixelMotion.FRAME_MS - 1) / PixelMotion.FRAME_MS

        Canvas(Modifier.matchParentSize().clipToBounds()) {
            val n = quantize(mask.value, frames)
            val count = (n * order.size).toInt()
            for (i in 0 until count) {
                val idx = order[i]
                drawRect(
                    color = coverColor,
                    topLeft = Offset((idx % cols) * cellW.toPx(), (idx / cols) * cellH.toPx()),
                    size = Size(cellW.toPx(), cellH.toPx())
                )
            }
        }
    }
}
