package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.androidkit.app.ui.theme.themedControlShape
import com.androidkit.app.ui.theme.themedCorner
import com.androidkit.app.ui.theme.LocalThemeStyle
import com.androidkit.app.ui.theme.ThemeStyle
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.Swiss

/**
 * 项目统一文本输入框圆角参数 - M3 规范 4dp
 */


/**
 * 项目统一文本输入框边框颜色
 */
@Composable
internal fun textFieldBorderColors() = OutlinedTextFieldDefaults.colors(
    // macOS 毛玻璃规范：聚焦边框是白 25%，不是主色蓝
    focusedBorderColor = when (LocalThemeStyle.current) {
        // macOS 毛玻璃规范：聚焦边框是白 25%，不是主色蓝
        ThemeStyle.MacOS -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
        // 瑞士规范：聚焦边框是信号红（input recipe: focus:border-red）
        ThemeStyle.Swiss -> Swiss.redBlock
        else -> MaterialTheme.colorScheme.primary
    },
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    errorBorderColor = MaterialTheme.colorScheme.error
)

/**
 * 简单的文本输入框组件
 * 使用 M3 OutlinedTextField 实现，符合 Material 3 规范
 */
@Composable
fun SimpleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    singleLine: Boolean = true,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    contentDescription: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
        enabled = enabled,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        isError = isError,
        label = if (label.isNotEmpty()) { { Text(label) } } else null,
        placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder) } } else null,
        supportingText = supportingText?.let {
            { SupportingHint(it, isError) }
        },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = themedControlShape(themedCorner(4.dp)),
        colors = textFieldBorderColors()
    )
}

/**
 * Composer Field - 聊天式输入条（Google 风格）
 *
 * 56dp 高的填充胶囊（surfaceContainerHigh）：文字在里面，语音按钮也在里面。
 * 和 [SearchBar] 同一套视觉语言；表单里的方框输入框别用它。
 */
@Composable
fun ComposerField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onMicClick: () -> Unit,
    micContentDescription: String,
    modifier: Modifier = Modifier,
    /** 键盘「发送」键：给了就显示发送键，不给就是普通输入 */
    onSubmit: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(start = 20.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                imeAction = if (onSubmit != null) ImeAction.Send else ImeAction.Default
            ),
            keyboardActions = KeyboardActions(onSend = { onSubmit?.invoke() }),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )
        IconButton(onClick = onMicClick, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Outlined.Mic,
                contentDescription = micContentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * 文本输入框组件集合
 */
object TextFields {
    /**
     * 简单的文本输入框组件
     * 使用 M3 OutlinedTextField 实现，符合 Material 3 规范
     */
    @Composable
    fun SimpleTextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        label: String = "",
        placeholder: String = "",
        singleLine: Boolean = true,
        enabled: Boolean = true,
        isError: Boolean = false,
        supportingText: String? = null,
        visualTransformation: VisualTransformation = VisualTransformation.None,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions.Default
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            isError = isError,
            label = if (label.isNotEmpty()) { { Text(label) } } else null,
            placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder) } } else null,
            supportingText = supportingText?.let {
            { SupportingHint(it, isError) }
        },
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = themedControlShape(themedCorner(4.dp)),
            colors = textFieldBorderColors()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SimpleTextFieldPreview() {
    AppTheme {
        SimpleTextField(
            value = DemoData.Text.INPUT_CONTENT,
            onValueChange = {},
            label = DemoData.Text.TITLE,
            placeholder = DemoData.Text.PLEASE_INPUT
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun TextFieldsSimpleTextFieldPreview() {
    AppTheme {
        TextFields.SimpleTextField(
            value = DemoData.Text.INPUT_CONTENT,
            onValueChange = {},
            label = DemoData.Text.TITLE
        ) // ponytail: demo data only
    }
}

/**
 * 字段下方的提示文字 —— 出错时带一个**空心方块标记**。
 *
 * 零色相主题（电子纸）里 error 和正文同为墨色，光靠颜色分不出来；
 * 规范要求「每个状态同时给出形状或文字」—— 那个方块就是形状。
 * 其它主题保持原来的纯文字。
 */
@Composable
private fun SupportingHint(text: String, isError: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val showMark = isError && LocalThemeStyle.current == ThemeStyle.EInk
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showMark) {
            StatusMark(filled = false, size = 7.dp, color = scheme.onSurface)
            Spacer(Modifier.width(6.dp))
        }
        Text(text, color = if (isError) scheme.error else scheme.onSurfaceVariant)
    }
}
