package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import com.androidkit.app.ui.theme.themedCorner
import com.androidkit.app.ui.theme.themedStrokeWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.tagColor
import com.androidkit.app.ui.theme.themedFontWeight

/**
 * Filter Chip
 * 规格：圆角 8dp，内边距水平 12dp 垂直 4dp
 * 符合 M3 FilterChip 规范
 */
@Composable
fun FilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingDotColor: Color? = null,
    leadingIcon: ImageVector? = null
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingDotColor != null) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(leadingDotColor)
                    )
                    Spacer(Modifier.width(4.dp))
                }
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = themedFontWeight(if (isSelected) FontWeight.Medium else FontWeight.Normal)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
        // 不写死高度：M3 Chip 默认 32dp 高 + 自动扩展到 48dp 触控目标，
        // 这里加 .height() 会压掉触控扩展，缩小可点区域
        modifier = modifier,
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = Color.Transparent,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            disabledBorderColor = Color.Transparent,
            disabledSelectedBorderColor = MaterialTheme.colorScheme.primary,
            borderWidth = 0.dp,
            selectedBorderWidth = themedStrokeWidth(2.dp),
        ),
        shape = MaterialTheme.shapes.medium
    )
}

/**
 * Add Tag Chip - M3 AssistChip 形态，符合 Google 风格
 * 高度 24dp，圆角 6dp，surfaceVariant 背景，点击添加标签
 */
@Composable
fun AddTagChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = "+",
                style = MaterialTheme.typography.labelSmall
            )
        },
        modifier = modifier.height(24.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = null,
        shape = themedCorner(6.dp)
    )
}

/**
 * Answer Chip - 问题卡里的可点选项
 *
 * 描边 chip：outlineVariant 边框 + surfaceContainerLowest 底，比卡片本身更亮一层。
 * 没有选中态 —— 点一下即生效。
 */
@Composable
fun AnswerChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        },
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    )
}

/**
 * Filter Chip Row - 筛选 Chip 横向列表
 * 规格：horizontal 16dp padding，8dp chip 间距
 * 支持横向滚动，选项过多时自动换行而非挤在一起
 */
@Composable
fun FilterChipRow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, option ->
            FilterChip(
                title = option,
                isSelected = selectedIndex == index,
                onClick = { onSelect(index) }
            )
        }
    }
}

/**
 * Filter Chip Row - 泛型版本，支持任意类型 T
 * 与 SelectDropdown API 对齐：selectedOption / onOptionSelected
 * 支持横向滚动，选项过多时自动换行而非挤在一起
 */
@Composable
fun <T> FilterChipRow(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                title = optionLabel(option),
                isSelected = option == selectedOption,
                onClick = { onOptionSelected(option) }
            )
        }
    }
}

/**
 * Filter Chip Row - 带 Leading Dot 版本
 * 支持显示优先级颜色点
 */
@Composable
fun <T> FilterChipRow(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String,
    leadingDotColors: List<androidx.compose.ui.graphics.Color>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, option ->
            FilterChip(
                title = optionLabel(option),
                isSelected = option == selectedOption,
                onClick = { onOptionSelected(option) },
                leadingDotColor = leadingDotColors.getOrNull(index)
            )
        }
    }
}

/**
 * Selectable Tag - 可选中的标签
 * 使用 M3 FilterChip 实现
 */
@Composable
fun SelectableTag(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = unselectedColor,
            selectedContainerColor = selectedColor,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLabelColor = if (selectedColor.luminance() > 0.5f) Color.Black else Color.White,
        ),
        modifier = modifier.height(32.dp),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = MaterialTheme.colorScheme.surfaceVariant,
            selectedBorderColor = Color.Transparent,
            disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledSelectedBorderColor = Color.Transparent,
        ),
        shape = MaterialTheme.shapes.medium
    )
}

/**
 * App Badge - 静态徽章（不可点）
 * 规格：圆角 8dp，内边距水平 8dp 垂直 4dp，labelMedium，primaryContainer 背景
 */
@Composable
fun AppBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

/**
 * Checkbox - M3 复选框封装
 * 基于 M3 原生 Checkbox 组件
 */
@Composable
fun Checkbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    checkedColor: Color = MaterialTheme.colorScheme.primary
) {
    val stateDescription = contentDescription ?: (if (isChecked) stringResource(Res.string.selected_state) else stringResource(Res.string.unselected_state))
    androidx.compose.material3.Checkbox(
        checked = isChecked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.semantics {
            this.contentDescription = stateDescription
        },
        colors = CheckboxDefaults.colors(
            checkedColor = checkedColor,
            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
            checkmarkColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/**
 * Color Dot - 单个色板圆点，选中时显示勾选标记与描边
 * 规格：视觉 40dp 圆形，选中态 2dp onSurface 描边；点击区扩展到 48dp 满足 M3 最小触控目标
 */
@Composable
fun ColorDot(
    color: Color,
    isSelected: Boolean,
    colorName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                onClickLabel = colorName
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null, // ponytail: decorative checkmark, selection conveyed by border
                    // 深色模式色板为浅调，按亮度取黑/白勾保证对比度
                    tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Color Dot Row - 色板选择行，FlowRow 排列可选颜色圆点
 * 传入 [com.androidkit.app.ui.theme.TagColorNames] 保证色名与 tagColor() 映射一致
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorDotRow(
    colorNames: List<String>,
    selectedColorName: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        // 圆点视觉 40dp、点击区 48dp，4dp 间距使视觉间距保持 12dp
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        colorNames.forEach { colorName ->
            ColorDot(
                color = tagColor(colorName),
                isSelected = colorName == selectedColorName,
                colorName = colorName,
                onClick = { onColorSelected(colorName) }
            )
        }
    }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun FilterChipPreview() {
    AppTheme {
        FilterChip(title = DemoData.Text.ALL, isSelected = true, onClick = {}) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTagChipPreview() {
    AppTheme {
        AddTagChip(onClick = {}) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipRowPreview() {
    AppTheme {
        FilterChipRow(
            options = listOf(DemoData.Text.ALL, DemoData.Text.IN_PROGRESS, DemoData.Text.COMPLETED),
            selectedIndex = 0,
            onSelect = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipRowTypedPreview() {
    AppTheme {
        FilterChipRow(
            options = listOf(DemoData.Text.HIGH, DemoData.Text.MEDIUM, DemoData.Text.LOW),
            selectedOption = DemoData.Text.MEDIUM,
            onOptionSelected = {},
            optionLabel = { it }
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipRowDotPreview() {
    AppTheme {
        FilterChipRow(
            options = listOf(DemoData.Text.HIGH, DemoData.Text.MEDIUM, DemoData.Text.LOW),
            selectedOption = DemoData.Text.HIGH,
            onOptionSelected = {},
            optionLabel = { it },
            leadingDotColors = listOf(
                DemoData.Palette.rose,
                DemoData.Palette.clay,
                DemoData.Palette.slate
            ) // ponytail: demo data only
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectableTagPreview() {
    AppTheme {
        SelectableTag(text = DemoData.Text.WORK, isSelected = true, onClick = {}) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun AppBadgePreview() {
    AppTheme {
        AppBadge(text = DemoData.Text.RECORD_COUNT_3) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckboxPreview() {
    AppTheme {
        Checkbox(isChecked = true, onCheckedChange = {}) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorDotPreview() {
    AppTheme {
        ColorDot(
            color = DemoData.Palette.rose,
            isSelected = true,
            colorName = DemoData.Value.COLOR_ROSE,
            onClick = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ColorDotRowPreview() {
    AppTheme {
        ColorDotRow(
            colorNames = listOf(
                DemoData.Value.COLOR_NEUTRAL,
                DemoData.Value.COLOR_ROSE,
                DemoData.Value.COLOR_SAGE,
                DemoData.Value.COLOR_MAUVE,
                DemoData.Value.COLOR_CLAY,
                DemoData.Value.COLOR_SLATE
            ),
            selectedColorName = DemoData.Value.COLOR_ROSE,
            onColorSelected = {}
        ) // ponytail: demo data only
    }
}

// endregion
