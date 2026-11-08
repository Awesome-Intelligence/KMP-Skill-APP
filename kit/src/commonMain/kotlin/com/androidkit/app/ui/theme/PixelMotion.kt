package com.androidkit.app.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/**
 * 位图主题动效 — 按帧跳变，零平滑过渡。
 *
 * 与 [M3Motion]（spring/emphasized）相对：位图风格里所有动画都是「帧数制」——
 * 进度按 [FRAME_MS] 一格一格跳，easing 一律线性。组件拿进度后必须过 [quantize]
 * 再画，中间值不允许出现在画面上。
 */
object PixelMotion {

    /** 一帧 ≈ 60ms（16fps 的复古跳帧感） */
    const val FRAME_MS = 60

    /**
     * 帧制动画 spec：线性 tween，时长向上取整到整帧数。
     * 减动效（[LocalReducedMotion]）时退化为 snap（直接终态）。
     */
    @Composable
    fun <T> stepped(durationMs: Int): FiniteAnimationSpec<T> {
        val frames = (durationMs + FRAME_MS - 1) / FRAME_MS
        return if (LocalReducedMotion.current || frames <= 0) snap()
        else tween(frames * FRAME_MS, easing = LinearEasing)
    }
}

/**
 * 把 0..1 进度量化成 [steps] 档（0.0, 1/steps, …, 1.0）。
 * 位图组件在绘制前必须过这一刀 —— 中间值出现在画面上就破功了。
 */
fun quantize(value: Float, steps: Int): Float {
    if (steps <= 0) return if (value >= 1f) 1f else 0f
    val clamped = value.coerceIn(0f, 1f)
    return ((clamped * steps).toInt().coerceAtMost(steps)) / steps.toFloat()
}

/**
 * 帧制进度：目标变化时 0→1 线性走 [durationMs]，读出来是量化后的档位。
 * 减动效时直接终态。
 */
@Composable
fun steppedProgressState(target: Float, durationMs: Int): State<Float> =
    animateFloatAsState(
        targetValue = target,
        animationSpec = PixelMotion.stepped(durationMs),
        label = "pixel_stepped_progress"
    )
