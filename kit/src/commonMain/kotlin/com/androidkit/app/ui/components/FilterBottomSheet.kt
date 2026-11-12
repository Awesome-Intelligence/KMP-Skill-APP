package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.tagColor
import com.androidkit.app.ui.theme.rememberTagColor

/**
 * Filter Section - 筛选区块
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

/**
 * Single Select Filter Chips - 单选筛选 Chips
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SingleSelectFilterChips(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            FilterChip(
                title = option,
                isSelected = isSelected,
                onClick = { onSelect(index) }
            )
        }
    }
}

/**
 * Multi Select Filter Chips - 多选筛选 Chips
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiSelectFilterChips(
    options: List<String>,
    selectedIndices: Set<Int>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = selectedIndices.contains(index)
            FilterChip(
                title = option,
                isSelected = isSelected,
                onClick = { onToggle(index) }
            )
        }
    }
}

/**
 * Importance Filter with colored dots - 重要性筛选
 * 样式与目标详情页 GoalOverviewCard 中的优先级 FilterChip 保持一致
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportanceFilterChips(
    options: List<Pair<String, Color>>,
    selectedIndices: Set<Int>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, (label, color) ->
            val isSelected = selectedIndices.contains(index)
            FilterChip(
                title = label,
                isSelected = isSelected,
                onClick = { onToggle(index) },
                leadingDotColor = color
            )
        }
    }
}

/**
 * Tag Filter Chips - 标签筛选
 * 样式与 TagPickerBottomSheet 中的 TagPickerChip 保持一致
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagFilterChips(
    tags: List<String>,
    tagColors: Map<String, String> = emptyMap(),
    selectedTags: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        tags.forEach { tag ->
            val isSelected = selectedTags.contains(tag)
            val color = rememberTagColor(tagColors[tag] ?: "neutral")
            TagFilterChip(
                tag = tag,
                color = color,
                isSelected = isSelected,
                onClick = { onToggle(tag) }
            )
        }
    }
}

@Composable
private fun TagFilterChip(
    tag: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    TagText(
        text = tag,
        color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
        isSelected = isSelected,
        onClick = onClick
    )
}

/**
 * Date Range Filter - 日期范围筛选
 */
@Composable
fun DateRangeFilter(
    startDate: String,
    endDate: String,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 开始日期
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onStartDateClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = startDate.ifEmpty { stringResource(Res.string.start_date) },
                style = MaterialTheme.typography.bodyMedium,
                color = if (startDate.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        Text(
            text = "~",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 结束日期
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onEndDateClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = endDate.ifEmpty { stringResource(Res.string.end_date) },
                style = MaterialTheme.typography.bodyMedium,
                color = if (endDate.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        // 清除按钮
        if (startDate.isNotEmpty() || endDate.isNotEmpty()) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.clear),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Mood Filter Chips - 心情筛选
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodFilterChips(
    moods: List<String>,
    selectedMoods: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        moods.forEach { mood ->
            val isSelected = selectedMoods.contains(mood)
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.shapes.small
                            )
                        } else {
                            Modifier.background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.shapes.small
                            )
                        }
                    )
                    .clickable { onToggle(mood) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = mood,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * Generic Filter Bottom Sheet - 通用筛选底部弹窗
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    title: String = stringResource(Res.string.filter),
    showFilter: Boolean,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    onConfirm: () -> Unit,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    if (showFilter) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasActiveFilters) {
                            TextButton(onClick = onReset) {
                                Text(stringResource(Res.string.reset))
                            }
                        }
                        TextButton(onClick = onConfirm) {
                            Text(stringResource(Res.string.done))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content
                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun FilterSectionPreview() {
    AppTheme {
        FilterSection(
            title = DemoData.Text.PRIORITY,
            content = {
                Text(
                    text = DemoData.Text.FILTER_CONTENT,
                    modifier = Modifier.padding(8.dp)
                )
            }
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun SingleSelectFilterChipsPreview() {
    AppTheme {
        SingleSelectFilterChips(
            options = listOf(DemoData.Text.ALL, DemoData.Text.HIGH, DemoData.Text.MEDIUM, DemoData.Text.LOW),
            selectedIndex = 1,
            onSelect = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun MultiSelectFilterChipsPreview() {
    AppTheme {
        MultiSelectFilterChips(
            options = listOf(DemoData.Text.WORK, DemoData.Text.LIFE, DemoData.Text.STUDY),
            selectedIndices = setOf(0, 2),
            onToggle = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun ImportanceFilterChipsPreview() {
    AppTheme {
        ImportanceFilterChips(
            options = listOf(
                DemoData.Text.HIGH to DemoData.Palette.rose,
                DemoData.Text.MEDIUM to DemoData.Palette.clay,
                DemoData.Text.LOW to DemoData.Palette.slate
            ), // ponytail: demo data only
            selectedIndices = setOf(0),
            onToggle = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun TagFilterChipsPreview() {
    AppTheme {
        TagFilterChips(
            tags = listOf(DemoData.Text.WORK, DemoData.Text.LIFE, DemoData.Text.STUDY),
            tagColors = mapOf(
                DemoData.Text.WORK to DemoData.Value.COLOR_CLAY,
                DemoData.Text.LIFE to DemoData.Value.COLOR_SAGE,
                DemoData.Text.STUDY to DemoData.Value.COLOR_SLATE
            ),
            selectedTags = setOf(DemoData.Text.WORK),
            onToggle = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun DateRangeFilterPreview() {
    AppTheme {
        DateRangeFilter(
            startDate = DemoData.Value.DATE_RANGE_START,
            endDate = DemoData.Value.DATE_RANGE_END,
            onStartDateClick = {},
            onEndDateClick = {},
            onClear = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun MoodFilterChipsPreview() {
    AppTheme {
        MoodFilterChips(
            moods = listOf(DemoData.Text.HAPPY, DemoData.Text.CALM, DemoData.Text.TIRED),
            selectedMoods = setOf(DemoData.Text.HAPPY),
            onToggle = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun FilterBottomSheetPreview() {
    AppTheme {
        FilterBottomSheet(
            title = DemoData.Text.FILTER,
            showFilter = true,
            onDismiss = {},
            onReset = {},
            onConfirm = {},
            hasActiveFilters = true,
            content = {
                Text(
                    text = DemoData.Text.FILTER_CONTENT,
                    modifier = Modifier.padding(8.dp)
                )
            }
        ) // ponytail: demo data only
    }
}
