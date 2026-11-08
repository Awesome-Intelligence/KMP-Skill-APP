package com.androidkit.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.Font as ResourceFont
import com.androidkit.app.res.Res
import com.androidkit.app.res.fusion_pixel_12px_proportional_zh_hans

/**
 * 位图主题排版 — fusion-pixel 12px 缝合像素字体（OFL，中英全覆盖）。
 *
 * 字号全部锁在 12px 网格的整数倍 {12, 24, 36, 48, 60}：像素字体在非整倍字号下
 * 会被缩放得像素宽窄不一。正/标题统一 12sp（老游戏 UI 本来就是 12px 正文），
 * 层级靠字号和颜色区分，不靠字重 —— 像素字体没有粗体，假粗体会糊。
 *
 * ponytail: 想要更密的层级（16/20sp）就得接受半像素模糊，或等上游出多字重 —— 现状不动。
 */
@Composable
fun bitmapTypography(): Typography {
    // 像素字体只有 Regular 一个字重；全部显式 Normal，禁止合成粗细
    val family = FontFamily(ResourceFont(Res.font.fusion_pixel_12px_proportional_zh_hans, weight = FontWeight.Normal))

    fun style(size: Int, lineHeight: Int) = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = 0.sp
    )

    return Typography(
        displayLarge = style(60, 72),
        displayMedium = style(48, 60),
        displaySmall = style(36, 48),
        headlineLarge = style(36, 48),
        headlineMedium = style(36, 48),
        headlineSmall = style(24, 36),
        titleLarge = style(24, 36),
        titleMedium = style(12, 20),
        titleSmall = style(12, 20),
        bodyLarge = style(12, 20),
        bodyMedium = style(12, 20),
        bodySmall = style(12, 20),
        labelLarge = style(12, 20),
        labelMedium = style(12, 20),
        labelSmall = style(12, 20)
    )
}
