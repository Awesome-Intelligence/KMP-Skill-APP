package com.androidkit.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================
// 应用扩展语义色（配合 Mono 主题使用）
// ============================================================

/**
 * 不在 Material 3 colorScheme 中的扩展语义色。
 * 通过 [LocalExtendedColors] 在 [AppTheme] 中注入，
 * 业务代码统一使用 [MaterialTheme.extendedColors] 读取。
 */
@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    /** AI 功能强调色（图标/小字）— 深浅色各自取值，保证 ≥4.5:1 */
    val aiAccent: Color,
    /** AI 容器背景（如 AI 建议条目、提示气泡） */
    val aiContainer: Color,
    /** aiContainer 上的前景色 */
    val onAiContainer: Color,
    /**
     * 收藏星标色（黄色系）。
     * M3 无「黄色」角色，收藏态属于语义强调，按扩展色处理；
     * 深浅色各自取值，保证对底色的非文字对比度 ≥3:1。
     */
    val star: Color,
)

internal val LightExtended = ExtendedColors(
    success = Color(0xFF2D6A4F),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFD8F3E4),
    warning = Color(0xFFB75D00),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFECCF),
    // purple-700 / purple-100 / purple-950
    aiAccent = Color(0xFF5E35B1),
    aiContainer = Color(0xFFEDE7F6),
    onAiContainer = Color(0xFF241436),
    // amber tone 60：浅底上仍是明确的黄，对 #FDFAF6 对比度 ≈3.4:1
    star = Color(0xFFC77800),
)

internal val DarkExtended = ExtendedColors(
    success = Color(0xFF6BCB96),
    onSuccess = Color(0xFF003822),
    successContainer = Color(0xFF1B4D35),
    warning = Color(0xFFFFB95C),
    onWarning = Color(0xFF462900),
    warningContainer = Color(0xFF6B3900),
    // purple-200 / purple-900 / purple-100
    aiAccent = Color(0xFFB39DDB),
    aiContainer = Color(0xFF3D2B6B),
    onAiContainer = Color(0xFFEDE7F6),
    // amber tone 80：深底上亮黄，对 #15110D 对比度 ≈13:1
    star = Color(0xFFFFD54F),
)

/**
 * CompositionLocal 提供扩展色，主题注入，预览默认 Light。
 */
val LocalExtendedColors = staticCompositionLocalOf { LightExtended }

/**
 * MaterialTheme 扩展，业务代码统一入口。
 *
 * 用法：
 * ```
 * val ext = MaterialTheme.extendedColors
 * Icon(tint = ext.success)
 * Box(background = ext.successContainer)
 * ```
 */
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current

// ============================================================
// 其他保留的语义/功能色
// ============================================================

/**
 * 优先级颜色 — 响应深色模式，对应 extendedColors warning/success/error
 */
fun importanceColor(importance: Importance): Color = when (importance) {
    Importance.LOW -> Color(0xFF6F6F6F)                          // 灰色，无对应语义色
    Importance.MEDIUM -> Color(0xFFB75D00)                        // Warning amber (浅色)
    Importance.HIGH -> Color(0xFFBA1A1A)                          // Error red
}

/**
 * Composable 版本，响应深色模式
 */
@Composable
fun MaterialTheme.importanceColor(importance: Importance): Color = when (importance) {
    Importance.LOW -> Color(0xFF6F6F6F)
    Importance.MEDIUM -> extendedColors.warning
    Importance.HIGH -> colorScheme.error
}

val AchievementGold = Color(0xFF8A7A50)   // 成就金：去饱和暖褐金，与单色主题协调

// Scrim（遮罩）— 纯黑不改
val Scrim = Color(0xFF000000)
