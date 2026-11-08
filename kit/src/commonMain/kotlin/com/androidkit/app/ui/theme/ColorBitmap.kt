package com.androidkit.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Bitmap（位图）主题色板 — 冷灰白中性色阶 + 橙色单一强调色。
 *
 * 与 [Mono] 同一套 35 槽位结构，主题切换靠 [ThemeStyle] 分发，组件零改动。
 * 中性色取 hue ≈ 250° 的冷灰白（对比 Mono 的暖灰 70°），关键对比对已过 WCAG 校验：
 * primary/背景 ≥4.5:1，outline/背景 ≥3:1（浅深两版均验）。
 *
 * primary 是**墨色**（浅色下近黑 `#17191E`、深色下近白 `#F2F3F6`）：
 * 按钮 / 选中芯片 / 选中日期 / 进度条格子一律用墨色，对应 1-bit 老游戏的实心黑块。
 * 橙色**不进 primary**，只留给强调（`star` / `aiAccent`：光标、图表前沿点、收藏），
 * 这样"强调色只用橙"才成立 —— 否则满屏按钮都是橙的，橙就不算强调了。
 *
 * ponytail: 想要整体调明暗改整张表重跑对比度校验，别单改某个 hex ——
 * 色阶一致性比单项好看重要（同 Mono 的约定）。
 */
object Bitmap {
    // ── Light：冷灰白底 ──────────────────────────────────────────────
    val primaryLight = Color(0xFF17191E)
    val onPrimaryLight = Color(0xFFFFFFFF)
    val primaryContainerLight = Color(0xFFD8DBE0)
    val onPrimaryContainerLight = Color(0xFF16191E)
    val secondaryLight = Color(0xFF3F454E)
    val onSecondaryLight = Color(0xFFFFFFFF)
    val secondaryContainerLight = Color(0xFFDDE1E6)
    val onSecondaryContainerLight = Color(0xFF16191E)
    val tertiaryLight = Color(0xFF4E5560)
    val onTertiaryLight = Color(0xFFFFFFFF)
    val tertiaryContainerLight = Color(0xFFD8DDE4)
    val onTertiaryContainerLight = Color(0xFF1B2029)
    val errorLight = Color(0xFFB3261E)
    val onErrorLight = Color(0xFFFFFFFF)
    val errorContainerLight = Color(0xFFF9D8D4)
    val onErrorContainerLight = Color(0xFF80150F)
    val backgroundLight = Color(0xFFF7F8FA)
    val onBackgroundLight = Color(0xFF17191E)
    val surfaceLight = Color(0xFFF7F8FA)
    val onSurfaceLight = Color(0xFF17191E)
    val surfaceVariantLight = Color(0xFFE4E7EB)
    val onSurfaceVariantLight = Color(0xFF45484F)
    val outlineLight = Color(0xFF757982)
    val outlineVariantLight = Color(0xFFD8DBE0)
    val scrimLight = Color(0xFF000000)
    val inverseSurfaceLight = Color(0xFF2C2F34)
    val inverseOnSurfaceLight = Color(0xFFF1F3F6)
    val inversePrimaryLight = Color(0xFFD8DBE0)
    val surfaceDimLight = Color(0xFFD8DBDF)
    val surfaceBrightLight = Color(0xFFF7F8FA)
    val surfaceContainerLowestLight = Color(0xFFFFFFFF)
    val surfaceContainerLowLight = Color(0xFFF1F3F6)
    val surfaceContainerLight = Color(0xFFECEEF2)
    val surfaceContainerHighLight = Color(0xFFE6E9ED)
    val surfaceContainerHighestLight = Color(0xFFE0E3E8)

    // ── Dark：冷炭黑底，橙色强调共用 ─────────────────────────────────
    val primaryDark = Color(0xFFF2F3F6)
    val onPrimaryDark = Color(0xFF15171B)
    val primaryContainerDark = Color(0xFF3B3E44)
    val onPrimaryContainerDark = Color(0xFFEDEFF2)
    val secondaryDark = Color(0xFFC3C7CE)
    val onSecondaryDark = Color(0xFF1E2228)
    val secondaryContainerDark = Color(0xFF31353C)
    val onSecondaryContainerDark = Color(0xFFE1E4E9)
    val tertiaryDark = Color(0xFF9AA2AD)
    val onTertiaryDark = Color(0xFF22272F)
    val tertiaryContainerDark = Color(0xFF3A414B)
    val onTertiaryContainerDark = Color(0xFFD5DBE3)
    val errorDark = Color(0xFFFFB3AA)
    val onErrorDark = Color(0xFF690005)
    val errorContainerDark = Color(0xFF93000A)
    val onErrorContainerDark = Color(0xFFFFDAD6)
    val backgroundDark = Color(0xFF15171B)
    val onBackgroundDark = Color(0xFFE3E5E9)
    val surfaceDark = Color(0xFF15171B)
    val onSurfaceDark = Color(0xFFE3E5E9)
    val surfaceVariantDark = Color(0xFF262930)
    val onSurfaceVariantDark = Color(0xFFC2C5CB)
    val outlineDark = Color(0xFF8C9098)
    val outlineVariantDark = Color(0xFF2C2F35)
    val scrimDark = Color(0xFF000000)
    val inverseSurfaceDark = Color(0xFFE3E5E9)
    val inverseOnSurfaceDark = Color(0xFF2C2F34)
    val inversePrimaryDark = Color(0xFF17191E)
    val surfaceDimDark = Color(0xFF15171B)
    val surfaceBrightDark = Color(0xFF3B3E44)
    val surfaceContainerLowestDark = Color(0xFF101215)
    val surfaceContainerLowDark = Color(0xFF1D2024)
    val surfaceContainerDark = Color(0xFF23262B)
    val surfaceContainerHighDark = Color(0xFF2B2E34)
    val surfaceContainerHighestDark = Color(0xFF343740)
}

// ============================================================
// 位图主题的扩展语义色
// ============================================================

// 强调色只用橙：aiAccent / star 都落橙系；success/warning/error 保留功能色但压平饱和度。
internal val BitmapLightExtended = ExtendedColors(
    success = Color(0xFF3D6B2F),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFDCE9D2),
    warning = Color(0xFF9A6A00),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFF2E3C0),
    aiAccent = Color(0xFFA65200),
    aiContainer = Color(0xFFFFE9D1),
    onAiContainer = Color(0xFF3A1D00),
    star = Color(0xFFA65200),
)

internal val BitmapDarkExtended = ExtendedColors(
    success = Color(0xFF9DCB8B),
    onSuccess = Color(0xFF17300F),
    successContainer = Color(0xFF2C4A22),
    warning = Color(0xFFE8C173),
    onWarning = Color(0xFF3F2D00),
    warningContainer = Color(0xFF57430F),
    aiAccent = Color(0xFFFF9E57),
    aiContainer = Color(0xFF573520),
    onAiContainer = Color(0xFFFFE3CC),
    star = Color(0xFFFF9E57),
)

// ============================================================
// 位图主题的标签色板（6 个复古老色，键名与 [LightTagColors] 一致）
// ============================================================
internal val BitmapLightTagColors = mapOf(
    "neutral" to Color(0xFF7A7E87),
    "rose" to Color(0xFFB5534A),
    "sage" to Color(0xFF628A44),
    "mauve" to Color(0xFF8A5F86),
    "clay" to Color(0xFFC06A2C),
    "slate" to Color(0xFF5578A0)
)

internal val BitmapDarkTagColors = mapOf(
    "neutral" to Color(0xFFA5A9B1),
    "rose" to Color(0xFFD98A81),
    "sage" to Color(0xFF97B87E),
    "mauve" to Color(0xFFBB90B7),
    "clay" to Color(0xFFE09259),
    "slate" to Color(0xFF8AA6C4)
)
