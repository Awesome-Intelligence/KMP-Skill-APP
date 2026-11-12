package com.androidkit.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 圆角风格：品牌主题解耦了「像素风」之后，圆角由 tokens 单独决定。 */
enum class CornerStyle {
    /** 跟随传入的半径（Mono 默认）。 */
    Rounded,

    /** 全直角（Swiss / E-Ink 默认，品牌也可以选）。 */
    Square,
}

/** 当前圆角风格；由 [AppTheme] 按 [ThemeTokens.cornerStyle] 注入。 */
internal val LocalCornerStyle = staticCompositionLocalOf { CornerStyle.Rounded }

/**
 * 主题化圆角：组件只传「这处该用多大半径」，风格由主题决定。
 *
 * 组件里别写死 `RoundedCornerShape(x.dp)` —— 写死了就跟主题脱钩，
 * 换风格时那几处不变，看起来就是"半套主题"。
 *
 * 圆形（头像、指示点）不在此列：那是图形本身，不是风格。
 */
@Composable
fun themedCorner(radius: Dp): Shape =
    if (LocalCornerStyle.current == CornerStyle.Square) RectangleShape else RoundedCornerShape(radius)

/**
 * 控件圆角（按钮 / 输入框）。
 *
 * macOS 毛玻璃规范：按钮 6px、输入框 6px —— 比卡片（10px）小一号。
 * 其它风格沿用调用方给的默认。
 */
@Composable
fun themedControlShape(default: Shape): Shape =
    if (LocalThemeStyle.current == ThemeStyle.MacOS) RoundedCornerShape(6.dp) else default

/**
 * 描边宽度。
 *
 * macOS 毛玻璃规范：「边框永远 1px」—— 再粗就变成描边强调了。
 * 其它风格沿用调用方给的默认。
 */
@Composable
fun themedStrokeWidth(default: Dp): Dp =
    if (LocalThemeStyle.current == ThemeStyle.MacOS) 1.dp else default

/**
 * 装饰性圆形容器 —— 直角风格（瑞士 / 电子纸）下改成直角。
 *
 * 规范：「No border radius」，且 `rounded-full` 在 lint 禁用表里。
 * 头像/照片那种**图形本身**的圆形不走这里 —— 直接用 CircleShape。
 */
@Composable
fun themedCircleShape(): Shape =
    if (LocalCornerStyle.current == CornerStyle.Square) RectangleShape else CircleShape
