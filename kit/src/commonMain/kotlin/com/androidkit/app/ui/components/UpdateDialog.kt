package com.androidkit.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.androidkit.app.res.*
import com.androidkit.app.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import com.androidkit.app.ui.theme.themedTextAlign
import com.androidkit.app.ui.theme.themedHorizontalAlignment

/**
 * 新版本提示弹窗 —— 只负责展示和回传用户的选择，不联网、不下载。
 *
 * 数据来自 [com.androidkit.app.data.update.UpdateResult.Available]：
 * - `mandatory = true`：强制更新。弹窗不可关闭、没有“稍后/跳过”，只留一个更新按钮
 * - `mandatory = false`：可以“稍后”或“跳过此版本”；[onSkipVersion] 传 null 就不显示跳过
 * - [updateEnabled]：下载地址没配时置 false（按钮灰掉，而不是点了没反应）
 *
 * “立即更新”做什么由 App 决定：Android 跳商店/浏览器、iOS 跳 App Store、Desktop 跳下载页。
 * kit 不碰平台跳转 —— 和“不写 expect/actual”是同一条边界。
 */
@Composable
fun UpdateDialog(
    latestVersion: String,
    currentVersion: String,
    releaseNotes: String,
    onUpdate: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier = Modifier,
    mandatory: Boolean = false,
    onSkipVersion: (() -> Unit)? = null,
    updateEnabled: Boolean = true,
) {
    val title = stringResource(
        if (mandatory) Res.string.update_required_title else Res.string.update_available_title
    )

    Dialog(onDismissRequest = { if (!mandatory) onLater() }) {
        Surface(
            modifier = modifier.widthIn(max = 440.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = themedHorizontalAlignment(Alignment.CenterHorizontally),
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = title,
                        modifier = Modifier.padding(14.dp).size(28.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = themedTextAlign(TextAlign.Center),
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.update_version_line, currentVersion, latestVersion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = themedTextAlign(TextAlign.Center),
                )

                if (mandatory) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.update_required_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = themedTextAlign(TextAlign.Center),
                    )
                }

                if (releaseNotes.isNotBlank()) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = stringResource(Res.string.update_notes_title),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    // 更新说明是不可信文本：只当纯文本渲染，不解析 HTML/Markdown。
                    // 限高 + 滚动：Release notes 可能很长，别把按钮挤出屏幕。
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = releaseNotes,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp),
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (mandatory) {
                    PrimaryButton(
                        text = stringResource(Res.string.update_now),
                        onClick = onUpdate,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = updateEnabled,
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SmallSecondaryButton(
                            text = stringResource(Res.string.update_later),
                            onClick = onLater,
                        )
                        Spacer(Modifier.width(8.dp))
                        SmallSecondaryButton(
                            text = stringResource(Res.string.update_now),
                            onClick = onUpdate,
                            enabled = updateEnabled,
                        )
                    }
                    // “跳过此版本”是最轻的操作，单独放一行 —— 三个按钮挤一行在窄屏会溢出
                    if (onSkipVersion != null) {
                        TextActionButton(
                            text = stringResource(Res.string.update_skip_version),
                            onClick = onSkipVersion,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun UpdateDialogPreview() {
    AppTheme {
        UpdateDialog(
            latestVersion = "1.4.0",
            currentVersion = "1.2.3",
            releaseNotes = "- 修复了检查更新偶发失败的问题\n- 新增深色模式", // ponytail: demo data only
            onUpdate = {},
            onLater = {},
            onSkipVersion = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun UpdateDialogMandatoryPreview() {
    AppTheme {
        UpdateDialog(
            latestVersion = "2.0.0",
            currentVersion = "1.2.3",
            releaseNotes = "旧版接口已下线，请更新后继续使用。", // ponytail: demo data only
            onUpdate = {},
            onLater = {},
            mandatory = true,
        )
    }
}
