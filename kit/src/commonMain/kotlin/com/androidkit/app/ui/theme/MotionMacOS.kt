package com.androidkit.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween

/**
 * macOS 毛玻璃的动效规格 —— 200ms ease-out（cubic-bezier(0.2, 0, 0, 1)），**只改颜色**。
 *
 * 规范原话：「Motion may only change color, over 200ms ease-out;
 * any translation or scale reminds the eye this is a web page.」
 *
 * 所以这个主题里唯一的动效是底色/边框色的过渡（悬停白 8%、选中蓝 18%）。
 * 减动效时时长归零 —— 状态色本身还在，只是不过渡。
 */
object MacOSMotion {
    const val DURATION_MS = 200
    val EASING = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    fun <T> spec(reducedMotion: Boolean): TweenSpec<T> =
        tween(durationMillis = if (reducedMotion) 0 else DURATION_MS, easing = EASING)
}
