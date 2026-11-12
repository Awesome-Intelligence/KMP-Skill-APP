package com.androidkit.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * macOS 毛玻璃排版 —— **三个字族各司其职**：衬线标题 + 无衬线正文 + 等宽代码。
 *
 * 规范给的替身：Georgia（标题）/ SF Pro Text（正文）/ SF Mono（代码）。
 * 这里用系统字族顶替：衬线 = `FontFamily.Serif`（桌面 Georgia / Android Noto Serif / iOS 衬线），
 * 正文 = `FontFamily.SansSerif`（SF Pro / Roboto），代码 = `FontFamily.Monospace`。
 *
 * 字号按**桌面密度**给，不是网页尺度：正文 15、辅助 13、标签 11，
 * 标题走衬线的 40 / 32 / 24 三个档。行高：display 1.15、标题 1.25、正文 1.5。
 * 字距：display −0.01em，大写标签 +0.02em。
 *
 * ponytail: Georgia 是规范点名要的衬线；系统衬线在 Android 上是 Noto Serif，
 * 观感接近但不是同一款字 —— 真要一致就得随包带字体文件（体积 + 授权）。
 */
val MacOSTypography = Typography(
    // ── 衬线：窗口标题与大标题（规范 40 / 32 / 24） ──────────────────
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp,          // 1.15
        letterSpacing = (-0.4).sp    // −0.01em
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,          // 1.25
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 30.sp,          // 1.25
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,          // 1.3
        letterSpacing = 0.sp
    ),
    // ── 无衬线：正文与控件 ────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,          // 1.3
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,          // 1.5
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,          // 1.5
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,          // 1.4
        letterSpacing = 0.22.sp      // +0.02em
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.22.sp
    )
)

/**
 * 代码 / 路径 / 数字 —— SF Mono 替身，12–13px。
 *
 * M3 的 Typography 没有「代码」槽位，所以单独给一个样式，由业务显式用。
 */
val MacOSCodeStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 21.sp,              // 1.6
    letterSpacing = 0.sp
)
