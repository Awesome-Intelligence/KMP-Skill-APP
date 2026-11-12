package com.androidkit.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * E-Ink（电子纸）主题色板 —— 暖纸底 + 墨色文字 + 三档灰阶，零色相。
 *
 * 规范（StyleKit e-ink）只给了浅色：
 * - 纸色 `#f5f3ea` 作页面底与输入框填充
 * - 墨色 `#20211e` 作正文、标题、主操作、焦点轮廓
 * - 石墨灰 `#555750` 作次级文字、图表线、短标签
 * - 栏线灰 `#bab9b0` 作卡片边框与分隔线（**不做正文色**，对纸底只有 1.8:1）
 * - 阅读面 `#e6e4da` 作列表分组、表格行、选中的阅读卡
 *
 * 规范明确「错误/警告不另发明新色相，状态靠词 + 形状」，
 * 所以 [errorLight] 也是墨色 —— 状态由形状和文字扛，不靠色相。
 *
 * **深色是自建的语义反转**（墨底 + 纸字）：规范没给深色契约
 * （readiness.darkMode = missing，「先造语义深色 token 再上深色」）。
 * 反转时守住「正文对比度 ≥4.5:1」「正文与图形不靠色相区分」两条纪律。
 *
 * ponytail: 想要整体调明暗改整张表重跑对比度校验，别单改某个 hex ——
 * 色阶一致性比单项好看重要（同 Mono 的约定）。
 */
object EInk {
    /** 纸色：页面底、输入框填充 */
    val paper = Color(0xFFF5F3EA)

    /** 墨色：正文、标题、主操作、焦点轮廓 */
    val ink = Color(0xFF20211E)

    /** 石墨灰：次级文字、图表线、短标签 */
    val graphite = Color(0xFF555750)

    /** 栏线灰：卡片边框、分隔线（非文字） */
    val rule = Color(0xFFBAB9B0)

    /** 阅读面：列表分组、表格行、选中的阅读卡 */
    val panel = Color(0xFFE6E4DA)

    // ── Light：纸底 ─────────────────────────────────────────────────────
    val primaryLight = ink
    val onPrimaryLight = paper
    val primaryContainerLight = panel
    val onPrimaryContainerLight = ink
    val secondaryLight = graphite
    val onSecondaryLight = paper
    val secondaryContainerLight = panel
    val onSecondaryContainerLight = ink
    val tertiaryLight = graphite
    val onTertiaryLight = paper
    val tertiaryContainerLight = Color(0xFFEDEBE2)
    val onTertiaryContainerLight = ink
    // 状态不发明新色相：错误也是墨色，靠形状 + 文字区分
    val errorLight = ink
    val onErrorLight = paper
    val errorContainerLight = panel
    val onErrorContainerLight = ink
    val backgroundLight = paper
    val onBackgroundLight = ink
    val surfaceLight = paper
    val onSurfaceLight = ink
    val surfaceVariantLight = panel
    val onSurfaceVariantLight = graphite
    // outline = 石墨灰（对纸底 6.6:1，够图形对比度）；栏线灰放 outlineVariant
    val outlineLight = graphite
    val outlineVariantLight = rule
    val scrimLight = ink
    val inverseSurfaceLight = ink
    val inverseOnSurfaceLight = paper
    val inversePrimaryLight = paper
    // 画布不吃色调 —— tint 取纸色 = 不着色（同 Swiss 的做法）
    val surfaceTintLight = paper
    val surfaceDimLight = panel
    val surfaceBrightLight = Color(0xFFFAF9F2)
    val surfaceContainerLowestLight = Color(0xFFFBFAF3)
    val surfaceContainerLowLight = Color(0xFFF0EEE4)
    val surfaceContainerLight = Color(0xFFEAE8DE)
    val surfaceContainerHighLight = panel
    val surfaceContainerHighestLight = Color(0xFFDBD9CD)

    // ── Dark：墨底（自建反转） ───────────────────────────────────────────
    val primaryDark = paper
    val onPrimaryDark = ink
    val primaryContainerDark = Color(0xFF34332E)
    val onPrimaryContainerDark = paper
    val secondaryDark = rule
    val onSecondaryDark = ink
    val secondaryContainerDark = Color(0xFF2C2B26)
    val onSecondaryContainerDark = panel
    val tertiaryDark = rule
    val onTertiaryDark = ink
    val tertiaryContainerDark = Color(0xFF34332E)
    val onTertiaryContainerDark = panel
    val errorDark = paper
    val onErrorDark = ink
    val errorContainerDark = Color(0xFF34332E)
    val onErrorContainerDark = paper
    val backgroundDark = ink
    val onBackgroundDark = paper
    val surfaceDark = ink
    val onSurfaceDark = paper
    val surfaceVariantDark = Color(0xFF2C2B26)
    val onSurfaceVariantDark = rule
    val outlineDark = rule
    val outlineVariantDark = Color(0xFF3A3833)
    val scrimDark = Color(0xFF000000)
    val inverseSurfaceDark = paper
    val inverseOnSurfaceDark = ink
    val inversePrimaryDark = ink
    val surfaceTintDark = ink
    val surfaceDimDark = Color(0xFF171614)
    val surfaceBrightDark = Color(0xFF34332E)
    val surfaceContainerLowestDark = Color(0xFF171614)
    val surfaceContainerLowDark = Color(0xFF1E1D1A)
    val surfaceContainerDark = Color(0xFF262522)
    val surfaceContainerHighDark = Color(0xFF2F2E2A)
    val surfaceContainerHighestDark = Color(0xFF3A3833)
}

// ============================================================
// 电子纸主题的扩展语义色
// ============================================================

// 状态一律灰阶：success 最浅 → warning 中 → error 最深（error 由 colorScheme.error 承担）。
// 深浅不一靠**明度差**而不是色相，浅底上全部 ≥4.5:1。
internal val EInkLightExtended = ExtendedColors(
    success = Color(0xFF6E6D64),
    onSuccess = Color(0xFF20211E),
    successContainer = Color(0xFFE6E4DA),
    warning = Color(0xFF45463F),
    onWarning = Color(0xFFF5F3EA),
    warningContainer = Color(0xFFEDEBE2),
    // AI 强调 = 墨色（灰阶主题里没有品牌色位）
    aiAccent = Color(0xFF20211E),
    aiContainer = Color(0xFFE6E4DA),
    onAiContainer = Color(0xFF20211E),
    star = Color(0xFF555750),
)

internal val EInkDarkExtended = ExtendedColors(
    success = Color(0xFFA9A79C),
    onSuccess = Color(0xFF20211E),
    successContainer = Color(0xFF34332E),
    warning = Color(0xFFE8E6DB),
    onWarning = Color(0xFF20211E),
    warningContainer = Color(0xFF45443D),
    aiAccent = Color(0xFFF5F3EA),
    aiContainer = Color(0xFF34332E),
    onAiContainer = Color(0xFFF5F3EA),
    star = Color(0xFFBAB9B0),
)

// ============================================================
// 电子纸主题的标签色板（6 档灰阶，键名与 [LightTagColors] 一致）
// ============================================================

// 标签靠文字认人，颜色只是灰阶底纹 —— 六档明度递降，最浅一档对纸底仍 ≥3:1。
internal val EInkLightTagColors = mapOf(
    "neutral" to Color(0xFF555750),
    "rose" to Color(0xFF5E5F57),
    "sage" to Color(0xFF67685F),
    "mauve" to Color(0xFF707167),
    "clay" to Color(0xFF797A6F),
    "slate" to Color(0xFF828377)
)

internal val EInkDarkTagColors = mapOf(
    "neutral" to Color(0xFFBAB9B0),
    "rose" to Color(0xFFADACA2),
    "sage" to Color(0xFFA0A096),
    "mauve" to Color(0xFF939389),
    "clay" to Color(0xFF86857C),
    "slate" to Color(0xFF797870)
)
