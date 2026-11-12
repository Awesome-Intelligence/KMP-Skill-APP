package com.androidkit.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

/**
 * 主题化字重 —— 瑞士风格只允许 400 和 700 两档字重。
 *
 * 组件里写 `fontWeight = FontWeight.Medium` 这类「中间档」时套一层：
 * 瑞士主题下 Medium / SemiBold 一律折成 Bold，Normal 保持 Normal。
 * 别的主题原样透传。
 *
 * 规范：「Do not exceed two weights.」
 */
@Composable
fun themedFontWeight(weight: FontWeight): FontWeight =
    if (LocalThemeStyle.current == ThemeStyle.Swiss && weight.weight > FontWeight.Normal.weight) {
        FontWeight.Bold
    } else {
        weight
    }
