package com.androidkit.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Light Color Scheme - M3 Tonal Palette based (default to Mono)
// surfaceTint 用 outline（中性）而非 primary：Mono 的 primary 是近黑，全强度 tint 会把
// 抬升表面染黑；单色主题的 M3 正解就是拿中性色当 tint，层次由 surfaceContainer* 色阶承担。
// ponytail: 想要真正的品牌色 tint 就得先给 primary 一个真实色相，那是换主题不是修 bug。
private val LightColorScheme = lightColorScheme(
    primary = Mono.primaryLight,
    onPrimary = Mono.onPrimaryLight,
    primaryContainer = Mono.primaryContainerLight,
    onPrimaryContainer = Mono.onPrimaryContainerLight,
    secondary = Mono.secondaryLight,
    onSecondary = Mono.onSecondaryLight,
    secondaryContainer = Mono.secondaryContainerLight,
    onSecondaryContainer = Mono.onSecondaryContainerLight,
    tertiary = Mono.tertiaryLight,
    onTertiary = Mono.onTertiaryLight,
    tertiaryContainer = Mono.tertiaryContainerLight,
    onTertiaryContainer = Mono.onTertiaryContainerLight,
    error = Mono.errorLight,
    onError = Mono.onErrorLight,
    errorContainer = Mono.errorContainerLight,
    onErrorContainer = Mono.onErrorContainerLight,
    background = Mono.backgroundLight,
    onBackground = Mono.onBackgroundLight,
    surface = Mono.surfaceLight,
    onSurface = Mono.onSurfaceLight,
    surfaceVariant = Mono.surfaceVariantLight,
    onSurfaceVariant = Mono.onSurfaceVariantLight,
    outline = Mono.outlineLight,
    outlineVariant = Mono.outlineVariantLight,
    scrim = Mono.scrimLight,
    inverseSurface = Mono.inverseSurfaceLight,
    inverseOnSurface = Mono.inverseOnSurfaceLight,
    inversePrimary = Mono.inversePrimaryLight,
    surfaceTint = Mono.outlineLight,
    surfaceDim = Mono.surfaceDimLight,
    surfaceBright = Mono.surfaceBrightLight,
    surfaceContainerLowest = Mono.surfaceContainerLowestLight,
    surfaceContainerLow = Mono.surfaceContainerLowLight,
    surfaceContainer = Mono.surfaceContainerLight,
    surfaceContainerHigh = Mono.surfaceContainerHighLight,
    surfaceContainerHighest = Mono.surfaceContainerHighestLight
)

// Dark Color Scheme - M3 Tonal Palette based (default to Mono)
private val DarkColorScheme = darkColorScheme(
    primary = Mono.primaryDark,
    onPrimary = Mono.onPrimaryDark,
    primaryContainer = Mono.primaryContainerDark,
    onPrimaryContainer = Mono.onPrimaryContainerDark,
    secondary = Mono.secondaryDark,
    onSecondary = Mono.onSecondaryDark,
    secondaryContainer = Mono.secondaryContainerDark,
    onSecondaryContainer = Mono.onSecondaryContainerDark,
    tertiary = Mono.tertiaryDark,
    onTertiary = Mono.onTertiaryDark,
    tertiaryContainer = Mono.tertiaryContainerDark,
    onTertiaryContainer = Mono.onTertiaryContainerDark,
    error = Mono.errorDark,
    onError = Mono.onErrorDark,
    errorContainer = Mono.errorContainerDark,
    onErrorContainer = Mono.onErrorContainerDark,
    background = Mono.backgroundDark,
    onBackground = Mono.onBackgroundDark,
    surface = Mono.surfaceDark,
    onSurface = Mono.onSurfaceDark,
    surfaceVariant = Mono.surfaceVariantDark,
    onSurfaceVariant = Mono.onSurfaceVariantDark,
    outline = Mono.outlineDark,
    outlineVariant = Mono.outlineVariantDark,
    scrim = Mono.scrimDark,
    inverseSurface = Mono.inverseSurfaceDark,
    inverseOnSurface = Mono.inverseOnSurfaceDark,
    inversePrimary = Mono.inversePrimaryDark,
    surfaceTint = Mono.outlineDark,
    surfaceDim = Mono.surfaceDimDark,
    surfaceBright = Mono.surfaceBrightDark,
    surfaceContainerLowest = Mono.surfaceContainerLowestDark,
    surfaceContainerLow = Mono.surfaceContainerLowDark,
    surfaceContainer = Mono.surfaceContainerDark,
    surfaceContainerHigh = Mono.surfaceContainerHighDark,
    surfaceContainerHighest = Mono.surfaceContainerHighestDark
)

// Swiss Color Schemes — 纯白画布 + 纯黑文字 + 信号红强调（瑞士国际风格）
private val SwissLightColorScheme = lightColorScheme(
    primary = Swiss.primaryLight,
    onPrimary = Swiss.onPrimaryLight,
    primaryContainer = Swiss.primaryContainerLight,
    onPrimaryContainer = Swiss.onPrimaryContainerLight,
    secondary = Swiss.secondaryLight,
    onSecondary = Swiss.onSecondaryLight,
    secondaryContainer = Swiss.secondaryContainerLight,
    onSecondaryContainer = Swiss.onSecondaryContainerLight,
    tertiary = Swiss.tertiaryLight,
    onTertiary = Swiss.onTertiaryLight,
    tertiaryContainer = Swiss.tertiaryContainerLight,
    onTertiaryContainer = Swiss.onTertiaryContainerLight,
    error = Swiss.errorLight,
    onError = Swiss.onErrorLight,
    errorContainer = Swiss.errorContainerLight,
    onErrorContainer = Swiss.onErrorContainerLight,
    background = Swiss.backgroundLight,
    onBackground = Swiss.onBackgroundLight,
    surface = Swiss.surfaceLight,
    onSurface = Swiss.onSurfaceLight,
    surfaceVariant = Swiss.surfaceVariantLight,
    onSurfaceVariant = Swiss.onSurfaceVariantLight,
    outline = Swiss.outlineLight,
    outlineVariant = Swiss.outlineVariantLight,
    scrim = Swiss.scrimLight,
    inverseSurface = Swiss.inverseSurfaceLight,
    inverseOnSurface = Swiss.inverseOnSurfaceLight,
    inversePrimary = Swiss.inversePrimaryLight,
    surfaceTint = Swiss.surfaceTintLight,
    surfaceDim = Swiss.surfaceDimLight,
    surfaceBright = Swiss.surfaceBrightLight,
    surfaceContainerLowest = Swiss.surfaceContainerLowestLight,
    surfaceContainerLow = Swiss.surfaceContainerLowLight,
    surfaceContainer = Swiss.surfaceContainerLight,
    surfaceContainerHigh = Swiss.surfaceContainerHighLight,
    surfaceContainerHighest = Swiss.surfaceContainerHighestLight
)

// 深色是自建的语义反转（黑画布 + 白字），规范本身没给深色契约
private val SwissDarkColorScheme = darkColorScheme(
    primary = Swiss.primaryDark,
    onPrimary = Swiss.onPrimaryDark,
    primaryContainer = Swiss.primaryContainerDark,
    onPrimaryContainer = Swiss.onPrimaryContainerDark,
    secondary = Swiss.secondaryDark,
    onSecondary = Swiss.onSecondaryDark,
    secondaryContainer = Swiss.secondaryContainerDark,
    onSecondaryContainer = Swiss.onSecondaryContainerDark,
    tertiary = Swiss.tertiaryDark,
    onTertiary = Swiss.onTertiaryDark,
    tertiaryContainer = Swiss.tertiaryContainerDark,
    onTertiaryContainer = Swiss.onTertiaryContainerDark,
    error = Swiss.errorDark,
    onError = Swiss.onErrorDark,
    errorContainer = Swiss.errorContainerDark,
    onErrorContainer = Swiss.onErrorContainerDark,
    background = Swiss.backgroundDark,
    onBackground = Swiss.onBackgroundDark,
    surface = Swiss.surfaceDark,
    onSurface = Swiss.onSurfaceDark,
    surfaceVariant = Swiss.surfaceVariantDark,
    onSurfaceVariant = Swiss.onSurfaceVariantDark,
    outline = Swiss.outlineDark,
    outlineVariant = Swiss.outlineVariantDark,
    scrim = Swiss.scrimDark,
    inverseSurface = Swiss.inverseSurfaceDark,
    inverseOnSurface = Swiss.inverseOnSurfaceDark,
    inversePrimary = Swiss.inversePrimaryDark,
    surfaceTint = Swiss.surfaceTintDark,
    surfaceDim = Swiss.surfaceDimDark,
    surfaceBright = Swiss.surfaceBrightDark,
    surfaceContainerLowest = Swiss.surfaceContainerLowestDark,
    surfaceContainerLow = Swiss.surfaceContainerLowDark,
    surfaceContainer = Swiss.surfaceContainerDark,
    surfaceContainerHigh = Swiss.surfaceContainerHighDark,
    surfaceContainerHighest = Swiss.surfaceContainerHighestDark
)

// Notion Color Schemes —— 米色纸面 + 暖墨字 + 发丝线，槽位结构与 Mono 一致
private val NotionLightColorScheme = lightColorScheme(
    primary = Notion.primaryLight,
    onPrimary = Notion.onPrimaryLight,
    primaryContainer = Notion.primaryContainerLight,
    onPrimaryContainer = Notion.onPrimaryContainerLight,
    secondary = Notion.secondaryLight,
    onSecondary = Notion.onSecondaryLight,
    secondaryContainer = Notion.secondaryContainerLight,
    onSecondaryContainer = Notion.onSecondaryContainerLight,
    tertiary = Notion.tertiaryLight,
    onTertiary = Notion.onTertiaryLight,
    tertiaryContainer = Notion.tertiaryContainerLight,
    onTertiaryContainer = Notion.onTertiaryContainerLight,
    error = Notion.errorLight,
    onError = Notion.onErrorLight,
    errorContainer = Notion.errorContainerLight,
    onErrorContainer = Notion.onErrorContainerLight,
    background = Notion.backgroundLight,
    onBackground = Notion.onBackgroundLight,
    surface = Notion.surfaceLight,
    onSurface = Notion.onSurfaceLight,
    surfaceVariant = Notion.surfaceVariantLight,
    onSurfaceVariant = Notion.onSurfaceVariantLight,
    outline = Notion.outlineLight,
    outlineVariant = Notion.outlineVariantLight,
    scrim = Notion.scrimLight,
    inverseSurface = Notion.inverseSurfaceLight,
    inverseOnSurface = Notion.inverseOnSurfaceLight,
    inversePrimary = Notion.inversePrimaryLight,
    surfaceTint = Notion.surfaceTintLight,
    surfaceDim = Notion.surfaceDimLight,
    surfaceBright = Notion.surfaceBrightLight,
    surfaceContainerLowest = Notion.surfaceContainerLowestLight,
    surfaceContainerLow = Notion.surfaceContainerLowLight,
    surfaceContainer = Notion.surfaceContainerLight,
    surfaceContainerHigh = Notion.surfaceContainerHighLight,
    surfaceContainerHighest = Notion.surfaceContainerHighestLight
)

// 深色是自建的语义反转（规范本身没给深色契约）
private val NotionDarkColorScheme = darkColorScheme(
    primary = Notion.primaryDark,
    onPrimary = Notion.onPrimaryDark,
    primaryContainer = Notion.primaryContainerDark,
    onPrimaryContainer = Notion.onPrimaryContainerDark,
    secondary = Notion.secondaryDark,
    onSecondary = Notion.onSecondaryDark,
    secondaryContainer = Notion.secondaryContainerDark,
    onSecondaryContainer = Notion.onSecondaryContainerDark,
    tertiary = Notion.tertiaryDark,
    onTertiary = Notion.onTertiaryDark,
    tertiaryContainer = Notion.tertiaryContainerDark,
    onTertiaryContainer = Notion.onTertiaryContainerDark,
    error = Notion.errorDark,
    onError = Notion.onErrorDark,
    errorContainer = Notion.errorContainerDark,
    onErrorContainer = Notion.onErrorContainerDark,
    background = Notion.backgroundDark,
    onBackground = Notion.onBackgroundDark,
    surface = Notion.surfaceDark,
    onSurface = Notion.onSurfaceDark,
    surfaceVariant = Notion.surfaceVariantDark,
    onSurfaceVariant = Notion.onSurfaceVariantDark,
    outline = Notion.outlineDark,
    outlineVariant = Notion.outlineVariantDark,
    scrim = Notion.scrimDark,
    inverseSurface = Notion.inverseSurfaceDark,
    inverseOnSurface = Notion.inverseOnSurfaceDark,
    inversePrimary = Notion.inversePrimaryDark,
    surfaceTint = Notion.surfaceTintDark,
    surfaceDim = Notion.surfaceDimDark,
    surfaceBright = Notion.surfaceBrightDark,
    surfaceContainerLowest = Notion.surfaceContainerLowestDark,
    surfaceContainerLow = Notion.surfaceContainerLowDark,
    surfaceContainer = Notion.surfaceContainerDark,
    surfaceContainerHigh = Notion.surfaceContainerHighDark,
    surfaceContainerHighest = Notion.surfaceContainerHighestDark
)

// E-Ink Color Schemes —— 暖纸底 + 墨色 + 灰阶（电子纸），槽位结构与 Mono 一致
private val EInkLightColorScheme = lightColorScheme(
    primary = EInk.primaryLight,
    onPrimary = EInk.onPrimaryLight,
    primaryContainer = EInk.primaryContainerLight,
    onPrimaryContainer = EInk.onPrimaryContainerLight,
    secondary = EInk.secondaryLight,
    onSecondary = EInk.onSecondaryLight,
    secondaryContainer = EInk.secondaryContainerLight,
    onSecondaryContainer = EInk.onSecondaryContainerLight,
    tertiary = EInk.tertiaryLight,
    onTertiary = EInk.onTertiaryLight,
    tertiaryContainer = EInk.tertiaryContainerLight,
    onTertiaryContainer = EInk.onTertiaryContainerLight,
    error = EInk.errorLight,
    onError = EInk.onErrorLight,
    errorContainer = EInk.errorContainerLight,
    onErrorContainer = EInk.onErrorContainerLight,
    background = EInk.backgroundLight,
    onBackground = EInk.onBackgroundLight,
    surface = EInk.surfaceLight,
    onSurface = EInk.onSurfaceLight,
    surfaceVariant = EInk.surfaceVariantLight,
    onSurfaceVariant = EInk.onSurfaceVariantLight,
    outline = EInk.outlineLight,
    outlineVariant = EInk.outlineVariantLight,
    scrim = EInk.scrimLight,
    inverseSurface = EInk.inverseSurfaceLight,
    inverseOnSurface = EInk.inverseOnSurfaceLight,
    inversePrimary = EInk.inversePrimaryLight,
    surfaceTint = EInk.surfaceTintLight,
    surfaceDim = EInk.surfaceDimLight,
    surfaceBright = EInk.surfaceBrightLight,
    surfaceContainerLowest = EInk.surfaceContainerLowestLight,
    surfaceContainerLow = EInk.surfaceContainerLowLight,
    surfaceContainer = EInk.surfaceContainerLight,
    surfaceContainerHigh = EInk.surfaceContainerHighLight,
    surfaceContainerHighest = EInk.surfaceContainerHighestLight
)

// 深色是自建的语义反转（墨底 + 纸字），规范本身没给深色契约
private val EInkDarkColorScheme = darkColorScheme(
    primary = EInk.primaryDark,
    onPrimary = EInk.onPrimaryDark,
    primaryContainer = EInk.primaryContainerDark,
    onPrimaryContainer = EInk.onPrimaryContainerDark,
    secondary = EInk.secondaryDark,
    onSecondary = EInk.onSecondaryDark,
    secondaryContainer = EInk.secondaryContainerDark,
    onSecondaryContainer = EInk.onSecondaryContainerDark,
    tertiary = EInk.tertiaryDark,
    onTertiary = EInk.onTertiaryDark,
    tertiaryContainer = EInk.tertiaryContainerDark,
    onTertiaryContainer = EInk.onTertiaryContainerDark,
    error = EInk.errorDark,
    onError = EInk.onErrorDark,
    errorContainer = EInk.errorContainerDark,
    onErrorContainer = EInk.onErrorContainerDark,
    background = EInk.backgroundDark,
    onBackground = EInk.onBackgroundDark,
    surface = EInk.surfaceDark,
    onSurface = EInk.onSurfaceDark,
    surfaceVariant = EInk.surfaceVariantDark,
    onSurfaceVariant = EInk.onSurfaceVariantDark,
    outline = EInk.outlineDark,
    outlineVariant = EInk.outlineVariantDark,
    scrim = EInk.scrimDark,
    inverseSurface = EInk.inverseSurfaceDark,
    inverseOnSurface = EInk.inverseOnSurfaceDark,
    inversePrimary = EInk.inversePrimaryDark,
    surfaceTint = EInk.surfaceTintDark,
    surfaceDim = EInk.surfaceDimDark,
    surfaceBright = EInk.surfaceBrightDark,
    surfaceContainerLowest = EInk.surfaceContainerLowestDark,
    surfaceContainerLow = EInk.surfaceContainerLowDark,
    surfaceContainer = EInk.surfaceContainerDark,
    surfaceContainerHigh = EInk.surfaceContainerHighDark,
    surfaceContainerHighest = EInk.surfaceContainerHighestDark
)

// ============================================================
// macOS 毛玻璃（vibrancy）—— 三档灰 + 系统蓝，规范原生是深色
// ============================================================

private val MacOSDarkColorScheme = darkColorScheme(
    primary = MacOS.primaryDark,
    onPrimary = MacOS.onPrimaryDark,
    primaryContainer = MacOS.primaryContainerDark,
    onPrimaryContainer = MacOS.onPrimaryContainerDark,
    secondary = MacOS.secondaryDark,
    onSecondary = MacOS.onSecondaryDark,
    secondaryContainer = MacOS.secondaryContainerDark,
    onSecondaryContainer = MacOS.onSecondaryContainerDark,
    tertiary = MacOS.tertiaryDark,
    onTertiary = MacOS.onTertiaryDark,
    tertiaryContainer = MacOS.tertiaryContainerDark,
    onTertiaryContainer = MacOS.onTertiaryContainerDark,
    error = MacOS.errorDark,
    onError = MacOS.onErrorDark,
    errorContainer = MacOS.errorContainerDark,
    onErrorContainer = MacOS.onErrorContainerDark,
    background = MacOS.backgroundDark,
    onBackground = MacOS.onBackgroundDark,
    surface = MacOS.surfaceDark,
    onSurface = MacOS.onSurfaceDark,
    surfaceVariant = MacOS.surfaceVariantDark,
    onSurfaceVariant = MacOS.onSurfaceVariantDark,
    outline = MacOS.outlineDark,
    outlineVariant = MacOS.outlineVariantDark,
    scrim = MacOS.scrimDark,
    inverseSurface = MacOS.inverseSurfaceDark,
    inverseOnSurface = MacOS.inverseOnSurfaceDark,
    inversePrimary = MacOS.inversePrimaryDark,
    surfaceTint = MacOS.surfaceTintDark,
    surfaceDim = MacOS.surfaceDimDark,
    surfaceBright = MacOS.surfaceBrightDark,
    surfaceContainerLowest = MacOS.surfaceContainerLowestDark,
    surfaceContainerLow = MacOS.surfaceContainerLowDark,
    surfaceContainer = MacOS.surfaceContainerDark,
    surfaceContainerHigh = MacOS.surfaceContainerHighDark,
    surfaceContainerHighest = MacOS.surfaceContainerHighestDark
)

// 浅色是自建的（规范只给了深色契约）
private val MacOSLightColorScheme = lightColorScheme(
    primary = MacOS.primaryLight,
    onPrimary = MacOS.onPrimaryLight,
    primaryContainer = MacOS.primaryContainerLight,
    onPrimaryContainer = MacOS.onPrimaryContainerLight,
    secondary = MacOS.secondaryLight,
    onSecondary = MacOS.onSecondaryLight,
    secondaryContainer = MacOS.secondaryContainerLight,
    onSecondaryContainer = MacOS.onSecondaryContainerLight,
    tertiary = MacOS.tertiaryLight,
    onTertiary = MacOS.onTertiaryLight,
    tertiaryContainer = MacOS.tertiaryContainerLight,
    onTertiaryContainer = MacOS.onTertiaryContainerLight,
    error = MacOS.errorLight,
    onError = MacOS.onErrorLight,
    errorContainer = MacOS.errorContainerLight,
    onErrorContainer = MacOS.onErrorContainerLight,
    background = MacOS.backgroundLight,
    onBackground = MacOS.onBackgroundLight,
    surface = MacOS.surfaceLight,
    onSurface = MacOS.onSurfaceLight,
    surfaceVariant = MacOS.surfaceVariantLight,
    onSurfaceVariant = MacOS.onSurfaceVariantLight,
    outline = MacOS.outlineLight,
    outlineVariant = MacOS.outlineVariantLight,
    scrim = MacOS.scrimLight,
    inverseSurface = MacOS.inverseSurfaceLight,
    inverseOnSurface = MacOS.inverseOnSurfaceLight,
    inversePrimary = MacOS.inversePrimaryLight,
    surfaceTint = MacOS.surfaceTintLight,
    surfaceDim = MacOS.surfaceDimLight,
    surfaceBright = MacOS.surfaceBrightLight,
    surfaceContainerLowest = MacOS.surfaceContainerLowestLight,
    surfaceContainerLow = MacOS.surfaceContainerLowLight,
    surfaceContainer = MacOS.surfaceContainerLight,
    surfaceContainerHigh = MacOS.surfaceContainerHighLight,
    surfaceContainerHighest = MacOS.surfaceContainerHighestLight
)

/**
 * 主题风格。底座预置能力：App 层自行选择并持久化。
 * - [ThemeStyle.Mono] — 默认，暖中性单色 + M3 动效
 * - [ThemeStyle.Swiss] — 瑞士国际风格（黑白 + 红强调、Helvetica 系无衬线、直角、150ms 变色）
 * - [ThemeStyle.Notion] — Notion 风格（米色纸面 + 墨字、4px 圆角、只变色不动效）
 * - [ThemeStyle.EInk] — 电子纸（暖纸 + 墨色 + 灰阶、等宽数值、直角、180ms 变色）
 * - [ThemeStyle.MacOS] — macOS 毛玻璃（三档灰 + 系统蓝、衬线标题、半透明层、200ms 变色）
 */
enum class ThemeStyle { Mono, Swiss, Notion, EInk, MacOS }

/**
 * 外观（深浅色）模式。
 *
 * - [System] — 跟随系统（`isSystemInDarkTheme()`）
 * - [Light] / [Dark] — 强制浅色 / 深色
 */
enum class AppearanceMode { System, Light, Dark }

/**
 * 把外观模式落成实际深浅色。[System] 时用系统值。
 *
 * 用它而不是到处 `when`：设置页和入口都要用同一套判定，散开写早晚不一致。
 */
fun AppearanceMode.resolveDark(systemDark: Boolean): Boolean = when (this) {
    AppearanceMode.System -> systemDark
    AppearanceMode.Light -> false
    AppearanceMode.Dark -> true
}

/** 瑞士 / 电子纸主题全直角：网格和纸面印刷都没有圆角，层次靠硬边框和色阶 */
private val SquareShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

/**
 * Notion 主题的圆角：卡片/按钮/输入框一律 4px，只有弹层类大容器给到 8px。
 *
 * 规范原话「Radius stays at 4px or below, except pill tags」——
 * 弹层用 8px 是 M3 自己的 large/extraLarge 槽位需要，不是风格破例。
 */
private val NotionShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(4.dp),
    extraLarge = RoundedCornerShape(8.dp)
)

/**
 * macOS 主题的圆角：按钮/输入框 6px、卡片 10px、面板 14px。
 *
 * 规范原表：cards 10 / buttons 6 / inputs 6 / panels 14 / pills 999。
 * M3 的五个桶照这个顺序分配；药丸（999）不占桶 —— 用 `CircleShape` 显式画。
 */
private val MacOSShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(14.dp)
)

/** 底座预置：Mono（暖中性单色 + M3 动效）。App 层想微调品牌色，可以拿它当基底再 copy。 */
fun monoThemeTokens(isDark: Boolean): ThemeTokens = ThemeTokens(
    colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
    extendedColors = if (isDark) DarkExtended else LightExtended,
    typography = Typography,
    shapes = Shapes(),
    isDark = isDark,
    themeStyle = ThemeStyle.Mono,
    cornerStyle = CornerStyle.Rounded,
    tagColors = tagColorsFor(ThemeStyle.Mono, isDark),
)

/**
 * 底座预置：Swiss（瑞士国际风格）。
 *
 * 黑白画布 + 信号红强调、Helvetica 系无衬线（只有 400/700）、全直角、
 * 150ms ease-out 只变色。规范本身只给了浅色，深色版是自建的语义反转。
 */
fun swissThemeTokens(isDark: Boolean): ThemeTokens = ThemeTokens(
    colorScheme = if (isDark) SwissDarkColorScheme else SwissLightColorScheme,
    extendedColors = if (isDark) SwissDarkExtended else SwissLightExtended,
    typography = SwissTypography,
    shapes = SquareShapes,
    isDark = isDark,
    themeStyle = ThemeStyle.Swiss,
    cornerStyle = CornerStyle.Square,
    tagColors = tagColorsFor(ThemeStyle.Swiss, isDark),
)

/**
 * 底座预置：Notion（文档工具风格）。
 *
 * 米色纸面 `#f7f6f3` + 暖墨字 `#37352f` + 1px 发丝线，4px 圆角，
 * 150ms ease-out 只变色（不位移、不缩放）。规范只给了浅色，深色版是自建的语义反转。
 */
fun notionThemeTokens(isDark: Boolean): ThemeTokens = ThemeTokens(
    colorScheme = if (isDark) NotionDarkColorScheme else NotionLightColorScheme,
    extendedColors = if (isDark) NotionDarkExtended else NotionLightExtended,
    typography = NotionTypography,
    shapes = NotionShapes,
    isDark = isDark,
    themeStyle = ThemeStyle.Notion,
    cornerStyle = CornerStyle.Rounded,
    tagColors = tagColorsFor(ThemeStyle.Notion, isDark),
)

/**
 * 底座预置：E-Ink（电子纸风格）。
 *
 * 暖纸底 `#f5f3ea` + 墨色 `#20211e` + 三档灰阶、全直角、等宽数值、
 * 180ms ease-out 只变色。规范只给了浅色，深色版是自建的语义反转（墨底 + 纸字）。
 */
fun eInkThemeTokens(isDark: Boolean): ThemeTokens = ThemeTokens(
    colorScheme = if (isDark) EInkDarkColorScheme else EInkLightColorScheme,
    extendedColors = if (isDark) EInkDarkExtended else EInkLightExtended,
    typography = EInkTypography,
    shapes = SquareShapes,
    isDark = isDark,
    themeStyle = ThemeStyle.EInk,
    cornerStyle = CornerStyle.Square,
    tagColors = tagColorsFor(ThemeStyle.EInk, isDark),
)

/**
 * 底座预置：macOS 毛玻璃（vibrancy）。
 *
 * 三档灰 `#1c1c1e → #2c2c2e → #3a3a3c` 定层次，系统蓝 `#0a84ff` 是唯一强调色，
 * 衬线标题 + 无衬线正文 + 等宽代码，圆角 6/10/14，
 * 200ms ease-out **只改颜色**（不位移、不缩放）。
 *
 * 深色是规范原生；浅色是自建的 macOS 浅色系统（见 [ColorMacOS]）。
 */
fun macOSThemeTokens(isDark: Boolean): ThemeTokens = ThemeTokens(
    colorScheme = if (isDark) MacOSDarkColorScheme else MacOSLightColorScheme,
    extendedColors = if (isDark) MacOSDarkExtended else MacOSLightExtended,
    typography = MacOSTypography,
    shapes = MacOSShapes,
    isDark = isDark,
    themeStyle = ThemeStyle.MacOS,
    cornerStyle = CornerStyle.Rounded,
    tagColors = tagColorsFor(ThemeStyle.MacOS, isDark),
)

/**
 * 阴影高度 —— 瑞士风格规范写死「Elevation: None」，该主题下所有 elevation 归零。
 *
 * 组件里凡是 `elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)` 这种写法，
 * 把默认值换成 `themedElevation(1.dp)` 就行；别的主题原样不动。
 *
 * Notion 同样是平铺文档（只有弹层允许阴影），所以也归零。
 * 电子纸是纸面印刷，层次靠栏线和色阶，也归零。
 *
 * macOS 毛玻璃同理：「层次靠背景深浅，不靠阴影」—— 它的层级就是那三档灰。
 */
@Composable
fun themedElevation(default: Dp): Dp =
    when (LocalThemeStyle.current) {
        ThemeStyle.Swiss, ThemeStyle.Notion, ThemeStyle.EInk, ThemeStyle.MacOS -> 0.dp
        else -> default
    }

/**
 * 底座预置主题入口（Mono / Swiss / Notion / EInk / MacOS）。
 *
 * **品牌主题不要改这里** —— 在 App 自己的模块里造 [ThemeTokens]，走下面那个 tokens 重载。
 * 改 kit 的 Theme.kt 会让下游 fork 丢掉底座更新。
 */
@Composable
fun AppTheme(
    /**
     * 主题风格。
     *
     * 默认**继承外层**（[LocalThemeStyle]）—— 外层套了 AppTheme 时，嵌套调用不再硬切回 Mono。
     * 最外层没有主题时还是 Mono（[LocalThemeStyle] 的默认值），历史行为不变。
     */
    style: ThemeStyle = LocalThemeStyle.current,
    /**
     * 深浅色。
     *
     * 嵌套时继承外层（[LocalIsDarkTheme]），最外层才读系统设置 ——
     * 理由同 style：套在外层主题里的 AppTheme 不该把用户的深色选择重置掉。
     */
    darkTheme: Boolean = defaultDarkTheme(),
    /**
     * 系统是否关掉了动画（无障碍设置）。
     *
     * ponytail: 读系统开关是平台活儿（Android 读 Settings.Global，iOS 读 UIKit），
     * 公共层不碰平台 API —— 由各端启动时读出来传进来。
     *
     * 默认继承外层（[LocalReducedMotion]），最外层是 false。
     */
    reducedMotion: Boolean = LocalReducedMotion.current,
    content: @Composable () -> Unit
) {
    val tokens = when (style) {
        ThemeStyle.Mono -> monoThemeTokens(darkTheme)
        ThemeStyle.Swiss -> swissThemeTokens(darkTheme)
        ThemeStyle.Notion -> notionThemeTokens(darkTheme)
        ThemeStyle.EInk -> eInkThemeTokens(darkTheme)
        ThemeStyle.MacOS -> macOSThemeTokens(darkTheme)
    }
    AppTheme(tokens = tokens, reducedMotion = reducedMotion, content = content)
}

/**
 * 按 [ThemeTokens] 装配主题 —— **App 自己的品牌主题走这个重载**。
 *
 * 明暗在 tokens 里（[ThemeTokens.isDark]），所以这里不再收 `darkTheme`：
 * 一套 tokens 就是一套完整的、已经定好明暗的主题，避免「浅色 tokens + 深色标记」的错配。
 *
 * ```kotlin
 * AppTheme(tokens = if (systemDark) Brand.dark() else Brand.light()) { App() }
 * ```
 */
@Composable
fun AppTheme(
    tokens: ThemeTokens,
    reducedMotion: Boolean = LocalReducedMotion.current,
    content: @Composable () -> Unit
) {
    // 标签色板留空 = 跟随主题风格取底座色板；品牌有自己的色板就在 tokens 里传
    val tagColors = tokens.tagColors.ifEmpty { tagColorsFor(tokens.themeStyle, tokens.isDark) }

    CompositionLocalProvider(
        LocalReducedMotion provides reducedMotion,
        LocalIsDarkTheme provides tokens.isDark,
        LocalThemeStyle provides tokens.themeStyle,
        LocalExtendedColors provides tokens.extendedColors,
        LocalTagColors provides tagColors,
        LocalCornerStyle provides tokens.cornerStyle,
        LocalThemeDepth provides LocalThemeDepth.current + 1,
    ) {
        MaterialTheme(
            colorScheme = tokens.colorScheme,
            typography = tokens.typography,
            shapes = tokens.shapes, // Mono = M3 默认圆角 4/8/12/16/28；Swiss / E-Ink = 全 0
            content = content
        )
    }
}

/** 当前主题风格，位图组件/色板分发用；预览/测试默认 Mono */
val LocalThemeStyle = staticCompositionLocalOf { ThemeStyle.Mono }

/**
 * AppTheme 的嵌套层数 —— 0 = 当前不在任何 AppTheme 里。
 *
 * 用来区分「外面没有主题，按默认值来」和「外面有主题，继承它」：
 * [LocalIsDarkTheme] 的默认值是 false，意思是「浅色」，没法用真假区分这两种情况。
 *
 * internal：只有主题装配自己要用，不进公开 API 基线。
 */
internal val LocalThemeDepth = staticCompositionLocalOf { 0 }

/**
 * 深浅色的默认值：套在主题里就继承外层，最外层才读系统设置。
 *
 * 分开写是因为 [LocalIsDarkTheme] 默认 false = 浅色，直接拿它当默认会让最外层的
 * AppTheme() 永远浅色 —— 系统深色就丢了。
 */
@Composable
private fun defaultDarkTheme(): Boolean =
    if (LocalThemeDepth.current > 0) LocalIsDarkTheme.current else isSystemInDarkTheme()

/**
 * 当前生效的深浅色（已包含用户在设置里强制浅色/深色的选择）。
 *
 * 别再用 `isSystemInDarkTheme()`：它只读系统设置，跟 `appearanceMode` 覆盖冲突 ——
 * 用户强制深色时系统是浅色，读到的还是 false，透明度/色阶就全反了。
 */
val LocalIsDarkTheme = staticCompositionLocalOf { false }
