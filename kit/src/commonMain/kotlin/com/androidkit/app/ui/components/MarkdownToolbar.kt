package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme

/**
 * Markdown 工具栏组件
 *
 * 提供 Markdown 编辑所需的常用格式按钮：
 * - 预览/编辑切换
 * - 粗体、斜体
 * - 标题、引用
 * - 列表（有序、无序）
 * - 链接插入
 */
@Composable
fun MarkdownToolbar(
    modifier: Modifier = Modifier,
    onBoldClick: (() -> Unit)? = null,
    onItalicClick: (() -> Unit)? = null,
    onHeadingClick: (() -> Unit)? = null,
    onQuoteClick: (() -> Unit)? = null,
    onBulletListClick: (() -> Unit)? = null,
    onNumberListClick: (() -> Unit)? = null,
    onTagClick: (() -> Unit)? = null,
    isBoldActive: Boolean = false,
    isItalicActive: Boolean = false,
    isLinkActive: Boolean = false,
    isTagActive: Boolean = false,
    onImageClick: (() -> Unit)? = null,
    goalLinkClick: (() -> Unit)? = null,
    contactLinkClick: (() -> Unit)? = null,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndoClick: (() -> Unit)? = null,
    onRedoClick: (() -> Unit)? = null,
    /** 当前是否处于预览模式；决定切换按钮显示"去编辑"还是"去预览" */
    isPreviewMode: Boolean = false,
    onTogglePreview: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            // ponytail: 不要上下 padding。工具栏置底，与编辑框的间距由外层
            // Spacer 给；键盘弹起时它贴着键盘上沿，加 padding 会显得悬空。
            ,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 编辑/预览切换（放最前，横滑时始终可见）
        ToolbarIconButton(
            icon = if (isPreviewMode) Icons.Outlined.Edit else Icons.Outlined.Visibility,
            contentDescription = stringResource(
                if (isPreviewMode) Res.string.edit else Res.string.preview
            ),
            isActive = isPreviewMode,
            onClick = onTogglePreview
        )

        // 分隔线
        ToolbarDivider()

        // 撤销
        ToolbarIconButton(
            icon = Icons.AutoMirrored.Outlined.Undo,
            contentDescription = stringResource(Res.string.undo_edit),
            enabled = canUndo,
            onClick = onUndoClick
        )

        // 重做
        ToolbarIconButton(
            icon = Icons.AutoMirrored.Outlined.Redo,
            contentDescription = stringResource(Res.string.redo),
            enabled = canRedo,
            onClick = onRedoClick
        )

        // 分隔线
        ToolbarDivider()

        // 粗体
        ToolbarIconButton(
            icon = Icons.Outlined.FormatBold,
            contentDescription = stringResource(Res.string.bold),
            isActive = isBoldActive,
            onClick = onBoldClick
        )

        // 斜体
        ToolbarIconButton(
            icon = Icons.Outlined.FormatItalic,
            contentDescription = stringResource(Res.string.italic),
            isActive = isItalicActive,
            onClick = onItalicClick
        )

        // 分隔线
        ToolbarDivider()

        // 关联目标
        ToolbarIconButton(
            icon = Icons.Outlined.Flag,
            contentDescription = stringResource(Res.string.link_goal),
            onClick = goalLinkClick
        )

        // 关联联系人
        ToolbarIconButton(
            icon = Icons.Outlined.Person,
            contentDescription = stringResource(Res.string.link_contact),
            onClick = contactLinkClick
        )

        // 标签
        ToolbarIconButton(
            icon = Icons.Outlined.Tag,
            contentDescription = stringResource(Res.string.insert_tag),
            isActive = isTagActive,
            onClick = onTagClick
        )

        // 图片
        ToolbarIconButton(
            icon = Icons.Outlined.Image,
            contentDescription = stringResource(Res.string.insert_image),
            onClick = onImageClick
        )

        // 分隔线
        ToolbarDivider()

        // 标题
        ToolbarIconButton(
            icon = Icons.Outlined.Title,
            contentDescription = stringResource(Res.string.heading),
            onClick = onHeadingClick
        )

        // 引用
        ToolbarIconButton(
            icon = Icons.Outlined.FormatQuote,
            contentDescription = stringResource(Res.string.quote),
            onClick = onQuoteClick
        )

        // 分隔线
        ToolbarDivider()

        // 无序列表
        ToolbarIconButton(
            icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
            contentDescription = stringResource(Res.string.bullet_list),
            onClick = onBulletListClick
        )

        // 有序列表
        ToolbarIconButton(
            icon = Icons.Outlined.FormatListNumbered,
            contentDescription = stringResource(Res.string.numbered_list),
            onClick = onNumberListClick
        )
    }
}

/**
 * 工具栏图标按钮
 * 视觉走项目统一的 ScaleIconButton：常态透明底 + M3 state layer（不缩放），
 * 激活态才用 primaryContainer 底。
 */
@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    ScaleIconButton(
        icon = icon,
        contentDescription = contentDescription,
        onClick = { onClick?.invoke() },
        modifier = modifier,
        enabled = enabled && onClick != null,
        isActive = isActive
    )
}

/**
 * 工具栏分隔线
 */
@Composable
private fun ToolbarDivider() {
    Spacer(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun MarkdownToolbarPreview() {
    AppTheme {
        MarkdownToolbar(canUndo = true, onBoldClick = {}, onTogglePreview = {}) // ponytail: demo data only
    }
}

@Preview(name = "Toolbar Icon Button", showBackground = true)
@Composable
fun ToolbarIconButtonPreview() {
    AppTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarIconButton(
                icon = Icons.Outlined.FormatBold,
                contentDescription = DemoData.Text.BOLD,
                isActive = true,
                onClick = {}
            )
            ToolbarIconButton(
                icon = Icons.Outlined.FormatItalic,
                contentDescription = DemoData.Text.ITALIC,
                onClick = {}
            )
            ToolbarIconButton(
                icon = Icons.AutoMirrored.Outlined.Undo,
                contentDescription = DemoData.Text.UNDO,
                enabled = false,
                onClick = {}
            )
        } // ponytail: demo data only
    }
}

@Preview(name = "Toolbar Divider", showBackground = true)
@Composable
fun ToolbarDividerPreview() {
    AppTheme {
        ToolbarDivider() // ponytail: demo data only
    }
}
