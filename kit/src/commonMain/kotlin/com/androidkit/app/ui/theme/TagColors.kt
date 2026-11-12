package com.androidkit.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 标签色板 — M3 Tonal Palette 精简版，仅 6 个核心色调。
 * neutral 放在首位作为默认色。
 *
 * 业务代码禁止硬编码颜色，统一通过 tagColor() 引用。
 */
private val LightTagColors = mapOf(
    "neutral" to Color(0xFF5A5A5A),  // Warm dark gray
    "rose" to Color(0xFFB87070),     // Muted rose red (default)
    "sage" to Color(0xFF5B7A5B),     // Muted sage green
    "mauve" to Color(0xFF8B6B8B),    // Dusty mauve
    "clay" to Color(0xFFAD7A5A),     // Warm clay
    "slate" to Color(0xFF6B7A8A)     // Blue-gray slate
)

private val DarkTagColors = mapOf(
    "neutral" to Color(0xFFA0A0A0),  // Light warm gray
    "rose" to Color(0xFFD0A0A0),     // Soft muted rose red (default)
    "sage" to Color(0xFFB0C9A0),     // Soft sage green
    "mauve" to Color(0xFFD0A8D0),    // Soft mauve
    "clay" to Color(0xFFD8B8A0),     // Soft clay
    "slate" to Color(0xFFA0B8C8)     // Soft slate
)

/** 按「像素风格 + 明暗」取底座色板；品牌主题可在 [ThemeTokens.tagColors] 里覆盖。 */
internal fun tagColorsFor(style: ThemeStyle, isDark: Boolean): Map<String, Color> = when (style) {
    ThemeStyle.Swiss -> if (isDark) SwissDarkTagColors else SwissLightTagColors
    ThemeStyle.Notion -> if (isDark) NotionDarkTagColors else NotionLightTagColors
    ThemeStyle.EInk -> if (isDark) EInkDarkTagColors else EInkLightTagColors
    ThemeStyle.MacOS -> if (isDark) MacOSDarkTagColors else MacOSLightTagColors
    ThemeStyle.Mono -> if (isDark) DarkTagColors else LightTagColors
}

/**
 * 当前标签色板；由 [AppTheme] 注入（tokens 留空时回落到 [tagColorsFor]）。
 *
 * 内部实现，业务代码用 [tagColor] / [rememberTagColor]，别直接读它。
 */
internal val LocalTagColors = staticCompositionLocalOf { LightTagColors }

@Composable
private fun tagColorPalette(): Map<String, Color> = LocalTagColors.current

@Composable
fun tagColor(name: String): Color {
    val palette = tagColorPalette()
    return palette[name.lowercase()] ?: palette.getValue("neutral")
}

/**
 * Tag color for use inside non-Composable lambdas (e.g. forEach, items).
 * The palette is resolved in composable context, then remembered per [name].
 */
@Composable
fun rememberTagColor(name: String): Color {
    val palette = tagColorPalette()
    return remember(name, palette) { palette[name.lowercase()] ?: palette.getValue("neutral") }
}

/**
 * 色板键名清单 — 色板选择器等 UI 遍历时使用，保证可选色名与 tagColor() 映射一致
 */
val TagColorNames: List<String> = LightTagColors.keys.toList()
