package com.androidkit.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween

/**
 * Notion 风格的动效规格 —— 150ms ease-out（cubic-bezier(0.2, 0, 0, 1)），**只改颜色**。
 *
 * 「Ultimate Restraint」：不位移、不缩放、不加阴影跳变 —— 文档工具里任何浮动感
 * 都会破坏阅读稳定性。交互反馈只有底色一档变化（`#f7f6f3` → `#efedea` → `#e3e1db`）。
 *
 * 减动效时时长归零，状态色本身还在 —— 只是不过渡。
 */
object NotionMotion {
    const val DURATION_MS = 150
    val EASING = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    fun <T> spec(reducedMotion: Boolean): TweenSpec<T> =
        tween(durationMillis = if (reducedMotion) 0 else DURATION_MS, easing = EASING)
}
