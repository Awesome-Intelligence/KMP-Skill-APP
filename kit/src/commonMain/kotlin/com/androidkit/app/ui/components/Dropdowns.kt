package com.androidkit.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme

/**
 * Select Dropdown - 下拉选择菜单
 * M3 Exposed Dropdown Menu 标准实现（ExposedDropdownMenuBox + menuAnchor）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectDropdown(
    options: List<DropdownOption<T>>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = stringResource(Res.string.select_placeholder),
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.find { it.value == selectedOption }?.label ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = enabled,
            readOnly = true,
            label = if (label.isNotEmpty()) {
                { Text(text = label, style = MaterialTheme.typography.labelMedium) }
            } else null,
            placeholder = {
                Text(text = placeholder)
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(if (expanded) 180f else 0f)
                )
            },
            singleLine = true,
            // GOOGLE_STYLE：输入控件统一 12dp 圆角（覆盖 M3 默认 4dp）
            shape = MaterialTheme.shapes.medium
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize(true)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.label) },
                    onClick = {
                        onOptionSelected(option.value)
                        expanded = false
                    },
                    // M3 规范：选中项以 Check 图标标识
                    leadingIcon = if (option.value == selectedOption) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

/**
 * Dropdown Option - 下拉选项数据类
 */
data class DropdownOption<T>(
    val label: String,
    val value: T
)

@Preview(showBackground = true)
@Composable
internal fun SelectDropdownPreview() {
    AppTheme {
        SelectDropdown(
            options = DemoData.Components.dropdownOptions, // ponytail: demo data only
            selectedOption = DemoData.Value.LEVEL_MEDIUM,
            onOptionSelected = {}
        )
    }
}
