package com.androidkit.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color

/**
 * 一套完整主题的「四轴 + 三个开关」。
 *
 * 底座预置的 Mono / Swiss / Notion 等只是几套**预设**（`monoThemeTokens()` / `swissThemeTokens()` …）。
 * App 要做自己的品牌，就在自己的模块里造一个 [ThemeTokens]，**不用改 kit**：
 *
 * ```kotlin
 * // apps/<name>/theme/BrandTheme.kt
 * object BrandTheme {
 *     fun light() = themeTokens(
 *         colorScheme = lightColorScheme(
 *             primary = Color(0xFF00696D),
 *             onPrimary = Color(0xFFFFFFFF),
 *             primaryContainer = Color(0xFF9CF1F5),
 *             onPrimaryContainer = Color(0xFF002022),
 *         ),
 *         isDark = false,
 *     )
 *     fun dark() = themeTokens(colorScheme = darkColorScheme(...), isDark = true)
 * }
 *
 * AppTheme(tokens = if (dark) BrandTheme.dark() else BrandTheme.light()) { App() }
 * ```
 *
 * 四个轴：颜色 / 扩展语义色 / 排版 / 形状。三个开关：明暗、像素风格、圆角。
 * [tagColors] 留空时按 [themeStyle] 取底座色板（品牌色板自己传）。
 *
 * 为什么 [isDark] 在 tokens 里而不是 `AppTheme` 的参数：tokens 本来就是**按明暗各造一套**，
 * 再单独传一个 `darkTheme` 必然出现「拿浅色 tokens 配深色标记」的错配。
 */
data class ThemeTokens(
    /** Material 3 的 35 个颜色槽位。 */
    val colorScheme: ColorScheme,
    /** M3 之外的语义色（success / warning / aiAccent / star…）。 */
    val extendedColors: ExtendedColors,
    /** 字号 / 行高 / 字体。 */
    val typography: Typography,
    /** 圆角桶（small / medium / large…）。 */
    val shapes: Shapes,
    /** 这套 tokens 是深色还是浅色 —— 组件靠它选透明度/色阶。 */
    val isDark: Boolean,
    /**
     * 像素风格开关：只影响点阵底纹、像素组件这类「风格化」组件。
     * 品牌主题保持默认 [ThemeStyle.Mono]（不强制直角、不用像素字体）。
     */
    val themeStyle: ThemeStyle = ThemeStyle.Mono,
    /** 圆角风格：`themedCorner()` 会用这个，而不是只看 [themeStyle]。 */
    val cornerStyle: CornerStyle = CornerStyle.Rounded,
    /**
     * 标签色板（键：neutral / rose / sage / mauve / clay / slate）。
     * 留空 = 按 [themeStyle] + [isDark] 取底座色板。
     */
    val tagColors: Map<String, Color> = emptyMap(),
)

/**
 * 造一套自定义主题的便捷入口 —— 只传颜色也能跑，其余轴走合理默认值。
 *
 * 默认值刻意选「不抢品牌」的：M3 默认排版 / 圆角、Mono 的扩展语义色、Mono 标签色板。
 * 品牌有自己的一套就显式传；没有就先跑起来，后面再逐轴替换。
 *
 * @param isDark 这套色板是深色还是浅色（不是「当前系统是不是深色」，别传错）
 */
fun themeTokens(
    colorScheme: ColorScheme,
    isDark: Boolean,
    extendedColors: ExtendedColors = if (isDark) DarkExtended else LightExtended,
    typography: Typography = Typography,
    shapes: Shapes = Shapes(),
    cornerStyle: CornerStyle = CornerStyle.Rounded,
    tagColors: Map<String, Color> = emptyMap(),
): ThemeTokens = ThemeTokens(
    colorScheme = colorScheme,
    extendedColors = extendedColors,
    typography = typography,
    shapes = shapes,
    isDark = isDark,
    themeStyle = ThemeStyle.Mono,
    cornerStyle = cornerStyle,
    tagColors = tagColors,
)
