package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme

/**
 * Page Header - 自定义实现，完全对齐 M3 TopAppBar 规范
 *
 * 布局：[leading1][leading2] [标题] [action1][action2][menu]
 *
 * 64dp 高度，标题垂直居中，无额外空行
 *
 * 状态栏内边距在组件内部处理（内容下移、背景仍铺到屏幕顶）——
 * app 开了 edge-to-edge 的话，标题和按钮不会被状态栏/系统栏盖住。
 */
@Composable
fun Header(
    title: String,
    modifier: Modifier = Modifier,
    leading1: (@Composable () -> Unit)? = null,
    leading2: (@Composable () -> Unit)? = null,
    titleIcon: (@Composable () -> Unit)? = null,
    action1: (@Composable () -> Unit)? = null,
    action2: (@Composable () -> Unit)? = null,
    menu: (@Composable () -> Unit)? = null,
    onTitleClick: (() -> Unit)? = null,
    maxChars: Int = Int.MAX_VALUE
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            // 背景在前、内边距在后：底色铺满状态栏区域，内容往下避开它
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.statusBars),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            contentAlignment = Alignment.Center
        ) {
            // 左侧图标（向左对齐，垂直居中）
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leading1?.invoke()
                leading2?.invoke()
            }

            // 标题（绝对居中）；onTitleClick 提供时整块可点（如详情页点标题重命名）
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable(enabled = onTitleClick != null) { onTitleClick?.invoke() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                titleIcon?.invoke()
                Text(
                    text = truncateTitle(title, maxChars),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 右侧图标（向右对齐，垂直居中）
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                action1?.invoke()
                action2?.invoke()
                menu?.invoke()
            }
        }
    }
}

/**
 * 按"人眼看到的字数"截断：emoji 算 1 个字，且不会从 emoji 中间砍断。
 *
 * ponytail: 按 Unicode 码点数，组合 emoji（如 👨‍👩‍👧、国旗）仍算多个字；要完全按视觉字数得上 BreakIterator，目前标题场景用不上。
 */
private fun truncateTitle(title: String, maxChars: Int): String {
    if (maxChars == Int.MAX_VALUE || title.codePointCount(0, title.length) <= maxChars) return title
    val end = title.offsetByCodePoints(0, maxChars)
    return title.substring(0, end) + "…"
}

@Preview(showBackground = true)
@Composable
private fun HeaderPreview() {
    AppTheme {
        Header(title = DemoData.Text.GOAL_LIST) // ponytail: demo data only
    }
}
