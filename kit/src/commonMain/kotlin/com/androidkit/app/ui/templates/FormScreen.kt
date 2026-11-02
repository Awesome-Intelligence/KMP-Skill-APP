package com.androidkit.app.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.androidkit.app.res.*
import com.androidkit.app.ui.components.Header
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme

/**
 * 表单页模板 - 新增/编辑的标准套路
 * 所有表单页实现须参考此模板结构
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    title: String,
    isEditMode: Boolean,
    onCloseClick: () -> Unit,
    onSaveClick: () -> Unit,
    saveEnabled: Boolean = true,
    saveLoading: Boolean = false,
    modifier: Modifier = Modifier,
    titleIcon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            Header(
                title = title.ifBlank { stringResource(if (isEditMode) Res.string.edit else Res.string.add) },
                titleIcon = titleIcon,
                leading1 = {
                    IconButton(onClick = onCloseClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.close),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                action1 = {
                    IconButton(
                        onClick = onSaveClick,
                        enabled = saveEnabled && !saveLoading
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = stringResource(Res.string.save),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            content()
        }
    }
}

/**
 * 表单域 - 非文本控件（日期选择卡、色板、Chip 组等）的外部标题布局
 * 文本输入框与下拉框已统一为 M3 内嵌浮动标签形态（AppTextField / SelectDropdown），不再使用本组件包裹
 */
@Composable
fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

/**
 * 表单分组
 */
@Composable
fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        content()
    }
}

/**
 * 表单页模板 Preview
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Form Screen", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun FormScreenPreview(onBackClick: () -> Unit = {}) {
    FormScreen(
        title = stringResource(Res.string.add_first_contact),
        isEditMode = false,
        onCloseClick = onBackClick,
        onSaveClick = { },
        saveEnabled = true,
        content = {
            FormSection(title = stringResource(Res.string.basic_info)) {
                FormField(label = stringResource(Res.string.tag_placeholder), content = {
                    Text(stringResource(Res.string.non_text_widget_placeholder), style = MaterialTheme.typography.bodyLarge)
                })
            }
        }
    )
}

@Preview(name = "Form Field", showBackground = true, widthDp = 360)
@Composable
fun FormFieldPreview() {
    AppTheme {
        FormField(label = DemoData.Text.TAG) {
            Text(DemoData.Text.HEALTH_STUDY_WORK, style = MaterialTheme.typography.bodyLarge)
        } // ponytail: demo data only
    }
}

@Preview(name = "Form Section", showBackground = true, widthDp = 360)
@Composable
fun FormSectionPreview() {
    AppTheme {
        FormSection(title = DemoData.Text.BASIC_INFO) {
            FormField(label = DemoData.Text.TAG) {
                Text(DemoData.Text.HEALTH_STUDY_WORK, style = MaterialTheme.typography.bodyLarge)
            }
            FormField(label = DemoData.Text.NOTES) {
                Text(DemoData.Text.HEALTH_TAG_DESC, style = MaterialTheme.typography.bodyMedium)
            }
        } // ponytail: demo data only
    }
}
