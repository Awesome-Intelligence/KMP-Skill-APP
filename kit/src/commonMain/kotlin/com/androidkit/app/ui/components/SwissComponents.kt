package com.androidkit.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.theme.LocalReducedMotion
import com.androidkit.app.ui.theme.Swiss
import com.androidkit.app.ui.theme.SwissMotion
import com.androidkit.app.ui.theme.swissRed

/**
 * 瑞士国际风格组件 —— 规范里的四个角色：Structural List Row / Section Label /
 * Inline Button / Color Field。
 *
 * 共性纪律：无圆角、无阴影、无渐变、无位移；交互只改颜色/边框色；
 * 全部左对齐；悬停 150ms ease-out；键盘聚焦有 2dp 可见焦点环。
 *
 * 这些是**新增**组件，不改 kit 现有组件的默认外观 —— 业务自己挑用哪个。
 */

/**
 * 章节标签 —— 11px/700、0.08em 字距、大写，序号和标题之间用一条 24dp 黑线连起来。
 */
@Composable
fun SwissSectionLabel(
    title: String,
    numeral: String? = null,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (numeral != null) {
            Text(
                text = numeral.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = swissRed()
            )
            Spacer(Modifier.width(8.dp))
        }
        Box(
            Modifier
                .width(24.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 结构行 —— 列表/索引/目录行：无底色、无圆角、无阴影，左侧 2px 栏线起于灰，
 * 行高 64dp、左内边距 16dp，序号 11px/700 红色带 0.08em 字距，标题 20px/700。
 *
 * 悬停：栏线变红、底色变 `#f0f0f0`，只变色不位移。
 */
@Composable
fun SwissStructuralRow(
    title: String,
    numeral: String? = null,
    supporting: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val reduced = LocalReducedMotion.current
    val spec = SwissMotion.spec<Color>(reduced)

    val ground by animateColorAsState(
        targetValue = if (hovered) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        animationSpec = spec,
        label = "swissGround"
    )
    // 栏线是图形不是文字 —— 用原色 #ff0000（对白底 4:1，满足图形对比要求）
    val rule by animateColorAsState(
        targetValue = if (hovered) Swiss.redBlock else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = spec,
        label = "swissRule"
    )
    var focused by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(ground)
            .hoverable(enabled = enabled, interactionSource = interaction)
            .clickable(enabled = enabled, onClick = onClick, interactionSource = interaction)
            .onFocusChanged { focused = it.isFocused }
            .swissFocusRing(focused),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(rule)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 禁用态降对比但保持可读：用 onSurfaceVariant，而不是给黑字打 40% 透明
            val textColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            if (numeral != null) {
                Text(
                    text = numeral.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (enabled) swissRed() else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
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
}

/**
 * 文字按钮 —— 无填充、无边框、无圆角、左对齐；13px/700 大写、0.08em 字距，
 * 底下 2px 黑线；悬停线变红，按下加粗到 3px。按规范带方向箭头。
 */
@Composable
fun SwissInlineButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()
    val reduced = LocalReducedMotion.current
    val spec = SwissMotion.spec<Color>(reduced)

    val rule by animateColorAsState(
        targetValue = if (hovered) Swiss.redBlock else MaterialTheme.colorScheme.primary,
        animationSpec = spec,
        label = "swissUnderline"
    )
    var focused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .width(IntrinsicSize.Min)
            // 无障碍：触控目标至少 44dp（规范 accessibility.targetSize）
            .heightIn(min = 44.dp)
            .hoverable(enabled = enabled, interactionSource = interaction)
            .clickable(enabled = enabled, onClick = onClick, interactionSource = interaction)
            .onFocusChanged { focused = it.isFocused }
            .swissFocusRing(focused),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val textColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = textColor
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "→",
                style = MaterialTheme.typography.labelLarge,
                color = textColor
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(if (pressed) 3.dp else 2.dp)
                .background(rule)
        )
    }
}
/**
 * 键盘焦点环 —— 2px 红框，与组件边缘留 2dp 偏移。
 *
 * 规范无障碍段：「Use a 2px visible focus-visible ring with offset from the component edge.」
 *
 * ponytail: 只画 focus（不是 focus-visible）—— Compose 没有现成的 focus-visible 语义，
 * 触屏不会触发键盘焦点，实操上够用；真要精确区分再接 KeyboardEvent。
 */
private fun Modifier.swissFocusRing(focused: Boolean): Modifier = drawBehind {
    if (!focused) return@drawBehind
    val inset = 2.dp.toPx()
    val stroke = 2.dp.toPx()
    drawRect(
        color = Swiss.redBlock,
        topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
        size = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height - inset * 2),
        style = Stroke(width = stroke)
    )
}

/**
 * 色块 —— 红/蓝/黄的整块分区，无圆角无阴影；里面的字至少 32px/700，左对齐，内边距 48dp。
 *
 * 规范说「一屏一块」，用的时候自己克制。
 */
@Composable
fun SwissColorField(
    color: Color,
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color)
            .padding(48.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = if (color.luminance() > 0.5f) Color.Black else Color.White
        )
    }
}
