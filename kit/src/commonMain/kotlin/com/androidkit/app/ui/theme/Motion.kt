package com.androidkit.app.ui.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * Material 3 Motion Tokens
 * https://m3.material.io/styles/motion/easing-and-duration/spring-specs
 *
 * 用法：位置/尺寸变化走 spatial，颜色/透明度走 effects，时长取 duration 常量。
 * 需要别的类型（IntSize / Dp / Offset）时直接 `M3Motion.fastSpatial()`，返回类型自动匹配。
 *
 * 所有 spec / 转场都是 @Composable：系统关闭动画时（`LocalReducedMotion`）自动退化成
 * `snap()` / `None`，调用点无需判断。
 */
object M3Motion {

    // ── Duration tokens ──────────────────────────────────────────────
    const val SHORT1 = 50    // 选中/取消选中
    const val SHORT2 = 100   // 简单状态切换
    const val SHORT3 = 150   // 小组件出现
    const val SHORT4 = 200   // 标准交互
    const val MEDIUM1 = 250  // 面板展开
    const val MEDIUM2 = 300  // 标准转场
    const val MEDIUM3 = 350  // 复杂转场
    const val MEDIUM4 = 400  // 全屏转场（启动页）
    const val LONG1 = 450    // 强调动画
    const val LONG2 = 500    // 复杂强调

    /** 转场退场淡出：M3 规定 90ms —— 退场必须比位移快，注意力才留在新页面 */
    const val EXIT_FADE = 90

    /** Fade Through 进场：M3 规范 210ms（先等 90ms 退场，总 300ms） */
    const val FADE_THROUGH_IN = 210

    /** Fade Through 进场初始缩放：M3 规范 92% —— 不加缩放就只剩淡入，看不出「换了一层」 */
    const val FADE_THROUGH_INITIAL_SCALE = 0.92f

    /** Shared Axis X 位移量：M3 规范 30dp（不是整屏宽度） */
    val SHARED_AXIS_OFFSET = 30.dp

    // ── Easing ───────────────────────────────────────────────────────
    // 进场用 decelerate、退场用 accelerate；全项目统一走 emphasized 一族。
    val emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val emphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val emphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    // ── Spatial springs（位置/尺寸，允许轻微过冲）──────────────────
    @Composable
    fun <T> fastSpatial(): FiniteAnimationSpec<T> =
        if (LocalReducedMotion.current) snap()
        else spring(dampingRatio = 0.9f, stiffness = 1400f)

    @Composable
    fun <T> defaultSpatial(): FiniteAnimationSpec<T> =
        if (LocalReducedMotion.current) snap()
        else spring(dampingRatio = 0.9f, stiffness = 700f)

    /**
     * 庆祝 / 强调回弹 —— 唯一允许明显过冲的一档（damping 0.6 / stiffness 800）。
     * M3 standard spatial 是 damping 0.9，几乎不弹；一次性反馈需要一点回弹才「有回应」。
     * 只用于「收藏点亮、任务完成」这类**一次性**动作，
     * **不要**用于常态位移：元件互相压过会显得廉价，连续操作还会发飘。
     */
    @Composable
    fun <T> expressiveSpatial(): FiniteAnimationSpec<T> =
        if (LocalReducedMotion.current) snap()
        else spring(dampingRatio = 0.6f, stiffness = 800f)

    // ── Effects springs（颜色/透明度，无过冲）───────────────────────
    @Composable
    fun <T> fastEffects(): FiniteAnimationSpec<T> =
        if (LocalReducedMotion.current) snap()
        else spring(dampingRatio = 1f, stiffness = 3800f)

    @Composable
    fun <T> defaultEffects(): FiniteAnimationSpec<T> =
        if (LocalReducedMotion.current) snap()
        else spring(dampingRatio = 1f, stiffness = 1600f)

    // ── 转场 ───────────────────────────────────────────────────────
    /** 展开一块内容：撑开 + 淡入。位移走 MEDIUM1，淡入走 SHORT3 —— 内容先亮起再长开，和收起时对称 */
    @Composable
    fun expandSection(): EnterTransition =
        if (LocalReducedMotion.current) EnterTransition.None
        else expandVertically(
            animationSpec = tween(MEDIUM1, easing = emphasizedDecelerate)
        ) + fadeIn(tween(SHORT3, easing = emphasizedDecelerate))

    /** 收起一块内容：高度和透明度同步收缩，列表不会在动画中途上跳。退场比进场快 */
    @Composable
    fun collapseSection(): ExitTransition =
        if (LocalReducedMotion.current) ExitTransition.None
        else shrinkVertically(
            animationSpec = tween(MEDIUM1, easing = emphasizedAccelerate)
        ) + fadeOut(tween(SHORT2, easing = emphasizedAccelerate))

    /** 同位置替换（图标出现、状态切换） */
    @Composable
    fun appear(): EnterTransition =
        if (LocalReducedMotion.current) EnterTransition.None
        else fadeIn(tween(SHORT2, easing = emphasizedDecelerate))

    @Composable
    fun disappear(): ExitTransition =
        if (LocalReducedMotion.current) ExitTransition.None
        else fadeOut(tween(SHORT2, easing = emphasizedAccelerate))

    /**
     * Fade Through —— M3 用于「无关联内容整体替换」（切视图模式、切分组、切一级页面）。
     * 旧内容先淡出（90ms），新内容隔 90ms 再淡入（210ms）+ 从 92% 放大，两段不重叠：
     * 同时在场会看到两层叠影，比硬闪更糟。
     */
    @Composable
    fun fadeThrough(): ContentTransform {
        if (LocalReducedMotion.current) {
            return EnterTransition.None togetherWith ExitTransition.None
        }
        // macOS 毛玻璃规范：转场只改颜色，不缩放 —— 位移/缩放会提醒人「这是个网页」
        if (LocalThemeStyle.current == ThemeStyle.MacOS) {
            val fade = tween<Float>(MacOSMotion.DURATION_MS, easing = MacOSMotion.EASING)
            return fadeIn(fade) togetherWith fadeOut(fade)
        }
        val enter = fadeIn(
            tween(FADE_THROUGH_IN, delayMillis = EXIT_FADE, easing = emphasizedDecelerate)
        ) + scaleIn(
            initialScale = FADE_THROUGH_INITIAL_SCALE,
            animationSpec = tween(
                FADE_THROUGH_IN,
                delayMillis = EXIT_FADE,
                easing = emphasizedDecelerate
            )
        )
        return enter togetherWith fadeOut(tween(EXIT_FADE, easing = emphasizedAccelerate))
    }

    /**
     * 列表项位移：排序变化时项目滑到新位置。
     * 只走位，不带淡入淡出 —— 淡入淡出会在滚动进屏时也触发，看着像闪。
     * 参数取 GOOGLE_STYLE 的 snappy（damping 1.0 / stiffness 400）：不过冲，项不会互相压过。
     */
    @Composable
    fun itemPlacement(): FiniteAnimationSpec<IntOffset> =
        if (LocalReducedMotion.current) snap()
        else spring(dampingRatio = 1f, stiffness = 400f)

    /**
     * Pager 程序化翻页（点页签切换）。整屏内容在动，不允许过冲：
     * 用 M3 emphasized decelerate + MEDIUM2，与导航转场同一节奏。
     * 只用于相邻页；跨多页请直接 `scrollToPage`，别横穿中间页。
     */
    @Composable
    fun pagerScroll(): FiniteAnimationSpec<Float> =
        if (LocalReducedMotion.current) snap()
        else tween(MEDIUM2, easing = emphasizedDecelerate)

    // ── 浮层（菜单 / 消息面板）──────────────────────────────────────
    /**
     * 浮层进场时的初始缩放：容器从锚点长出来。
     * M3 菜单规范用 0.8 —— 起步足够小，"从按钮里长出来"才看得出来；
     * 0.9 几乎看不出来，只剩淡入。
     */
    const val FLOAT_INITIAL_SCALE = 0.8f

    /**
     * 浮层出现：容器从锚点 scale 0.8→1 + 淡入。
     * [transformOrigin] 由调用点给 —— 锚点在容器的哪个角，就从哪个角长出来。
     * 时长走 MEDIUM1（250ms，M3 菜单容器规范），淡入先走完（SHORT4），
     * 内容先亮起再长开，跟退场方向相反。
     * 用 tween + 强调减速，不过冲：浮层回弹会显得廉价。
     */
    @Composable
    fun floatEnter(transformOrigin: TransformOrigin): EnterTransition {
        if (LocalReducedMotion.current) return EnterTransition.None
        // macOS：只淡入，不缩放
        if (LocalThemeStyle.current == ThemeStyle.MacOS) {
            return fadeIn(tween(MacOSMotion.DURATION_MS, easing = MacOSMotion.EASING))
        }
        return scaleIn(
            initialScale = FLOAT_INITIAL_SCALE,
            animationSpec = tween(MEDIUM1, easing = emphasizedDecelerate),
            transformOrigin = transformOrigin
        ) + fadeIn(tween(SHORT4, easing = emphasizedDecelerate))
    }

    /** 浮层退场：缩回锚点 + 淡出，退场比进场快 */
    @Composable
    fun floatExit(transformOrigin: TransformOrigin): ExitTransition {
        if (LocalReducedMotion.current) return ExitTransition.None
        // macOS：只淡出，不缩放
        if (LocalThemeStyle.current == ThemeStyle.MacOS) {
            return fadeOut(tween(MacOSMotion.DURATION_MS, easing = MacOSMotion.EASING))
        }
        return scaleOut(
            targetScale = FLOAT_INITIAL_SCALE,
            animationSpec = tween(SHORT2, easing = emphasizedAccelerate),
            transformOrigin = transformOrigin
        ) + fadeOut(tween(SHORT2, easing = emphasizedAccelerate))
    }
}

/**
 * 系统「移除动画」开关（开发者选项 → 动画时长缩放 = 0）打开时为 true。
 * M3 要求动效可关闭：命中后所有 spec 退化为 `snap()`，转场退化为 `None`。
 * 由 [AppTheme] 提供，预览/测试默认 false。
 *
 * ponytail: 仅在主题创建时读一次，中途改系统开关需重启 App 生效；
 * 需要热更新时加 ContentObserver 监听 ANIMATOR_DURATION_SCALE。
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

/**
 * 呼吸脉动：AI 生成中/加载中的通用节奏（透明度在 1f 与 [minAlpha] 之间往复）。
 *
 * 时长/缓动取 M3 effects 规范：LONG1 + emphasized，与骨架屏同源，视觉节奏一致。
 * 减动效（[LocalReducedMotion]）下不闪，直接返回 1f。
 *
 * macOS 毛玻璃规范：不做装饰性脉动（脉冲/呼吸），那里动效只改颜色 —— 静态返回 1f。
 */
@Composable
fun rememberBreathingAlpha(minAlpha: Float = 0.4f): Float {
    if (LocalReducedMotion.current) return 1f
    if (LocalThemeStyle.current == ThemeStyle.MacOS) return 1f
    val pulse = rememberInfiniteTransition(label = "breathing_pulse")
    val animated by pulse.animateFloat(
        initialValue = 1f,
        targetValue = minAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(M3Motion.LONG1, easing = M3Motion.emphasized),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_alpha"
    )
    return animated
}
