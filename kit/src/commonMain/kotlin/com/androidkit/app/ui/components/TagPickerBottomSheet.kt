package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.input.pointer.pointerInput
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.tagColor

/**
 * Tag Picker Bottom Sheet - 半屏弹窗选择标签
 * 显示所有标签列表，已选中的显示打勾和背景色
 * 模式与标签列表页一致：FlowRow 流式布局 + 胶囊样式
 *
 * @param existingTags 已存在的标签列表
 * @param selectedTags 当前已选中的标签（会显示打勾和背景色）
 * @param onTagSelected 点击标签时的回调（选中则移除/未选中则添加）
 * @param onTagCreated 创建新标签时的回调
 * @param onDismiss 关闭弹窗
 * @param tagColors 标签名到颜色名的映射，用于显示正确的标签颜色
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagPickerBottomSheet(
    existingTags: List<String>,
    selectedTags: List<String> = emptyList(),
    onTagSelected: (String) -> Unit,
    onTagCreated: (String) -> Unit,
    onDismiss: () -> Unit,
    tagColors: Map<String, String> = emptyMap()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newTagName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // 标题
            Text(
                text = stringResource(Res.string.select_tag),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 创建新标签输入框
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.enter_tag_name),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            onTagCreated(newTagName.trim())
                            newTagName = ""
                        }
                    },
                    enabled = newTagName.isNotBlank()
                ) {
                    Text(
                        text = stringResource(Res.string.add),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (newTagName.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 标签列表 - FlowRow 流式布局，与标签列表页一致
            if (existingTags.isEmpty()) {
                Text(
                    text = stringResource(Res.string.no_tags_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // 未选中的标签
                val unselectedTags = existingTags.filter { it !in selectedTags }
                // 已选中的标签（排在前面）
                val selectedTagsList = existingTags.filter { it in selectedTags }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 已选中的标签（如果有）
                    if (selectedTagsList.isNotEmpty()) {
                        Text(
                            text = stringResource(Res.string.selected_tags),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            selectedTagsList.forEach { tag ->
                                TagPickerChip(
                                    tagName = tag,
                                    tagColorName = tagColors[tag] ?: "primary",
                                    isSelected = true,
                                    onClick = { onTagSelected(tag) }
                                )
                            }
                        }
                    }

                    // 未选中的标签
                    if (unselectedTags.isNotEmpty()) {
                        Text(
                            text = stringResource(Res.string.unselected_tags),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp, top = if (selectedTagsList.isNotEmpty()) 8.dp else 0.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            unselectedTags.forEach { tag ->
                                TagPickerChip(
                                    tagName = tag,
                                    tagColorName = tagColors[tag] ?: "primary",
                                    isSelected = false,
                                    onClick = { onTagSelected(tag) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tag Picker Chip - 纯文字标签，选中态加描边
 * 选中 = 标签色文字 + 描边 + 打勾；未选中 = 灰色文字
 */
@Composable
private fun TagPickerChip(
    tagName: String,
    tagColorName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    TagText(
        text = tagName,
        color = if (isSelected) tagColor(tagColorName) else MaterialTheme.colorScheme.onSurfaceVariant,
        isSelected = isSelected,
        onClick = onClick,
        showSelectedIcon = false
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun TagPickerBottomSheetPreview() {
    AppTheme {
        TagPickerBottomSheet(
            existingTags = listOf(
                DemoData.Tags.work.name,
                DemoData.Tags.life.name,
                DemoData.Tags.study.name
            ),
            selectedTags = listOf(DemoData.Tags.work.name),
            onTagSelected = {},
            onTagCreated = {},
            onDismiss = {},
            tagColors = mapOf(
                DemoData.Tags.work.name to DemoData.Value.COLOR_ROSE,
                DemoData.Tags.life.name to DemoData.Value.COLOR_SAGE
            ) // ponytail: demo data only
        )
    }
}

@Preview(name = "Tag Picker Chip", showBackground = true)
@Composable
fun TagPickerChipPreview() {
    AppTheme {
        TagPickerChip(
            tagName = DemoData.Text.HEALTH,
            tagColorName = DemoData.Value.COLOR_PRIMARY,
            isSelected = true,
            onClick = {}
        ) // ponytail: demo data only
    }
}
