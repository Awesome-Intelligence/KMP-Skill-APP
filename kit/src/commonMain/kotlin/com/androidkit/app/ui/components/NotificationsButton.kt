package com.androidkit.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.LocalIsDarkTheme
import com.androidkit.app.ui.theme.M3Motion
import com.androidkit.app.ui.theme.themedElevation
import com.androidkit.app.ui.theme.themedFontWeight
import com.androidkit.app.ui.theme.themedCircleShape


/**
 * 一条消息 —— 任务提醒 / AI 提醒共用
 */
data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val time: String,
    val icon: ImageVector = Icons.Outlined.Notifications,
    val isRead: Boolean = true,
    /** false = 实时派生的提醒（逾期/临期目标），没有删除按钮 */
    val deletable: Boolean = true,
    /** 点击回跳路由；null = 纯展示。当前只有 x记 总结消息会带 */
    val targetRoute: String? = null
)

/**
 * 右上角消息按钮 —— 点击弹出贴靠右上角的消息面板（主流 App 的消息浮层）
 *
 * M3 / Google 风格：
 * - 按钮：48dp 触控目标 + 24dp 描边图标，角标 BadgedBox 罩图标（不罩按钮）
 * - 面板：16dp 圆角（shapes.large）+ surfaceContainerHigh 靠色阶分层，0dp elevation（不投影）
 * - 列表：两行列表项（标题 titleMedium + 正文 bodyMedium + 尾部时间 labelMedium），垂直 8dp
 */
@Composable
fun NotificationsButton(
    items: List<NotificationItem>,
    onItemClick: (NotificationItem) -> Unit = {},
    onClearAll: () -> Unit = {},
    onItemDelete: (NotificationItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val unreadCount = items.count { !it.isRead }
    val density = LocalDensity.current

    // dismiss 时先把 targetState 置 false 播完退场，再移除 Popup
    val panelState = remember { MutableTransitionState(false) }
    panelState.targetState = expanded

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            // 角标贴在图标右上角（M3：BadgedBox 罩住图标本身，不是 48dp 按钮）
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        Badge { Text(unreadCount.coerceAtMost(99).toString()) }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = stringResource(Res.string.notifications_title),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (expanded || !panelState.isIdle) {
            Popup(
                popupPositionProvider = PanelPositionProvider(density.density),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                NotificationsPanel(
                    visibleState = panelState,
                    items = items,
                    onItemClick = {
                        expanded = false
                        onItemClick(it)
                    },
                    onClearAll = onClearAll,
                    onItemDelete = onItemDelete
                )
            }
        }
    }
}

/**
 * 消息面板内容 —— 标题栏 + 列表，空时显示空状态
 */
@Composable
private fun NotificationsPanel(
    visibleState: MutableTransitionState<Boolean>,
    items: List<NotificationItem>,
    onItemClick: (NotificationItem) -> Unit,
    onClearAll: () -> Unit,
    onItemDelete: (NotificationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // 缩放原点 = 面板右上角。面板定位时右边缘贴图标右边缘、顶边在图标底边下方 4dp，
    // 所以这个角几乎就是铃铛图标本身 —— 卡片从这里长出来。
    val origin = TransformOrigin(1f, 0f)

    // 浮层动画包在 Surface 外面：缩放的是整张卡片（底色+圆角一起缩），
    // 缩在 Surface 里面的话卡片是满尺寸啪地出现的，只有里面的字在缩。
    AnimatedVisibility(
        visibleState = visibleState,
        enter = M3Motion.floatEnter(origin),
        exit = M3Motion.floatExit(origin)
    ) {
        Surface(
            modifier = modifier
                .widthIn(max = PANEL_WIDTH)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
            // M3 浮层（菜单）容器 elevation 2 —— 阴影让它从背景里浮起来。
            // 靠色阶分层是底色，阴影才是"浮在内容之上"的线索，浅色下尤其需要。
            shadowElevation = themedElevation(2.dp)
        ) {
            Column {
                // 标题行：右侧"清空"只清收件箱消息（面板还可能挂着实时派生的目标提醒）
                // 48dp = TextActionButton 的触控目标高度：没消息时没有按钮撑行，补上同样的最小高度
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = TITLE_ROW_MIN_HEIGHT)
                        .padding(start = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.notifications_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (items.isNotEmpty()) {
                        TextActionButton(
                            text = stringResource(Res.string.clear_all),
                            onClick = onClearAll
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (items.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(Res.string.notifications_empty_title),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(Res.string.notifications_empty_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = PANEL_MAX_HEIGHT)
                            .verticalScroll(rememberScrollState())
                    ) {
                        items.forEach { item ->
                            NotificationRow(
                                item = item,
                                onClick = { onItemClick(item) },
                                onDelete = if (item.deletable) {
                                    { onItemDelete(item) }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 两行列表项：40dp 圆形图标 + 标题 + 正文 + 尾部时间（可删条目再带一个删除按钮）
 */
@Composable
private fun NotificationRow(
    item: NotificationItem,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(themedCircleShape())
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.15f else 0.08f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = themedFontWeight(if (item.isRead) FontWeight.Normal else FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.time,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        onDelete?.let {
            // 24dp 小按钮贴行尾，点击不冒泡到行的 onClick（Compose 子级点击自行消费）
            IconButton(onClick = it, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(Res.string.delete),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

internal val PANEL_WIDTH = 320.dp
private val PANEL_MAX_HEIGHT = 400.dp
private val PANEL_GAP = 4.dp
private val TITLE_ROW_MIN_HEIGHT = 48.dp

/** IconButton 48dp 触控目标比 24dp 图标宽出的部分的一半 —— 面板要跟图标对齐，不是跟触控框 */
private val ICON_INSET = 12.dp

/**
 * 面板定位：以铃铛图标为锚 —— 右边缘贴图标右边缘，顶边贴图标底边（间隙 4dp），
 * 看起来像从图标里长出来；下方空间不够时翻到图标上方，仍不够则贴窗口底部
 */
internal class PanelPositionProvider(private val density: Float) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val gap = (PANEL_GAP.value * density).toInt()
        val inset = (ICON_INSET.value * density).toInt()
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        // 减去 ICON_INSET：锚点是 48dp 触控框，图标本身比它窄 24dp
        val x = (anchorBounds.right - inset - popupContentSize.width).coerceIn(0, maxX)

        // 顶边从触控框底边往上提 12dp = 图标底边，面板才贴着图标
        val below = anchorBounds.bottom - inset + gap
        val above = anchorBounds.top + inset - gap - popupContentSize.height
        val y = when {
            below + popupContentSize.height <= windowSize.height -> below
            above >= 0 -> above
            else -> (windowSize.height - popupContentSize.height).coerceAtLeast(0)
        }
        return IntOffset(x, y)
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsButtonPreview() {
    AppTheme {
        NotificationsButton(
            items = DemoData.Components.notificationsTwo
        ) // ponytail: demo data only
    }
}

@Preview(name = "Notification Row", showBackground = true, widthDp = 360)
@Composable
fun NotificationRowPreview() {
    AppTheme {
        NotificationRow(
            item = DemoData.Components.notificationGoalDue,
            onClick = {},
            onDelete = {}
        ) // ponytail: demo data only
    }
}

@Preview(name = "Notifications Panel", showBackground = true, widthDp = 360)
@Composable
fun NotificationsPanelPreview() {
    AppTheme {
        NotificationsPanel(
            visibleState = remember { MutableTransitionState(true) },
            items = DemoData.Components.notificationsThree,
            onItemClick = {},
            onClearAll = {},
            onItemDelete = {}
        ) // ponytail: demo data only
    }
}

@Preview(name = "Notifications Panel Empty", showBackground = true, widthDp = 360)
@Composable
fun NotificationsPanelEmptyPreview() {
    AppTheme {
        NotificationsPanel(
            visibleState = remember { MutableTransitionState(true) },
            items = emptyList(),
            onItemClick = {},
            onClearAll = {},
            onItemDelete = {}
        ) // ponytail: demo data only
    }
}
