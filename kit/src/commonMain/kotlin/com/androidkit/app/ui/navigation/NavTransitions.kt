package com.androidkit.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import com.androidkit.app.ui.theme.M3Motion

/**
 * 页面级 M3 转场 —— 和 [M3Motion]（组件级令牌）配套的第二层。
 *
 * 分工按 M3 的四种转场模式：
 * - 同层级、无空间关系（底部导航 tab、设置页进出）→ **Fade Through**
 * - 层级推进 / 返回（列表 → 详情）→ **Shared Axis X**，位移 30dp，不是整屏
 *
 * 六个函数都**不是** @Composable：navigation 库的 `enterTransition` /
 * `popEnterTransition` lambda 本身不是 Composable，读不到 `LocalReducedMotion`，
 * 所以减动效由调用侧以 [reducedMotion] 传入（Compose 里就是 `LocalReducedMotion.current`）。
 * 纯 Compose 的整页替换直接用 `M3Motion.fadeThrough()`，不必走这里。
 *
 * 目前的 app 用 `AnimatedContent` 切页（没有 navigation 依赖），所以只用到
 * [M3Motion.fadeThrough] + [sharedAxisOffsetPx]。将来接 navigation-compose 时：
 *
 * ```kotlin
 * NavHost(
 *     navController = navController,
 *     enterTransition = { if (reducedMotion) EnterTransition.None else sharedAxisIn(axisPx) },
 *     exitTransition = { if (reducedMotion) ExitTransition.None else sharedAxisOut(axisPx) },
 *     popEnterTransition = { if (reducedMotion) EnterTransition.None else sharedAxisPopIn(axisPx) },
 *     popExitTransition = { if (reducedMotion) ExitTransition.None else sharedAxisPopOut(axisPx) },
 * ) {
 *     // 同层级目的地单独覆盖成 Fade Through
 *     composable(
 *         route = "home",
 *         enterTransition = { if (reducedMotion) EnterTransition.None else fadeThroughIn() },
 *         exitTransition = { if (reducedMotion) ExitTransition.None else fadeThroughOut() },
 *         popEnterTransition = { if (reducedMotion) EnterTransition.None else fadeThroughIn() },
 *         popExitTransition = { if (reducedMotion) ExitTransition.None else fadeThroughOut() },
 *     ) { HomeScreen() }
 * }
 * ```
 */

/**
 * Shared Axis X 的位移像素：M3 规范 30dp，按当前密度换算。
 *
 * nav 层的转场 lambda 不是 @Composable，也要不到 Density —— 只能在这里先换成 px，
 * 再由调用侧闭包捕获。
 */
@Composable
fun sharedAxisOffsetPx(): Int =
    with(LocalDensity.current) { M3Motion.SHARED_AXIS_OFFSET.roundToPx() }

/** Fade Through 进场：90ms 淡出的同时，新内容从 92% 放大 + 淡入 */
fun fadeThroughIn(): EnterTransition = fadeIn(
    tween(M3Motion.FADE_THROUGH_IN, delayMillis = M3Motion.EXIT_FADE, easing = M3Motion.emphasizedDecelerate)
) + scaleIn(
    initialScale = M3Motion.FADE_THROUGH_INITIAL_SCALE,
    animationSpec = tween(
        M3Motion.FADE_THROUGH_IN,
        delayMillis = M3Motion.EXIT_FADE,
        easing = M3Motion.emphasizedDecelerate
    )
)

/** Fade Through 退场：只淡出，比进场快，注意力留在新内容上 */
fun fadeThroughOut(): ExitTransition = fadeOut(
    tween(M3Motion.EXIT_FADE, easing = M3Motion.emphasizedAccelerate)
)

/** 前进：新页面从右滑入 [offset] px */
fun sharedAxisIn(offset: Int): EnterTransition = slideInHorizontally(
    animationSpec = tween(M3Motion.MEDIUM2, easing = M3Motion.emphasizedDecelerate),
    initialOffsetX = { offset }
) + fadeIn(tween(M3Motion.MEDIUM2, easing = M3Motion.emphasizedDecelerate))

/** 前进：旧页面左移淡出 */
fun sharedAxisOut(offset: Int): ExitTransition = slideOutHorizontally(
    animationSpec = tween(M3Motion.MEDIUM2, easing = M3Motion.emphasizedAccelerate),
    targetOffsetX = { -offset }
) + fadeOut(tween(M3Motion.EXIT_FADE, easing = M3Motion.emphasizedAccelerate))

/** 返回：被退回的页面从左滑回 */
fun sharedAxisPopIn(offset: Int): EnterTransition = slideInHorizontally(
    animationSpec = tween(M3Motion.MEDIUM2, easing = M3Motion.emphasizedDecelerate),
    initialOffsetX = { -offset }
) + fadeIn(tween(M3Motion.MEDIUM2, easing = M3Motion.emphasizedDecelerate))

/** 返回：当前页面右移淡出 */
fun sharedAxisPopOut(offset: Int): ExitTransition = slideOutHorizontally(
    animationSpec = tween(M3Motion.MEDIUM2, easing = M3Motion.emphasizedAccelerate),
    targetOffsetX = { offset }
) + fadeOut(tween(M3Motion.EXIT_FADE, easing = M3Motion.emphasizedAccelerate))
