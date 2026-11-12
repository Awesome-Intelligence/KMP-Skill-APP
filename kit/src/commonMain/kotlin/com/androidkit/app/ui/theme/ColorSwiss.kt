package com.androidkit.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Swiss（瑞士国际风格）主题色板 —— 黑白画布 + 原色强调，零圆角、零阴影、零渐变。
 *
 * 规范（StyleKit swiss-style）给的是纯浅色：
 * - 画布 `#ffffff`，文字与栏线 `#000000`
 * - 信号红 `#ff0000` 是**唯一**强调色：数字、激活的栏线、关键数字
 * - 群青 `#0057b8` 只做链接/可点标记，镉黄 `#ffcc00` 只做大色块分区
 * - 栏线灰 `#d4d4d4`，悬停底 `#f0f0f0`
 *
 * **深色版是自建的语义反转**（黑画布 + 白字）：规范本身没有深色契约
 * （readiness.darkMode = missing，"先造语义深色 token 再上深色"），
 * 反转时守住「一屏只用红或蓝之一」「红只到字符宽度」两条纪律。
 *
 * 红色 `#ff0000` 对白底只有 4.0:1，小字达不到 AA —— 所以**文字位**用 [redInk] `#c40000`（6.3:1），
 * 红块 / 栏线 / 数字仍用原色 `#ff0000`。
 */
object Swiss {
    /** 文字可用的深红（AA） */
    val redInk = Color(0xFFC40000)

    /** 深色画布上的亮红（#c40000 对黑底只有 3.3:1） */
    val redOnDark = Color(0xFFFF3B30)

    /** 色块用的原色红（白字在 32px/700 上够 3:1 大字对比） */
    val redBlock = Color(0xFFFF0000)

    // ── Light：白画布 ─────────────────────────────────────────────────────────
    val primaryLight = Color(0xFF000000)
    val onPrimaryLight = Color(0xFFFFFFFF)
    val primaryContainerLight = Color(0xFF000000)
    val onPrimaryContainerLight = Color(0xFFFFFFFF)
    val secondaryLight = Color(0xFF0057B8)
    val onSecondaryLight = Color(0xFFFFFFFF)
    val secondaryContainerLight = Color(0xFFE3ECFA)
    val onSecondaryContainerLight = Color(0xFF003061)
    val tertiaryLight = Color(0xFFFFCC00)
    val onTertiaryLight = Color(0xFF000000)
    val tertiaryContainerLight = Color(0xFFFFCC00)
    val onTertiaryContainerLight = Color(0xFF000000)
    val errorLight = Color(0xFFC40000)
    val onErrorLight = Color(0xFFFFFFFF)
    val errorContainerLight = Color(0xFFFFE5E5)
    val onErrorContainerLight = Color(0xFF5C0000)
    val backgroundLight = Color(0xFFFFFFFF)
    val onBackgroundLight = Color(0xFF000000)
    val surfaceLight = Color(0xFFFFFFFF)
    val onSurfaceLight = Color(0xFF000000)
    val surfaceVariantLight = Color(0xFFF0F0F0)
    val onSurfaceVariantLight = Color(0xFF4A4A4A)
    val outlineLight = Color(0xFF000000)
    val outlineVariantLight = Color(0xFFD4D4D4)
    val scrimLight = Color(0xFF000000)
    val inverseSurfaceLight = Color(0xFF000000)
    val inverseOnSurfaceLight = Color(0xFFFFFFFF)
    val inversePrimaryLight = Color(0xFFFFFFFF)
    // 画布不允许色调渐变 —— tint 取白 = 不着色
    val surfaceTintLight = Color(0xFFFFFFFF)
    val surfaceDimLight = Color(0xFFD9D9D9)
    val surfaceBrightLight = Color(0xFFFFFFFF)
    val surfaceContainerLowestLight = Color(0xFFFFFFFF)
    val surfaceContainerLowLight = Color(0xFFFAFAFA)
    val surfaceContainerLight = Color(0xFFF5F5F5)
    val surfaceContainerHighLight = Color(0xFFF0F0F0)
    val surfaceContainerHighestLight = Color(0xFFE6E6E6)

    // ── Dark：黑画布（自建反转） ─────────────────────────────────────────────
    val primaryDark = Color(0xFFFFFFFF)
    val onPrimaryDark = Color(0xFF000000)
    val primaryContainerDark = Color(0xFFFFFFFF)
    val onPrimaryContainerDark = Color(0xFF000000)
    val secondaryDark = Color(0xFF5B9BE0)
    val onSecondaryDark = Color(0xFF000000)
    val secondaryContainerDark = Color(0xFF12304F)
    val onSecondaryContainerDark = Color(0xFFCFE2FF)
    val tertiaryDark = Color(0xFFFFCC00)
    val onTertiaryDark = Color(0xFF000000)
    val tertiaryContainerDark = Color(0xFF4A3B00)
    val onTertiaryContainerDark = Color(0xFFFFE680)
    val errorDark = Color(0xFFFF6B6B)
    val onErrorDark = Color(0xFF3A0000)
    val errorContainerDark = Color(0xFF5C0000)
    val onErrorContainerDark = Color(0xFFFFD9D9)
    val backgroundDark = Color(0xFF000000)
    val onBackgroundDark = Color(0xFFFFFFFF)
    val surfaceDark = Color(0xFF000000)
    val onSurfaceDark = Color(0xFFFFFFFF)
    val surfaceVariantDark = Color(0xFF1A1A1A)
    val onSurfaceVariantDark = Color(0xFFBFBFBF)
    val outlineDark = Color(0xFFFFFFFF)
    val outlineVariantDark = Color(0xFF3D3D3D)
    val scrimDark = Color(0xFF000000)
    val inverseSurfaceDark = Color(0xFFFFFFFF)
    val inverseOnSurfaceDark = Color(0xFF000000)
    val inversePrimaryDark = Color(0xFF000000)
    val surfaceTintDark = Color(0xFF000000)
    val surfaceDimDark = Color(0xFF000000)
    val surfaceBrightDark = Color(0xFF1F1F1F)
    val surfaceContainerLowestDark = Color(0xFF000000)
    val surfaceContainerLowDark = Color(0xFF0D0D0D)
    val surfaceContainerDark = Color(0xFF141414)
    val surfaceContainerHighDark = Color(0xFF1F1F1F)
    val surfaceContainerHighestDark = Color(0xFF2A2A2A)
}

// ============================================================
// 瑞士主题的扩展语义色
// ============================================================

// 强调只用红/蓝两个原色；success/warning 保留功能色但压平成印刷色阶。
internal val SwissLightExtended = ExtendedColors(
    success = Color(0xFF1B7A4B),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFDFF2E6),
    warning = Color(0xFF8A5A00),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFF0D0),
    aiAccent = Color(0xFF0057B8),
    aiContainer = Color(0xFFE3ECFA),
    onAiContainer = Color(0xFF003061),
    // 规范强调色板里的 #6c3b00（黄在浅底上只有 1.5:1，做不了星标色）
    star = Color(0xFF6C3B00),
)

internal val SwissDarkExtended = ExtendedColors(
    success = Color(0xFF6BCB96),
    onSuccess = Color(0xFF003822),
    successContainer = Color(0xFF1B4D35),
    warning = Color(0xFFE8B84B),
    onWarning = Color(0xFF3F2D00),
    warningContainer = Color(0xFF57430F),
    aiAccent = Color(0xFF5B9BE0),
    aiContainer = Color(0xFF12304F),
    onAiContainer = Color(0xFFCFE2FF),
    star = Color(0xFFC08A3E),
)

// ============================================================
// 瑞士主题的标签色板（6 个平涂印刷色，键名与 [LightTagColors] 一致）
// ============================================================
internal val SwissLightTagColors = mapOf(
    "neutral" to Color(0xFF000000),
    "rose" to Swiss.redInk,
    "sage" to Color(0xFF1B7A4B),
    "mauve" to Color(0xFF5C3D91),
    "clay" to Color(0xFF8A5A00),
    "slate" to Color(0xFF0057B8)
)

internal val SwissDarkTagColors = mapOf(
    "neutral" to Color(0xFFFFFFFF),
    "rose" to Color(0xFFFF6B6B),
    "sage" to Color(0xFF6BCB96),
    "mauve" to Color(0xFFB79BEA),
    "clay" to Color(0xFFE8B84B),
    "slate" to Color(0xFF7FB0F0)
)

/**
 * 当前明暗下可读的强调红。
 *
 * 浅色用 [Swiss.redInk]（对白 6.3:1），深色用 [Swiss.redOnDark]（对黑 5.9:1）；
 * 原色 `#ff0000` 只用于色块和栏线，不进小字。
 */
@Composable
fun swissRed(): Color = if (LocalIsDarkTheme.current) Swiss.redOnDark else Swiss.redInk
