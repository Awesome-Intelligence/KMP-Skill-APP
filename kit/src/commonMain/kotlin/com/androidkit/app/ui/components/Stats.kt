package com.androidkit.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme

/**
 * Stat Item
 * 显示单个统计数据：图标 + 数值 + 标题
 */
@Composable
fun StatItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    iconContentDescription: String? = null
) {
    Row(
        modifier = modifier.height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconContentDescription,
            modifier = Modifier.size(16.dp),
            tint = color
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Stats Card - 统计卡片容器
 */
@Composable
fun StatsCard(
    stats: List<StatItemData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            stats.forEach { stat ->
                StatItem(
                    title = stat.title,
                    value = stat.value,
                    icon = stat.icon,
                    color = stat.color,
                    iconContentDescription = stat.iconContentDescription,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Stats data class
 */
data class StatItemData(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val iconContentDescription: String? = null
)

@Preview(showBackground = true)
@Composable
internal fun StatItemPreview() {
    AppTheme {
        StatItem(
            title = DemoData.Components.statInProgress.title,
            value = DemoData.Components.statInProgress.value,
            icon = DemoData.Components.statInProgress.icon,
            color = DemoData.Components.statInProgress.color
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun StatsCardPreview() {
    AppTheme {
        StatsCard(
            stats = DemoData.Components.stats // ponytail: demo data only
        )
    }
}
