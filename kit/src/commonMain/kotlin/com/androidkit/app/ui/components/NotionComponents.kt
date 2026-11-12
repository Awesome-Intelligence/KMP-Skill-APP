package com.androidkit.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.theme.LocalReducedMotion
import com.androidkit.app.ui.theme.NotionMotion
import com.androidkit.app.ui.theme.themedCorner

/**
 * Notion 风格组件 —— 规范里的四个角色：Block Row / Ghost Button / Inline Input / Callout。
 *
 * 共性纪律：4px 圆角、无渐变、无重阴影、**不位移不缩放**；
 * 交互只改底色一档（纸面 → 悬停灰 → 按下灰），150ms ease-out；
 * 键盘聚焦有 2dp 可见焦点环。
 *
 * 这些是**新增**组件，不改 kit 现有组件的默认外观 —— 业务自己挑用哪个。
 */

/**
 * 文档块行 —— 透明底、4px 圆角、4x8 内边距、正文 14px/1.6。
 *
 * 悬停：底色从纸面变 `#efedea`（只变色，不位移）。
 * 左侧 16dp 拖拽手柄默认透明，悬停或聚焦时淡入 —— Notion 的标志性细节
 * （Drag Handle Illusion）：它只说「这一块可以拖」，不添加任何别的东西。
 */
@Composable
fun NotionBlockRow(
    title: String,
    supporting: String? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val reduced = LocalReducedMotion.current
    val spec = NotionMotion.spec<Color>(reduced)

    val ground by animateColorAsState(
        targetValue = if (hovered) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            Color.Transparent
        },
        animationSpec = spec,
        label = "notionBlockGround"
    )
    var focused by remember { mutableStateOf(false) }
    val focusRing = MaterialTheme.colorScheme.outline

    val handleAlpha by animateFloatAsState(
        targetValue = if (hovered || focused) 1f else 0f,
        animationSpec = NotionMotion.spec(reduced),
        label = "notionHandle"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ground, themedCorner(4.dp))
            .hoverable(interactionSource = interaction)
            .clickable(onClick = onClick, interactionSource = interaction)
            .onFocusChanged { focused = it.isFocused }
            .drawBehind {
                if (focused) {
                    drawRect(
                        color = focusRing,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 拖拽手柄：默认透明，悬停/聚焦时淡入
        Text(
            text = "⋮⋮",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .width(16.dp)
                .alpha(handleAlpha)
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 幽灵按钮 —— 透明底、无边框、4px 圆角、4x8 内边距、13-14px/500 文字。
 *
 * 悬停填 `#efedea`，按下填 `#e3e1db` —— 只加深底色，不上浮、不缩放。
 * 图标 16dp 线性风格，与文字间距 6dp。
 */
@Composable
fun NotionGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()
    val reduced = LocalReducedMotion.current

    val ground by animateColorAsState(
        targetValue = when {
            pressed -> MaterialTheme.colorScheme.surfaceContainerHighest
            hovered -> MaterialTheme.colorScheme.surfaceVariant
            else -> Color.Transparent
        },
        animationSpec = NotionMotion.spec(reduced),
        label = "notionGhostGround"
    )
    var focused by remember { mutableStateOf(false) }
    val focusRing = MaterialTheme.colorScheme.outline

    Row(
        modifier = modifier
            .background(ground, themedCorner(4.dp))
            .hoverable(interactionSource = interaction)
            .clickable(onClick = onClick, interactionSource = interaction)
            .onFocusChanged { focused = it.isFocused }
            .drawBehind {
                if (focused) {
                    drawRect(
                        color = focusRing,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 行内输入框 —— 透明底、无边框、4x8 内边距、正文 14px。
 *
 * 聚焦时只加一条 1px 底线（发丝线 `#e9e7e4`）—— 没有外圈、没有底色变化。
 * 占位符是墨色的 60%。
 */
@Composable
fun NotionInlineInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true
) {
    val hairline = MaterialTheme.colorScheme.outlineVariant
    val ink = MaterialTheme.colorScheme.onSurface
    var focused by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = ink),
        cursorBrush = SolidColor(ink),
        modifier = modifier
            .onFocusChanged { focused = it.isFocused }
            .drawBehind {
                if (focused) {
                    val y = size.height - 1.dp.toPx()
                    drawRect(
                        color = hairline,
                        topLeft = Offset(0f, y),
                        size = Size(size.width, 1.dp.toPx())
                    )
                }
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ink.copy(alpha = 0.6f)
                    )
                }
                inner()
            }
        }
    )
}

/**
 * 提示块 —— 淡底 `#efedea`、4px 圆角、12x16 内边距，左侧 8dp 图标槽，正文 14px/1.6。
 *
 * 规范说「一页只准一种强调底色」—— 用的时候自己克制。
 */
@Composable
fun NotionCallout(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, themedCorner(4.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
