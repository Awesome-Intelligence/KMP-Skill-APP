package com.androidkit.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween

/**
 * 瑞士风格的动效规格 —— 150ms ease-out（cubic-bezier(0.2, 0, 0, 1)）。
 *
 * 交互只允许改颜色/边框色：不位移、不缩放、不加阴影（Rational Restraint）。
 * 减动效时时长归零，状态色本身还在 —— 只是不过渡。
 */
object SwissMotion {
    const val DURATION_MS = 150
    val EASING = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    fun <T> spec(reducedMotion: Boolean): TweenSpec<T> =
        tween(durationMillis = if (reducedMotion) 0 else DURATION_MS, easing = EASING)
}
