package com.androidkit.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme

/**
 * 活跃筛选条件的数据类
 *
 * @param id 唯一标识
 * @param type 筛选类型
 * @param label 显示文本
 * @param color 可选的颜色，用于标签筛选
 */
data class ActiveFilter(
    val id: String,
    val type: FilterType,
    val label: String,
    val color: Color? = null
)

/**
 * 筛选类型枚举
 */
enum class FilterType {
    IMPORTANCE,
    TAG,
    TYPE
}

/**
 * 活跃筛选条件展示栏
 *
 * 显示当前应用的筛选条件，包括重要性筛选和标签筛选。
 * 支持横向滚动和清除全部筛选功能。
 *
 * @param filters 活跃筛选列表
 * @param onRemoveFilter 移除单个筛选的回调
 * @param onClearAll 清除全部筛选的回调
 * @param modifier 修饰符
 */
@Composable
fun ActiveFiltersBar(
    filters: List<ActiveFilter>,
    onRemoveFilter: (ActiveFilter) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (filters.isEmpty()) return

    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            filters.forEach { filter ->
                FilterChipItem(
                    filter = filter,
                    onRemove = { onRemoveFilter(filter) }
                )
            }
        }

        // 清除全部按钮
        if (filters.isNotEmpty()) {
            Text(
                text = stringResource(Res.string.clear_all),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
            IconButton(
                onClick = onClearAll,
                modifier = Modifier
                    .width(24.dp)
                    .height(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.clear_all_filters),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 单个筛选 Chip 项目
 * 标签筛选走 [TagText]（纯文字 + 描边），其余类型保留 FilterChip
 */
@Composable
private fun FilterChipItem(
    filter: ActiveFilter,
    onRemove: () -> Unit
) {
    if (filter.type == FilterType.TAG) {
        TagText(
            text = filter.label,
            color = filter.color ?: MaterialTheme.colorScheme.tertiary,
            isSelected = true,
            onClick = onRemove,
            selectedIcon = Icons.Default.Close
        )
        return
    }

    FilterChip(
        selected = true,
        onClick = onRemove,
        label = {
            Text(
                text = filter.label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.remove_filter, filter.label),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .height(16.dp)
                    .width(16.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier.height(32.dp)
    )
}

@Preview(showBackground = true)
@Composable
internal fun ActiveFiltersBarPreview() {
    AppTheme {
        ActiveFiltersBar(
            filters = DemoData.Components.activeFilters,
            onRemoveFilter = {},
            onClearAll = {}
        ) // ponytail: demo data only
    }
}
