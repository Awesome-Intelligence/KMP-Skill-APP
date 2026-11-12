package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.LocalIsDarkTheme
import com.androidkit.app.ui.theme.tagColor

/**
 * App ListItem - M3 ListItem 封装
 * 规格：
 * - leading: 40dp 圆形色块（重要性指示），或 9dp 圆点（当 leadingBadgeIsDot = true）
 * - headline: titleSmall, maxLines 1, ellipsis
 * - supporting: bodySmall, onSurfaceVariant, maxLines 1
 * - trailing: 自定义尾随内容
 * - tags: 标签列表，以 TagText 渲染（FlowRow），与详情页一致
 * - 触控目标 ≥ 48dp
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppListItem(
    headlineText: String,
    supportingText: String? = null,
    leadingBadge: Int? = null,
    leadingBadgeColor: Color? = null,
    leadingBadgeLabel: String? = null,
    leadingBadgeIsDot: Boolean = false,
    avatarUri: String? = null,
    trailingText: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    progress: Float? = null,
    tags: List<String> = emptyList(),
    tagColors: Map<String, String> = emptyMap(),
    // 外层已用 AppCard/onClick 承载点击时传 null，避免嵌套两层 ripple
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // M3 规范：进度条使用 primary 颜色，轨道使用 surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingBadgeIsDot && leadingBadgeColor != null && leadingBadgeLabel != null) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(leadingBadgeColor)
                            .semantics { contentDescription = leadingBadgeLabel }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = headlineText,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        supportingContent = {
            Column {
                // 支持文本
                if (supportingText != null) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                // M3 LinearProgressIndicator - 细长进度条
                if (progress != null) {
                    LinearProgressIndicator(
                        progress = { progress.toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(4.dp),
                        color = progressColor,
                        trackColor = trackColor,
                        strokeCap = StrokeCap.Round
                    )
                }
                // 标签（与详情页 TagText 一致）
                if (tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tags.take(3).forEach { tag ->
                            TagText(
                                text = tag,
                                color = tagColor(tagColors[tag] ?: "primary"),
                                showSelectedIcon = false
                            )
                        }
                    }
                }
            }
        },
        leadingContent = when {
            avatarUri != null -> {
                {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(leadingBadgeColor ?: MaterialTheme.colorScheme.primaryContainer)
                            .semantics { contentDescription = headlineText },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = headlineText,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            leadingBadge != null || leadingBadgeLabel != null -> {
                {
                    val color = leadingBadgeColor ?: MaterialTheme.colorScheme.primary
                    val label = leadingBadgeLabel ?: leadingBadge?.toString() ?: ""
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .semantics { contentDescription = label },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }
            else -> null
        },
        trailingContent = trailingContent ?: trailingText?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    )
}

/**
 * Add List Item - 列表底部的添加入口
 * 与列表条目同风格（AppCard），leading 为 40dp primary 圆形底 Add 图标，文字 primary 色
 * 触控目标 ≥ 48dp
 */
@Composable
fun AddListItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        contentPadding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = if (LocalIsDarkTheme.current) 0.10f else 0.05f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun AppListItemPreview() {
    AppTheme {
        AppListItem(
            headlineText = DemoData.Text.READ_30_MIN_TASK,
            supportingText = DemoData.Text.FIVE_TIMES_WEEK_12_DAYS,
            leadingBadge = DemoData.Components.navGoals.badgeCount,
            progress = DemoData.Home.favoriteQuarterly.progress,
            tags = listOf(DemoData.Text.HEALTH, DemoData.Text.STUDY), // ponytail: demo data only
            trailingText = DemoData.Value.PERCENT_60
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun AddListItemPreview() {
    AppTheme {
        AddListItem(text = DemoData.Text.ADD_GOAL, onClick = {})
    }
}
