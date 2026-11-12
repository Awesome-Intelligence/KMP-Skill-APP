package com.androidkit.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.theme.LocalIsDarkTheme
import com.androidkit.app.ui.theme.LocalReducedMotion
import com.androidkit.app.ui.theme.MacOS
import com.androidkit.app.ui.theme.MacOSMotion
import com.androidkit.app.ui.theme.themedCorner

/**
 * macOS 毛玻璃组件 —— 规范里的四个角色：Vibrancy Panel / List Row / Toolbar / Segmented Control。
 *
 * 共性纪律：**只用三档灰定层次**、分隔一律 1px、**不用阴影制造深度**、
 * 200ms ease-out **只改颜色**（不位移、不缩放）；正文密度按桌面（行 44dp、正文 15px）。
 *
 * 这些是**新增**组件，不改 kit 现有组件的默认外观 —— 业务自己挑用哪个。
 */

/**
 * 毛玻璃面板 —— 80% 不透明的最深灰 + 1px 白边、14px 圆角（规范：面板 14px）。
 *
 * 规范原话是 `rgba(28,28,30,0.8)` 配 `backdrop-filter: blur(40px)`。
 * ponytail: Compose Multiplatform **没有 backdrop-filter**（`Modifier.blur` 模糊的是节点自身），
 * 所以这里只保留半透明 + 1px 边 —— 底下的内容能透出来，观感接近；真背景模糊要各端原生实现。
 */
@Composable
fun MacOSVibrancyPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dark = LocalIsDarkTheme.current
    Box(
        modifier = modifier
            .background(
                color = if (dark) MacOS.vibrancyDark else MacOS.vibrancyLight,
                shape = themedCorner(14.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = themedCorner(14.dp)
            )
    ) {
        content()
    }
}

/**
 * 毛玻璃侧边栏 —— 240px 宽、12px 内边距、**只有右边一条 1px 白 10% 边**。
 *
 * 规范：「Sidebar and overlay background」「240px」「12px padding」。
 */
@Composable
fun MacOSVibrancySidebar(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dark = LocalIsDarkTheme.current
    Box(
        modifier = modifier
            .width(240.dp)
            .background(if (dark) MacOS.vibrancyDark else MacOS.vibrancyLight)
            .drawBehind {
                // 只有右边一条 1px 白 10% 分隔线
                drawRect(
                    color = Color.White.copy(alpha = 0.10f),
                    topLeft = Offset(size.width - 1.dp.toPx(), 0f),
                    size = Size(1.dp.toPx(), size.height)
                )
            }
            .padding(12.dp)
    ) {
        content()
    }
}


/**
 * 列表行 —— 44dp 高、12dp 左右内边距、6px 圆角、默认透明底。
 *
 * 行间用 1px 白 8% 分隔；悬停填白 6%，选中填系统蓝 18%。
 * 行首图标 16dp，行尾备注 13px / 白 60%。**不位移、不缩放**。
 */
@Composable
fun MacOSListRow(
    title: String,
    trailing: String? = null,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val reduced = LocalReducedMotion.current
    val dark = LocalIsDarkTheme.current

    val ground by animateColorAsState(
        targetValue = when {
            selected -> if (dark) MacOS.selectionDark else MacOS.selectionLight
            // 悬停填白 6%（浅色底下白看不见，换黑）
            hovered -> if (dark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)
            else -> Color.Transparent
        },
        animationSpec = MacOSMotion.spec(reduced),
        label = "macOSRowGround"
    )
    var focused by remember { mutableStateOf(false) }
    // drawBehind 里不能读 CompositionLocal —— 先在外面取出来再捕获
    // 焦点环用系统蓝（规范：焦点环用 accent 色）
    val focusRing = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(ground, themedCorner(6.dp))
            .hoverable(interactionSource = interaction)
            .clickable(onClick = onClick, interactionSource = interaction)
            .onFocusChanged { focused = it.isFocused }
            .drawBehind {
                if (focused) {
                    // 2px 焦点环，往里缩 1px —— 环和元件边缘之间留一道缝
                    val stroke = 2.dp.toPx()
                    drawRect(
                        color = focusRing,
                        style = Stroke(width = stroke),
                        topLeft = Offset(stroke / 2f, stroke / 2f),
                        size = Size(size.width - stroke, size.height - stroke)
                    )
                }
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 窗口工具栏 —— 52dp 高、浅灰 `#3a3a3c` 底、底边 1px 白 10%、**无阴影**。
 *
 * 左边三颗 12dp 红黄绿交通灯，中间是**居中**的 13px 衬线标题，右边留给工具按钮。
 */
@Composable
fun MacOSToolbar(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val hairline = MaterialTheme.colorScheme.outlineVariant
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .drawBehind {
                    // 底边 1px 白 10%
                    drawRect(
                        color = hairline,
                        topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - 1.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(size.width, 1.dp.toPx())
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TrafficLight(MacOSTrafficLight.Red)
                TrafficLight(MacOSTrafficLight.Yellow)
                TrafficLight(MacOSTrafficLight.Green)
            }
            // 标题居中：左右两侧各占等宽，标题才是真居中
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    // 规范：13px 衬线标题居中（bodyMedium 是 13px，改字体家族成衬线）
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            if (trailing != null) {
                Box(modifier = Modifier.padding(end = 12.dp)) { trailing() }
            }
        }
    }
}

/** 交通灯颜色（macOS 标准三色） */
enum class MacOSTrafficLight { Red, Yellow, Green }

/** 单颗交通灯 —— 12dp 圆点。 */
@Composable
private fun TrafficLight(color: MacOSTrafficLight) {
    val tint = when (color) {
        MacOSTrafficLight.Red -> Color(0xFFFF5F57)
        MacOSTrafficLight.Yellow -> Color(0xFFFFBD2E)
        MacOSTrafficLight.Green -> Color(0xFF28C840)
    }
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(tint, themedCorner(6.dp))
    )
}

/**
 * 分段控件 —— 轨道白 6%、8px 圆角、28dp 高；选中段填浅灰 `#3a3a3c`、6px 圆角。
 *
 * 文字 11px / 500。切换只改颜色（200ms），不缩放不弹跳。
 *
 * @param options 选项文案
 * @param selectedIndex 当前选中项
 */
@Composable
fun MacOSSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val reduced = LocalReducedMotion.current
    val dark = LocalIsDarkTheme.current
    // 轨道与悬停都是「白 6/8%」—— 浅色底下白看不见，改成黑
    val track = if (dark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)
    val hover = if (dark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)

    Row(
        modifier = modifier
            .height(28.dp)
            .background(track, themedCorner(8.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val interaction = remember { MutableInteractionSource() }
            val hovered by interaction.collectIsHoveredAsState()

            val ground by animateColorAsState(
                targetValue = when {
                    selected -> MaterialTheme.colorScheme.secondaryContainer
                    hovered -> hover
                    else -> Color.Transparent
                },
                animationSpec = MacOSMotion.spec(reduced),
                label = "macOSSegmentGround"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(ground, themedCorner(6.dp))
                    .hoverable(interactionSource = interaction)
                    .clickable(onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * 系统按钮 —— 规范里的第五个角色。
 *
 * 系统蓝实心、6px 圆角、13px/500 文字、上下 6px 左右 14px 内边距；
 * 悬停整体亮 8%、按下暗 10%、禁用是 12% 白底。**只改颜色**（200ms），不缩放。
 */
@Composable
fun MacOSSystemButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()
    val reduced = LocalReducedMotion.current
    var focused by remember { mutableStateOf(false) }

    val base = MaterialTheme.colorScheme.primary
    val focusRing = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.45f)
    val ground by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            pressed -> base.brightened(0.90f)
            hovered -> base.brightened(1.08f)
            else -> base
        },
        animationSpec = MacOSMotion.spec(reduced),
        label = "macOSButtonGround"
    )

    Box(
        modifier = modifier
            .clip(themedCorner(6.dp))
            .background(ground)
            .hoverable(interactionSource = interaction)
            .clickable(enabled = enabled, onClick = onClick, interactionSource = interaction)
            .onFocusChanged { focused = it.isFocused }
            .drawBehind {
                if (focused) {
                    val stroke = 2.dp.toPx()
                    drawRect(
                        color = focusRing,
                        style = Stroke(width = stroke),
                        topLeft = Offset(stroke / 2f, stroke / 2f),
                        size = Size(size.width - stroke, size.height - stroke)
                    )
                }
            }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            }
        )
    }
}

/** 亮度微调（悬停 +8% / 按下 -10%），只动颜色不改形状。 */
private fun Color.brightened(factor: Float): Color = Color(
    red = (red * factor).coerceAtMost(1f),
    green = (green * factor).coerceAtMost(1f),
    blue = (blue * factor).coerceAtMost(1f),
    alpha = alpha
)
