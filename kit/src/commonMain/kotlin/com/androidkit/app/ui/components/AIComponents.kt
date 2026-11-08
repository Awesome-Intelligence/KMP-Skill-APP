package com.androidkit.app.ui.components

import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.M3Motion
import com.androidkit.app.ui.theme.rememberBreathingAlpha
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.res.*
import com.androidkit.app.ui.theme.extendedColors

// ─── AI Accent Colors ────────────────────────────────────────────────────────
// AI 强调色走 MaterialTheme.extendedColors（aiAccent / aiContainer / onAiContainer），
// 深浅色各一套，这里不再硬编码。

// ─── AI Insight Card ─────────────────────────────────────────────────────────

/**
 * AI Insight Card - 标准 AI 结论展示卡片
 * 规格：12dp 圆角，16dp 内边距，AI 渐变背景色，机器人头像 + 标题 + 内容
 */
@Composable
fun AIInsightCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Psychology,
    isExpanded: Boolean = true,
    onExpandToggle: (() -> Unit)? = null,
    modelName: String? = null
) {
    val ext = MaterialTheme.extendedColors
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI avatar
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = ext.aiAccent.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = ext.aiAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (modelName != null) {
                        Text(
                            text = modelName,
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.aiAccent
                        )
                    }
                }

                if (onExpandToggle != null) {
                    IconButton(
                        onClick = onExpandToggle,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded)
                                stringResource(Res.string.collapse)
                            else
                                stringResource(Res.string.expand),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Content with animation
            AnimatedVisibility(
                visible = isExpanded,
                enter = M3Motion.expandSection(),
                exit = M3Motion.collapseSection()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── AI Suggestion Chip ───────────────────────────────────────────────────────

/**
 * AI 建议条目的玻璃底 —— 半透明中性底（向下淡出）+ 顶部 1dp 反光 + 左侧 AI 柔光晕。
 * 一次 drawBehind 画完（没有图片背景，模糊没有内容可糊，靠"光的层次"而非 backdrop blur）。
 *
 * AISuggestionChip 和记录页的总结浮层共用同一套质感。
 */
@Composable
fun Modifier.aiGlassSurface(shape: Shape = MaterialTheme.shapes.medium): Modifier {
    val ext = MaterialTheme.extendedColors
    // drawBehind 的 lambda 不是 composable，颜色必须在外面取
    val glassTint = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    return this
        .clip(shape)
        .drawBehind {
            // 玻璃底：中性半透明，向上收拢、向下淡出，不留硬边
            drawRect(
                Brush.verticalGradient(
                    listOf(glassTint, glassTint.copy(alpha = 0f))
                )
            )
            // 左侧柔光晕：只在徽标一侧留一点 AI 色
            drawRect(
                Brush.radialGradient(
                    colors = listOf(ext.aiAccent.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(0f, size.height / 2f),
                    radius = 120.dp.toPx()
                )
            )
            // 顶部 1dp 反光
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.55f), Color.Transparent)
                ),
                size = Size(size.width, 1.dp.toPx())
            )
        }
}

/**
 * AI 徽标：半透明圆片（无描边），图标轻微呼吸
 */
@Composable
fun AISuggestionBadge(modifier: Modifier = Modifier) {
    val ext = MaterialTheme.extendedColors
    val pulse = rememberBreathingAlpha(0.7f)
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(ext.aiAccent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null, // ponytail: 装饰性图标，上方已有「AI 建议」标题
            modifier = Modifier.size(16.dp),
            tint = ext.aiAccent.copy(alpha = pulse)
        )
    }
}

/**
 * AI 建议条目的公共骨架：玻璃底（aiGlassSurface）+ AI 徽标 + 内容槽。
 * AISuggestionChip（目标 AI 拆解建议）和 x记 AI 总结浮层的正文条目共用，
 * 保证两处「AI 条目」样式一致。
 */
@Composable
fun AISuggestionItem(
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aiGlassSurface(MaterialTheme.shapes.medium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = verticalAlignment
        ) {
            // AI 徽标：半透明圆片（无描边），图标轻微呼吸
            AISuggestionBadge()

            Spacer(modifier = Modifier.width(12.dp))

            content()
        }
    }
}

/**
 * AI Suggestion Chip - 单条 AI 建议（紧凑展示，接受/忽略按钮）
 *
 * 条目骨架走 [AISuggestionItem]（玻璃质感，与 x记 AI 总结浮层共用）。
 *
 * 整行不可点：采纳/忽略是显式动作，各自带 contentDescription 的按钮，
 * 避免"看不见的整行热区"误触。
 */
@Composable
fun AISuggestionChip(
    text: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = MaterialTheme.extendedColors

    AISuggestionItem(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = ext.onAiContainer,
            modifier = Modifier.weight(1f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.width(4.dp))

        FilledTonalIconButton(
            onClick = onAccept,
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = ext.aiAccent.copy(alpha = 0.16f),
                contentColor = ext.aiAccent
            )
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(Res.string.accept_suggestion),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.dismiss_suggestion),
                modifier = Modifier.size(18.dp),
                tint = ext.onAiContainer.copy(alpha = 0.7f)
            )
        }
    }
}

// ─── AI Loading Skeleton ─────────────────────────────────────────────────────

/**
 * AI Loading Skeleton - AI 生成中的骨架屏
 * 显示脉冲动画的占位行，模拟 AI 正在思考
 */
@Composable
fun AILoadingSkeleton(
    modifier: Modifier = Modifier,
    lineCount: Int = 3
) {
    // 呼吸节奏统一走 rememberBreathingAlpha（减动效时静止不闪）
    val alpha = rememberBreathingAlpha()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header placeholder
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .width(80.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content lines
            repeat(lineCount) { index ->
                val lastLineWidth = if (index == lineCount - 1) 0.6f else 1f
                Box(
                    modifier = Modifier
                        .fillMaxWidth(lastLineWidth)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha))
                )
                if (index < lineCount - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// ─── AI Breathing Icon ───────────────────────────────────────────────────────

/**
 * AI 按钮加载态：图标做呼吸脉动，替代转圈。
 *
 * AI 按钮统一规则：**常态静态图标，AI 请求进行中才呼吸**（呼吸是状态信号，不是身份信号）；
 * 配色统一走 [extendedColors] 的 `aiAccent`（浅色深紫 / 深色浅紫）。
 * 节奏与 [AILoadingSkeleton] 同源（[rememberBreathingAlpha]：LONG1 + emphasized），
 * 减动效下静止不闪。tint 需显式给，否则 disabled 按钮会把它调暗到看不清。
 */
@Composable
fun BreathingIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.extendedColors.aiAccent
) {
    val alpha = rememberBreathingAlpha()
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.alpha(alpha),
        tint = tint
    )
}

// ─── Goal AI Button ───────────────────────────────────────────────────────────

/**
 * Goal AI Menu Item - 目标详情页 AI 菜单项
 */
data class GoalAIMenuItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * Goal AI Button - 目标详情页顶部的 AI 操作按钮
 * 点击后弹出 Popover Menu 包含 AI 功能选项
 */
@Composable
fun GoalAIButton(
    menuItems: List<GoalAIMenuItem>,
    modifier: Modifier = Modifier,
    expandedContentDescription: String = stringResource(Res.string.ai_menu)
) {
    var expanded by remember { mutableStateOf(false) }
    val aiAccent = MaterialTheme.extendedColors.aiAccent

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .semantics { contentDescription = expandedContentDescription }
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = expandedContentDescription,
                tint = aiAccent
            )
        }

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            menuItems.forEach { item ->
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = aiAccent
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = item.label)
                        }
                    },
                    onClick = {
                        expanded = false
                        item.onClick()
                    }
                )
            }
        }
    }
}

// ─── AI Action Button ────────────────────────────────────────────────────────

/**
 * AI Action Button - AI 功能入口按钮
 * 规格同 PrimaryButton（48dp 高、12dp 圆角），配色走 AI 语义色：
 * aiContainer 底 + onAiContainer 文字 + aiAccent 的 AutoAwesome 图标，
 * 与 x记顶栏 AI 图标、AI 建议徽章同一视觉语言
 */
@Composable
fun AIActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.extendedColors.aiContainer,
            contentColor = MaterialTheme.extendedColors.onAiContainer
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.extendedColors.aiAccent
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}


// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun AIInsightCardPreview() {
    AppTheme {
        AIInsightCard(
            title = DemoData.Text.AI_INSIGHT,
            content = DemoData.Text.WEEKLY_COMPLETION_75,
            modelName = DemoData.Value.MODEL_GLM
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun AISuggestionBadgePreview() {
    AppTheme {
        AISuggestionBadge() // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun AISuggestionItemPreview() {
    AppTheme {
        AISuggestionItem {
            Text(text = DemoData.Text.SPLIT_WEEKLY_REPORT)
        } // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun AISuggestionChipPreview() {
    AppTheme {
        AISuggestionChip(
            text = DemoData.Text.DECOMPOSE_SUGGESTION,
            onAccept = {},
            onDismiss = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun AILoadingSkeletonPreview() {
    AppTheme {
        AILoadingSkeleton() // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun BreathingIconPreview() {
    AppTheme {
        BreathingIcon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun GoalAIButtonPreview() {
    AppTheme {
        GoalAIButton(
            menuItems = listOf(
                DemoData.Components.aiMenuDecompose,
                DemoData.Components.aiMenuSummarize
            )
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun AIActionButtonPreview() {
    AppTheme {
        AIActionButton(
            text = DemoData.Text.AI_SMART_DECOMPOSE,
            onClick = {}
        ) // ponytail: demo data only
    }
}
