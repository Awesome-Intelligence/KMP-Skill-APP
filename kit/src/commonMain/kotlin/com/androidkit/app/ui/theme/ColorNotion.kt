package com.androidkit.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Notion 风格色板 —— 米色纸面 + 暖墨字 + 1px 发丝线，零渐变、零重阴影。
 *
 * 规范（StyleKit notion-style）给的是纯浅色：
 * - 纸面 `#f7f6f3`（画布），白页 `#ffffff`（正文/弹层）
 * - 墨 `#37352f`（正文与图标）—— 暖调，纯黑会把纸面弄脏
 * - 悬停灰 `#efedea`，按下灰 `#e3e1db`（比悬停再深一档）
 * - 发丝线 `#e9e7e4`，链接蓝 `#2eaadc`，标记红 `#eb5757`
 *
 * **深色版是自建的语义反转**（规范本身没有深色契约，
 * readiness.darkMode = missing，"先造语义深色 token 再上深色"）：
 * 纸面换 `#191918`，墨换 `#e9e7e4`，悬停/按下在深色下是「比底更亮」而非更暗。
 *
 * 槽位映射的关键取舍：M3 的 surfaceContainer* 色阶在这里不是「高度」，而是
 * 悬停灰 / 按下灰 —— 文档工具没有浮层层次，层次只由「底色离纸面多远」表达。
 */
object Notion {
    // ── Light：米色纸面 ─────────────────────────────────────────────────────
    val primaryLight = Color(0xFF37352F)          // 墨：主色就是正文色
    val onPrimaryLight = Color(0xFFFFFFFF)
    val primaryContainerLight = Color(0xFFEFEDEA)  // 悬停灰
    val onPrimaryContainerLight = Color(0xFF37352F)
    val secondaryLight = Color(0xFF6B6A67)        // 次要文字（对纸面 ≈5.4:1）
    val onSecondaryLight = Color(0xFFFFFFFF)
    val secondaryContainerLight = Color(0xFFF1F0EC)
    val onSecondaryContainerLight = Color(0xFF37352F)
    val tertiaryLight = Color(0xFF2EAADC)          // 链接蓝：唯一「往前走」的颜色
    val onTertiaryLight = Color(0xFF0D2530)        // 蓝底上的深色前景（白字不够）
    val tertiaryContainerLight = Color(0xFFDBEEF8)
    val onTertiaryContainerLight = Color(0xFF10485F)
    val errorLight = Color(0xFFEB5757)             // 标记红
    val onErrorLight = Color(0xFF2B0A0A)
    val errorContainerLight = Color(0xFFFBE4E4)
    val onErrorContainerLight = Color(0xFF6B1D1D)
    val backgroundLight = Color(0xFFF7F6F3)       // 纸面
    val onBackgroundLight = Color(0xFF37352F)
    val surfaceLight = Color(0xFFFFFFFF)          // 白页
    val onSurfaceLight = Color(0xFF37352F)
    val surfaceVariantLight = Color(0xFFEFEDEA)    // 悬停灰
    val onSurfaceVariantLight = Color(0xFF6B6A67)
    val outlineLight = Color(0xFF9B9A97)
    val outlineVariantLight = Color(0xFFE9E7E4)    // 发丝线
    val scrimLight = Color(0xFF000000)
    val inverseSurfaceLight = Color(0xFF37352F)
    val inverseOnSurfaceLight = Color(0xFFFFFFFF)
    val inversePrimaryLight = Color(0xFFFFFFFF)
    val surfaceTintLight = Color(0xFFE9E7E4)      // 不做品牌染色，层次靠色阶
    val surfaceDimLight = Color(0xFFE6E4E0)
    val surfaceBrightLight = Color(0xFFFFFFFF)
    val surfaceContainerLowestLight = Color(0xFFFFFFFF)
    val surfaceContainerLowLight = Color(0xFFFBFAF8)
    val surfaceContainerLight = Color(0xFFF7F6F3)
    val surfaceContainerHighLight = Color(0xFFEFEDEA)   // 悬停
    val surfaceContainerHighestLight = Color(0xFFE3E1DB) // 按下

    // ── Dark：自建语义反转 ─────────────────────────────────────────────────
    val errorDark = Color(0xFFEB5757)
    val onErrorDark = Color(0xFF2B0A0A)
    val errorContainerDark = Color(0xFF4A1F1F)
    val onErrorContainerDark = Color(0xFFFFD9D9)
    val primaryDark = Color(0xFFE9E7E4)
    val onPrimaryDark = Color(0xFF191918)
    val primaryContainerDark = Color(0xFF3A3936)
    val onPrimaryContainerDark = Color(0xFFF0EFEC)
    val secondaryDark = Color(0xFFA8A6A3)
    val onSecondaryDark = Color(0xFF191918)
    val secondaryContainerDark = Color(0xFF333230)
    val onSecondaryContainerDark = Color(0xFFE9E7E4)
    val tertiaryDark = Color(0xFF2EAADC)
    val onTertiaryDark = Color(0xFF0D2530)
    val tertiaryContainerDark = Color(0xFF163C4E)
    val onTertiaryContainerDark = Color(0xFFA8DCF5)
    val backgroundDark = Color(0xFF191918)        // 深色纸面
    val onBackgroundDark = Color(0xFFE9E7E4)
    val surfaceDark = Color(0xFF202020)
    val onSurfaceDark = Color(0xFFE9E7E4)
    val surfaceVariantDark = Color(0xFF2F2E2C)     // 悬停：比底更亮
    val onSurfaceVariantDark = Color(0xFFA8A6A3)
    val outlineDark = Color(0xFF7C7A77)
    val outlineVariantDark = Color(0xFF3A3936)
    val scrimDark = Color(0xFF000000)
    val inverseSurfaceDark = Color(0xFFE9E7E4)
    val inverseOnSurfaceDark = Color(0xFF191918)
    val inversePrimaryDark = Color(0xFF191918)
    val surfaceTintDark = Color(0xFF3A3936)
    val surfaceDimDark = Color(0xFF131211)
    val surfaceBrightDark = Color(0xFF2A2928)
    val surfaceContainerLowestDark = Color(0xFF131211)
    val surfaceContainerLowDark = Color(0xFF1F1E1D)
    val surfaceContainerDark = Color(0xFF252423)
    val surfaceContainerHighDark = Color(0xFF2F2E2C)   // 悬停
    val surfaceContainerHighestDark = Color(0xFF3A3936) // 按下
}

// ============================================================
// Notion 主题的扩展语义色
// ============================================================

/**
 * 绿/黄是规范给的强调色，但原色对纸面不够当文字色：
 * `#0f7b6c` 直接当 success 文字没问题，`#dfab01` 只有 1.9:1 —— 文字位改用 `#8a6800`。
 */
internal val NotionLightExtended = ExtendedColors(
    success = Color(0xFF0F7B6C),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFD7EBE7),
    warning = Color(0xFF8A6800),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFF7EDD0),
    // 紫罗兰取自规范强调色板（#a083ff），但那色对白底只有 3.4:1，文字位压深
    aiAccent = Color(0xFF6D4AD8),
    aiContainer = Color(0xFFEFE9FB),
    onAiContainer = Color(0xFF26174F),
    star = Color(0xFF8A6800),
)

internal val NotionDarkExtended = ExtendedColors(
    success = Color(0xFF4FBFAE),
    onSuccess = Color(0xFF00382F),
    successContainer = Color(0xFF1C4A42),
    warning = Color(0xFFE0B64B),
    onWarning = Color(0xFF3A2B00),
    warningContainer = Color(0xFF4A3A0C),
    aiAccent = Color(0xFFB39CF0),
    aiContainer = Color(0xFF322A52),
    onAiContainer = Color(0xFFE6DCFF),
    star = Color(0xFFE0B64B),
)

// ============================================================
// Notion 主题的标签色板（键名与 [LightTagColors] 一致）
// ============================================================

internal val NotionLightTagColors = mapOf(
    "neutral" to Color(0xFF6B6A67),
    "rose" to Color(0xFFB04A44),
    "sage" to Color(0xFF0F7B6C),
    "mauve" to Color(0xFF6D4AD8),
    "clay" to Color(0xFFA06A2C),
    "slate" to Color(0xFF1B6F96)
)

internal val NotionDarkTagColors = mapOf(
    "neutral" to Color(0xFFA8A6A3),
    "rose" to Color(0xFFE08A84),
    "sage" to Color(0xFF4FBFAE),
    "mauve" to Color(0xFFB39CF0),
    "clay" to Color(0xFFD99A5B),
    "slate" to Color(0xFF6FB6DD)
)
