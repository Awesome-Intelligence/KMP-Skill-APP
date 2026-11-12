package com.androidkit.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween

/**
 * 电子纸风格的动效规格 —— 180ms ease-out（cubic-bezier(0.2, 0, 0, 1)）。
 *
 * 规范给的区间是 120–220ms；这里取中位。规矩：
 * - 只改颜色 / 边框色，不位移、不缩放、不加阴影
 * - 不用闪烁、循环刷新式动效、不用大范围渐变（电子纸刷新感 = 不稳定感）
 * - 减动效时时长归零，状态本身还在 —— 只是不过渡
 */
object EInkMotion {
    const val DURATION_MS = 180
    val EASING = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    fun <T> spec(reducedMotion: Boolean): TweenSpec<T> =
        tween(durationMillis = if (reducedMotion) 0 else DURATION_MS, easing = EASING)
}
