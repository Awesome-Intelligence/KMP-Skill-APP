package com.androidkit.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.res.*
import com.androidkit.app.ui.components.SmallSecondaryButton
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme

/**
 * Confirm Dialog - M3 确认弹窗
 * 28dp 圆角，紧凑内边距
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = stringResource(Res.string.confirm),
    dismissText: String = stringResource(Res.string.cancel),
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    iconBackgroundColor: Color? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon with background
                if (icon != null) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = MaterialTheme.shapes.large,
                        color = iconBackgroundColor ?: MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = iconContentDescription,
                            modifier = Modifier
                                .padding(16.dp)
                                .size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons - dismiss on leading, confirm on trailing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    SmallSecondaryButton(text = dismissText, onClick = onDismiss)
                    SmallSecondaryButton(text = confirmText, onClick = onConfirm)
                }
            }
        }
    }
}

/**
 * Select Dialog - 单选弹窗（Google 风格）
 */
@Composable
fun SelectDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 先选后确认：选项只更新待提交值，点"确认"才回调 onSelect
    var pendingIndex by remember(selectedIndex) { mutableStateOf(selectedIndex) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Options
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    options.forEachIndexed { index, option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = index == pendingIndex,
                                onClick = { pendingIndex = index }
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    SmallSecondaryButton(text = stringResource(Res.string.cancel), onClick = onDismiss)
                    SmallSecondaryButton(
                        text = stringResource(Res.string.confirm),
                        onClick = {
                            onSelect(pendingIndex)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Delete Confirm Dialog - 删除确认弹窗
 */
@Composable
fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(Res.string.delete_confirm_title),
    message: String = stringResource(Res.string.delete_confirm_message)
) {
    ConfirmDialog(
        title = title,
        message = message,
        confirmText = stringResource(Res.string.delete),
        dismissText = stringResource(Res.string.cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        modifier = modifier,
        icon = Icons.Default.Delete,
        iconContentDescription = stringResource(Res.string.delete),
        iconBackgroundColor = MaterialTheme.colorScheme.errorContainer
    )
}

/**
 * Input Dialog - 文本输入弹窗（Google 风格）
 */
@Composable
fun InputDialog(
    title: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
    initialValue: String = "",
    confirmText: String = stringResource(Res.string.confirm),
    dismissText: String = stringResource(Res.string.cancel),
    singleLine: Boolean = true,
    isPassword: Boolean = false
) {
    var text by remember { mutableStateOf(initialValue) }
    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                SimpleTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = if (hint.isNotBlank()) hint else "",
                    singleLine = singleLine,
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        capitalization = if (singleLine) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
                        imeAction = if (singleLine) ImeAction.Done else ImeAction.Default,
                        keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onConfirm(text)
                            focusManager.clearFocus()
                        }
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    SmallSecondaryButton(text = dismissText, onClick = onDismiss)
                    SmallSecondaryButton(
                        text = confirmText,
                        onClick = {
                            onConfirm(text)
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Loading Dialog - 加载弹窗（Google 风格）
 */
@Composable
fun LoadingDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = "",
    cancellable: Boolean = true
) {
    Dialog(
        onDismissRequest = if (cancellable) onDismiss else {{} }
    ) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                if (message.isNotEmpty()) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmDialogPreview() {
    AppTheme {
        ConfirmDialog(
            title = DemoData.Text.DELETE_GOAL,
            message = DemoData.Text.DELETE_IRREVERSIBLE,
            onConfirm = {},
            onDismiss = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectDialogPreview() {
    AppTheme {
        SelectDialog(
            title = DemoData.Text.SORT_BY,
            options = listOf(DemoData.Text.BY_NAME, DemoData.Text.BY_MODIFIED),
            selectedIndex = 0,
            onSelect = {},
            onDismiss = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun DeleteConfirmDialogPreview() {
    AppTheme {
        DeleteConfirmDialog(onConfirm = {}, onDismiss = {}) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun InputDialogPreview() {
    AppTheme {
        InputDialog(
            title = DemoData.Text.RENAME_TAG,
            hint = DemoData.Text.NEW_NAME_INPUT,
            initialValue = DemoData.Text.WORK,
            onConfirm = {},
            onDismiss = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingDialogPreview() {
    AppTheme {
        LoadingDialog(onDismiss = {}, message = DemoData.Text.LOADING) // ponytail: demo data only
    }
}
