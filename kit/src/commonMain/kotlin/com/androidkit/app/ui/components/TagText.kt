package com.androidkit.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.androidkit.app.ui.theme.themedCorner
import com.androidkit.app.ui.theme.themedStrokeWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.themedFontWeight

/** 选中态描边圆角 —— 刻意用小圆角：大圆角空心描边读起来仍是胶囊，小圆角才是勾选框 */


/**
 * 标签文字 —— 全局唯一的标签渲染形式，不使用胶囊底色
 *
 * - 展示：`TagText(text, color)`
 * - 可点：`TagText(text, color, onClick = ...)`，自动补 32dp 最小触控高度（原本由胶囊高度提供）
 * - 已选中：`TagText(text, color, isSelected = true)` —— 加描边与图标
 *
 * [color] 始终是文字颜色，未选中要不要用灰色由调用方决定（筛选/选择场景传 onSurfaceVariant）。
 */
@Composable
fun TagText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    selectedIcon: ImageVector = Icons.Default.Check,
    /** 选中态是否显示尾部图标（picker 不需要，筛选需要） */
    showSelectedIcon: Boolean = true
) {
    Row(
        modifier = modifier
            .then(if (isSelected) Modifier.border(themedStrokeWidth(1.5.dp), color, themedCorner(6.dp)) else Modifier)
            .then(
                if (onClick != null) {
                    // ponytail: 32dp 而非 M3 的 48dp —— FlowRow 里 48dp 会把行距撑开
                    Modifier.heightIn(min = 32.dp).clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(
                horizontal = if (isSelected) 8.dp else 0.dp,
                vertical = if (isSelected) 4.dp else 0.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Box 包裹 prefix + 文字，使数字和字母视觉中心对齐
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "# $text",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = themedFontWeight(FontWeight.Medium),
                color = color
            )
        }
        if (isSelected && showSelectedIcon) {
            Icon(
                imageVector = selectedIcon,
                contentDescription = null, // ponytail: 装饰性图标，选中态已由描边传达，整行 clickable
                tint = color,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun TagTextPreview() {
    AppTheme {
        TagText(
            text = DemoData.Text.TAG,
            color = DemoData.Palette.primary,
            isSelected = true
        ) // ponytail: demo data only
    }
}
