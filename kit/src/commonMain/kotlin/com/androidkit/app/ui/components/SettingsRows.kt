package com.androidkit.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme

/**
 * M3 设置行图标：24dp 图标，无容器底色
 */
@Composable
fun AppSettingsIcon(
    icon: ImageVector,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = modifier.size(24.dp),
        tint = contentColor
    )
}

/**
 * M3 列表行：最小高度 56dp（≥48dp 触控目标），标题 bodyLarge + 副文本 bodyMedium。
 *
 * 左侧 24dp 图标 + 16dp 间距，右侧可选箭头。
 * [contentPadding] 让同一组件既能放进"无内边距"的分组卡片（设置页），
 * 也能放进自带 16dp 内边距的卡片（我的页）。
 */
@Composable
fun AppSettingsRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    iconContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showChevron: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppSettingsIcon(
            icon = icon,
            contentColor = iconContentColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (showChevron) {
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 图标 + 文案的入口块（"我的"页的功能入口）。
 *
 * 24dp 主色图标 + labelSmall 文案，整块可点。
 */
@Composable
fun AppIconTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun AppSettingsIconPreview() {
    AppTheme {
        AppSettingsIcon(
            icon = Icons.Outlined.Settings,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun AppSettingsRowPreview() {
    AppTheme {
        AppSettingsRow(
            title = DemoData.Text.SETTING_ITEM,
            icon = Icons.Outlined.Settings,
            onClick = {},
            subtitle = DemoData.Text.SUBTITLE_TEXT
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun AppIconTilePreview() {
    AppTheme {
        AppIconTile(
            icon = Icons.Outlined.Settings,
            label = DemoData.Text.FEATURE_ENTRY,
            onClick = {}
        ) // ponytail: demo data only
    }
}
