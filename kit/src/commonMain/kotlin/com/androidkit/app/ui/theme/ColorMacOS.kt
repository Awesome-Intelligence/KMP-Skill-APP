package com.androidkit.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * macOS 毛玻璃（vibrancy）主题色板 —— 三档灰 + 系统蓝，零渐变、零发光、1px 分隔。
 *
 * 规范（StyleKit macos-vibrancy）**只给了深色契约**（`theme: dark`）：
 * - 最深灰 `#1c1c1e`（窗口底 / 侧边栏底）
 * - 中灰 `#2c2c2e`（内容区 / 列表行）
 * - 浅灰 `#3a3a3c`（工具栏 / 浮动面板）
 * - 系统蓝 `#0a84ff`（唯一的强调色）、绿 `#30d158`、橙 `#ff9f0a`、红 `#ff453a`
 * - 毛玻璃层 `rgba(28,28,30,0.8)`（配 backdrop blur）
 *
 * **浅色是自建的**（规范 readiness.darkMode = missing，风格本身是暗色原生）：
 * 按 macOS 浅色系统的实际做法做的 —— 窗底 `#f2f2f7`、白内容面、
 * 强调蓝换成浅色模式的 `#007aff`，层次靠色阶而不是边框。
 *
 * 层次纪律：**不用阴影制造深度**，全靠背景深浅（`#1c1c1e → #2c2c2e → #3a3a3c`）。
 * 分隔一律 1px 半透明白（8% / 12%），所以 outline 槽位给的是带 alpha 的白，
 * 画在任何底色上都自然合成 —— 这是这套风格唯一允许的「半透明」。
 */
object MacOS {
    // ── Dark：规范原生（三档灰 + 系统色） ────────────────────────────────
    val primaryDark = Color(0xFF0A84FF)             // 系统蓝：唯一强调色
    val onPrimaryDark = Color(0xFFFFFFFF)
    val primaryContainerDark = Color(0xFF263C54)    // 蓝 18% 叠在中灰上
    val onPrimaryContainerDark = Color(0xFFA9D2FF)
    val secondaryDark = Color(0xFFAEAEAE)           // 次级文字（白 70%）
    val onSecondaryDark = Color(0xFF1C1C1E)
    val secondaryContainerDark = Color(0xFF3A3A3C)  // 工具栏 / 浮动面板灰
    val onSecondaryContainerDark = Color(0xFFE5E5EA)
    val tertiaryDark = Color(0xFF30D158)            // 系统绿
    val onTertiaryDark = Color(0xFF06210F)
    val tertiaryContainerDark = Color(0xFF1C3D26)
    val onTertiaryContainerDark = Color(0xFFA6F0BE)
    val errorDark = Color(0xFFFF453A)               // 系统红：破坏性操作
    val onErrorDark = Color(0xFF2B0A05)
    val errorContainerDark = Color(0xFF4A1A16)
    val onErrorContainerDark = Color(0xFFFFD9D4)
    val backgroundDark = Color(0xFF1C1C1E)          // 窗口底（最深灰）
    val onBackgroundDark = Color(0xFFF2F2F7)         // 白 95%
    val surfaceDark = Color(0xFF2C2C2E)             // 内容区（中灰）
    val onSurfaceDark = Color(0xFFF2F2F7)
    val surfaceVariantDark = Color(0xFF3A3A3C)      // 悬停 = 白 8% 叠中灰
    val onSurfaceVariantDark = Color(0xFFAEAEAE)
    val outlineDark = Color(0x1FFFFFFF)             // 白 12%：聚焦环 / 强分隔
    val outlineVariantDark = Color(0x14FFFFFF)      // 白 8%：常规分隔
    val scrimDark = Color(0xFF000000)
    val inverseSurfaceDark = Color(0xFFF2F2F7)
    val inverseOnSurfaceDark = Color(0xFF1C1C1E)
    val inversePrimaryDark = Color(0xFF0A84FF)
    val surfaceTintDark = Color(0xFF0A84FF)
    val surfaceDimDark = Color(0xFF161618)
    val surfaceBrightDark = Color(0xFF3A3A3C)
    val surfaceContainerLowestDark = Color(0xFF1C1C1E)
    val surfaceContainerLowDark = Color(0xFF232325)
    val surfaceContainerDark = Color(0xFF2C2C2E)
    val surfaceContainerHighDark = Color(0xFF333336)
    val surfaceContainerHighestDark = Color(0xFF3A3A3C)

    // ── Light：自建（macOS 浅色系统） ──────────────────────────────────────
    // 蓝/绿都压深了一档：macOS 浅色模式的 #007aff 在白底只有 4.0:1，
    // 做 13px 按钮文字不够 AA —— 文字位用 #0060d6（5.8:1），观感几乎一样。
    val primaryLight = Color(0xFF0060D6)
    val onPrimaryLight = Color(0xFFFFFFFF)
    val primaryContainerLight = Color(0xFFDCEBFF)
    val onPrimaryContainerLight = Color(0xFF003A73)
    val secondaryLight = Color(0xFF6E6E73)
    val onSecondaryLight = Color(0xFFFFFFFF)
    val secondaryContainerLight = Color(0xFFE9E9EE)
    val onSecondaryContainerLight = Color(0xFF1C1C1E)
    val tertiaryLight = Color(0xFF1E7A34)           // 绿压深过 AA
    val onTertiaryLight = Color(0xFFFFFFFF)
    val tertiaryContainerLight = Color(0xFFDFF3E6)
    val onTertiaryContainerLight = Color(0xFF0B3D1C)
    val errorLight = Color(0xFFD70015)
    val onErrorLight = Color(0xFFFFFFFF)
    val errorContainerLight = Color(0xFFFFE5E3)
    val onErrorContainerLight = Color(0xFF7A0B05)
    val backgroundLight = Color(0xFFF2F2F7)          // 窗底
    val onBackgroundLight = Color(0xFF1C1C1E)
    val surfaceLight = Color(0xFFFFFFFF)             // 内容面：白
    val onSurfaceLight = Color(0xFF1C1C1E)
    val surfaceVariantLight = Color(0xFFEDEDF0)     // 悬停
    val onSurfaceVariantLight = Color(0xFF6E6E73)
    val outlineLight = Color(0x1F000000)             // 黑 12%
    val outlineVariantLight = Color(0x14000000)      // 黑 8%
    val scrimLight = Color(0xFF000000)
    val inverseSurfaceLight = Color(0xFF1C1C1E)
    val inverseOnSurfaceLight = Color(0xFFF2F2F7)
    val inversePrimaryLight = Color(0xFF007AFF)
    val surfaceTintLight = Color(0xFF007AFF)
    val surfaceDimLight = Color(0xFFE0E0E4)
    val surfaceBrightLight = Color(0xFFFFFFFF)
    val surfaceContainerLowestLight = Color(0xFFFFFFFF)
    val surfaceContainerLowLight = Color(0xFFFAFAFC)
    val surfaceContainerLight = Color(0xFFF2F2F7)
    val surfaceContainerHighLight = Color(0xFFEDEDF0)
    val surfaceContainerHighestLight = Color(0xFFE6E6EA)

    /**
     * 毛玻璃层 —— 80% 不透明的最深灰。
     *
     * ponytail: 规范写的是 `rgba(28,28,30,0.8)` + `backdrop-filter: blur(40px)`。
     * Compose Multiplatform **没有 backdrop-filter**（`Modifier.blur` 模糊的是节点自身内容，
     * 不是背后的东西），所以这里只保留半透明 + 1px 白边，靠底下的内容透出来近似毛玻璃；
     * 真要背景模糊得走各端原生（Android RenderEffect / iOS UIVisualEffectView）。
     */
    val vibrancyDark = Color(0xCC1C1C1E)
    val vibrancyLight = Color(0xCCF7F7F9)

    /** 选中态：系统蓝 18% 叠在当前底色上（规范原话「selected: blue 18%」）。 */
    val selectionDark = Color(0x2E0A84FF)
    val selectionLight = Color(0x2E007AFF)
}

// ============================================================
// macOS 主题的扩展语义色
// ============================================================

internal val MacOSDarkExtended = ExtendedColors(
    success = Color(0xFF30D158),
    onSuccess = Color(0xFF06210F),
    successContainer = Color(0xFF1C3D26),
    warning = Color(0xFFFF9F0A),
    onWarning = Color(0xFF2B1A00),
    warningContainer = Color(0xFF4A3208),
    aiAccent = Color(0xFF0A84FF),
    aiContainer = Color(0xFF16324B),
    onAiContainer = Color(0xFFA9D2FF),
    star = Color(0xFFFFD60A),   // macOS 浅黄（收藏星标）
)

internal val MacOSLightExtended = ExtendedColors(
    success = Color(0xFF1B7F3B),  // 浅色模式的绿只有 1.9:1，文字位压深
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFDFF3E6),
    warning = Color(0xFFA8620A),  // 橙同理
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFF0D6),
    aiAccent = Color(0xFF007AFF),
    aiContainer = Color(0xFFDCEBFF),
    onAiContainer = Color(0xFF003A73),
    star = Color(0xFF8A6D00),     // 黄在白底读不出来，文字位用深琥珀
)

// ============================================================
// macOS 主题的标签色板（键名与 [LightTagColors] 一致）
// 用的是 macOS 系统色板，深浅两套各取其标准值
// ============================================================

internal val MacOSDarkTagColors = mapOf(
    "neutral" to Color(0xFFAEAEAE),
    "rose" to Color(0xFFFF6961),
    "sage" to Color(0xFF30D158),
    "mauve" to Color(0xFFBF5AF2),
    "clay" to Color(0xFFFF9F0A),
    "slate" to Color(0xFF64D2FF)
)

internal val MacOSLightTagColors = mapOf(
    "neutral" to Color(0xFF6E6E73),
    "rose" to Color(0xFFD70015),
    "sage" to Color(0xFF1E7A34),
    "mauve" to Color(0xFF8944AB),
    "clay" to Color(0xFFC93400),
    "slate" to Color(0xFF0071E3)
)
