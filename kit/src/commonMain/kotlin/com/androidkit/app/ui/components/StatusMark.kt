package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

/**
 * 状态形状标记 —— 实心 / 空心方块。
 *
 * 规范（电子纸）：「圆点不是唯一的状态载体，错误和警告不另发明新色相」。
 * 零色相主题里颜色扛不住状态，**形状 + 文字**才扛得住 —— 这个方块就是那个形状。
 *
 * 不叫 EInkxxx：它是通用的形状标记，灰阶之外的主题也能用。
 */
@Composable
fun StatusMark(
    filled: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Box(
        modifier
            .size(size)
            .then(
                if (filled) {
                    Modifier.background(color)
                } else {
                    Modifier.border(1.dp, color, RectangleShape)
                }
            )
    )
}
