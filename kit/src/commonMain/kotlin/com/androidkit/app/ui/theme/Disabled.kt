package com.androidkit.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 禁用态文字色。
 *
 * 瑞士规范的按钮禁用态要求「降对比但仍可读」——
 * M3 默认是 onSurface 38% 透明（约 2.4:1），读不了。
 * 瑞士主题改用 onSurfaceVariant（白底上 ≈8.9:1），降了对比但读得清。
 *
 * 其它风格沿用 M3 默认值。
 */
@Composable
fun themedDisabledContentColor(): Color =
    if (LocalThemeStyle.current == ThemeStyle.Swiss) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
