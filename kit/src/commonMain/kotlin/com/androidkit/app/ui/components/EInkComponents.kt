package com.androidkit.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.theme.EInkMotion
import com.androidkit.app.ui.theme.EInkType
import com.androidkit.app.ui.theme.LocalReducedMotion
import org.jetbrains.compose.resources.stringResource
import com.androidkit.app.res.*

/**
 * 电子纸风格组件 —— 规范里的角色：Ink Action（主操作）/ Reading Row（阅读行）/
 * Paper Field（纸面输入）/ Information Panel（信息面板）/ Halftone（网点底纹）/ Status Marker（状态标记）。
 *
 * 共性纪律：无圆角、无阴影、无渐变；行之间用 1px 栏线分组；
 * 数值用等宽并带单位；状态 = 形状（实心/空心方块）+ 文字，不靠颜色；
 * 网点底纹只做装饰，不铺正文。
 *
 * 这些是**新增**组件，不改 kit 现有组件的默认外观 —— 业务自己挑用哪个。
 */

/**
 * 墨面按钮 —— 页面的单一主要行动。
 *
 * 规范：墨色填充 + 零圆角 + 12–16px 内边距 + 至少 44px 高；
 * 标签 14px/600 等宽；hover 变面板灰、active 变栏线灰，
 * **底色变浅时文字必须换成墨色**（否则浅底浅字看不见）；
 * 键盘焦点用 2px 墨环。
 *
 * 焦点为什么是「反色」而不是「墨环」：墨底上画墨色描边等于没画。
 * 聚焦时整块翻成纸底墨字 + 2px 墨环，才是真看得见的焦点。
 */
@Composable
fun EInkInkAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val reduced = LocalReducedMotion.current
    val scheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    var focused by remember { mutableStateOf(false) }

    val container by animateColorAsState(
        targetValue = when {
            focused -> scheme.surface
            hovered -> scheme.surfaceVariant
            else -> scheme.primary
        },
        animationSpec = EInkMotion.spec(reduced),
        label = "eink-action-container"
    )
    val content by animateColorAsState(
        targetValue = if (focused || hovered) scheme.onSurface else scheme.onPrimary,
        animationSpec = EInkMotion.spec(reduced),
        label = "eink-action-content"
    )
    val alpha = if (enabled) 1f else 0.6f

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .background(container.copy(alpha = alpha))
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = scheme.primary.copy(alpha = alpha),
                shape = RectangleShape
            )
            .onFocusChanged { focused = it.isFocused }
            .hoverable(interactionSource)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = EInkType.action,
            color = content.copy(alpha = alpha),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 纸面输入框 —— 搜索、筛选、短文本输入。
 *
 * 规范：暖纸填充 + 1px 中性边框 + 至少 44px 高 + **可见 label** +
 * 占位文字保持可读灰度 + 2px 键盘焦点轮廓。
 */
@Composable
fun EInkPaperField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    var focused by remember { mutableStateOf(false) }
    val alpha = if (enabled) 1f else 0.6f

    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurface.copy(alpha = alpha)
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 44.dp)
                .background(scheme.surface.copy(alpha = alpha))
                .border(
                    width = if (focused) 2.dp else 1.dp,
                    color = (if (focused) scheme.primary else scheme.outline).copy(alpha = alpha),
                    shape = RectangleShape
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty() && placeholder != null) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant.copy(alpha = alpha)
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface),
                cursorBrush = SolidColor(scheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focused = it.isFocused }
            )
        }
    }
}

/**
 * 信息面板 —— 规范里的卡片角色。
 *
 * 纸面 + 单线栏线边框 + 0 elevation + 20px 内边距；
 * 层次靠小标题（label）和数值（等宽）建立。
 *
 * 底色用**纸色**而不是阅读面：阅读面 #e6e4da 是「选中行」的标记色，
 * 卡片再用同一个色，选中行就看不出选了没有 —— 状态色只能占一个位置。
 */
@Composable
fun EInkPanel(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(contentPadding)) { content() }
    }
}

/**
 * 阅读行 —— 列表 / 索引 / 摘要行。
 *
 * 结构：顶部一条 1px 栏线；序号与数值用等宽；数值右侧带单位；
 * 选中时左侧加 2px 墨线、底色转阅读面，并给出「已选」文字
 * （规范要求「文字 + 墨线」同时出现）。
 *
 * 窄屏（< 360dp）时数值/状态整体换到第二行 —— 规范要求
 * 「在窄屏按阅读顺序合并列」，硬挤会把字压到 12px 以下。
 *
 * @param state 状态词（如「离线」「已完成」）；传 null 就不显示状态标记
 */
@Composable
fun EInkReadingRow(
    index: String,
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    value: String? = null,
    unit: String? = null,
    state: String? = null,
    stateFilled: Boolean = true,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val reduced = LocalReducedMotion.current
    val scheme = MaterialTheme.colorScheme
    val selectedLabel = stringResource(Res.string.eink_selected)
    // drawBehind 里不能读 CompositionLocal —— 先在外面取出来再捕获
    val inkLine = scheme.primary

    // 选中态：底色渐变到阅读面（规范「selected: reading gray, ink rule and label」）
    val rowBackground by animateColorAsState(
        targetValue = if (selected) scheme.surfaceVariant else scheme.surface,
        animationSpec = EInkMotion.spec(reduced),
        label = "eink-row-background"
    )

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val narrow = maxWidth < 360.dp
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(scheme.outlineVariant)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rowBackground)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .then(
                        if (selected) {
                            Modifier.drawBehind {
                                drawRect(color = inkLine, size = Size(2.dp.toPx(), size.height))
                            }
                        } else {
                            Modifier
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = index,
                    style = EInkType.dataSmall,
                    color = scheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurface
                    )
                    if (supporting != null) {
                        Text(
                            text = supporting,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
                if (!narrow) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ReadingRowTrailing(
                            state = state,
                            stateFilled = stateFilled,
                            selectedLabel = if (selected) selectedLabel else null,
                            value = value,
                            unit = unit
                        )
                    }
                }
            }
            if (narrow) {
                // 窄屏：数值与状态换行到第二行，按阅读顺序排
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowBackground)
                        .padding(start = 40.dp, end = 16.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReadingRowTrailing(
                        state = state,
                        stateFilled = stateFilled,
                        selectedLabel = if (selected) selectedLabel else null,
                        value = value,
                        unit = unit
                    )
                }
            }
        }
    }
}

/** 阅读行的右侧内容：状态标记 → 已选标签 → 数值 + 单位。 */
@Composable
private fun ReadingRowTrailing(
    state: String?,
    stateFilled: Boolean,
    selectedLabel: String?,
    value: String?,
    unit: String?,
) {
    val scheme = MaterialTheme.colorScheme
    if (state != null) {
        EInkStatusMarker(state = state, filled = stateFilled)
        Spacer(Modifier.width(12.dp))
    }
    if (selectedLabel != null) {
        Text(
            text = selectedLabel,
            style = EInkType.dataLabel,
            color = scheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
    }
    if (value != null) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = EInkType.data,
                color = scheme.onSurface
            )
            if (unit != null) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = EInkType.dataLabel,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 状态标记 —— 实心 / 空心小方块 + 状态词。
 *
 * 规范：「点不是唯一的状态载体，错误和警告不另发明新色相」——
 * 所以形状和词一起给，颜色只是灰阶底色。
 */
@Composable
fun EInkStatusMarker(
    state: String,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        StatusMark(filled = filled, color = scheme.primary)
        Spacer(Modifier.width(6.dp))
        Text(
            text = state.uppercase(),
            style = EInkType.dataLabel,
            color = scheme.onSurfaceVariant
        )
    }
}

/**
 * 网点底纹 —— 装饰性半调，5dp 一格、点距 ≥5px。
 *
 * 规范只允许把网点用在装饰块 / 图表填充，**别铺正文**；
 * 正文和控件保持实心，网点不该糊掉字边。
 *
 * 实现：一次 drawPoints 画完整片网点（一个绘制调用，不逐点循环），
 * 点位随尺寸缓存 —— 尺寸不变就不重算。
 */
@Composable
fun Modifier.eInkGrain(): Modifier {
    // 网点用「次级文字色」—— 深浅主题各取各的，不写死色值
    val dotColor = MaterialTheme.colorScheme.onSurfaceVariant
    return this.drawWithCache {
        val spacing = 5.dp.toPx()
        val dots = buildList {
            var y = spacing / 2f
            while (y < this@drawWithCache.size.height) {
                var x = spacing / 2f
                while (x < this@drawWithCache.size.width) {
                    add(Offset(x, y))
                    x += spacing
                }
                y += spacing
            }
        }
        onDrawBehind {
            drawPoints(
                points = dots,
                pointMode = PointMode.Points,
                color = dotColor.copy(alpha = 0.35f),
                strokeWidth = spacing * 0.26f,
                cap = StrokeCap.Round
            )
        }
    }
}
