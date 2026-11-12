package com.androidkit.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign

/**
 * 主题化文本对齐 —— 瑞士风格**一律左对齐，绝不居中**。
 *
 * 规范：「Flush left everywhere, never centred」「Never centre typography」。
 * 组件里写 `textAlign = TextAlign.Center` 时套一层：瑞士主题下改成 Start。
 */
@Composable
fun themedTextAlign(centered: TextAlign): TextAlign =
    if (LocalThemeStyle.current == ThemeStyle.Swiss) TextAlign.Start else centered

/**
 * 主题化横向对齐 —— 瑞士风格一律左对齐，绝不居中。
 *
 * 容器（Column / Row）也走这个：文字左对齐了、容器还居中，视觉上照样是"居中版面"。
 */
@Composable
fun themedHorizontalAlignment(centered: Alignment.Horizontal): Alignment.Horizontal =
    if (LocalThemeStyle.current == ThemeStyle.Swiss) Alignment.Start else centered
