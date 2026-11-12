package com.androidkit.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import com.androidkit.app.ui.theme.LocalReducedMotion
import com.androidkit.app.ui.theme.LocalThemeStyle
import com.androidkit.app.ui.theme.ThemeStyle
import com.androidkit.app.ui.theme.themedElevation
import com.androidkit.app.ui.theme.M3Motion
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme

/**
 * AvatarStack - 联系人头像堆叠组件
 *
 * 两种显示模式：
 * 1. Compact 模式（默认）：
 *    - 数量少（≤maxVisible）时：头像横向排列，不重叠
 *    - 数量多时：所有头像堆叠显示，重叠 1/3，+N badge 重叠在最右边
 * 2. Expanded 模式（点击后）：横向展开显示所有联系人，带 M3 规范动画
 *
 * 展开模式下，点击单个头像才显示该头像的删除按钮
 *
 * 符合 M3 规范：
 * - Avatar 尺寸 40dp，触控目标 48dp
 * - 展开动画使用 M3 spatial spring（见 M3Motion）
 * - 深色模式适配
 */
@Composable
fun AvatarStack(
    contacts: List<ContactItem>,
    onContactClick: (ContactItem) -> Unit,
    onRemoveContact: ((ContactItem) -> Unit)? = null,
    modifier: Modifier = Modifier,
    maxVisible: Int = 4,
    avatarSize: Dp = 40.dp
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedContactId by remember { mutableStateOf<String?>(null) }

    if (contacts.isEmpty()) {
        return
    }

    val hiddenCount = contacts.size - maxVisible
    val shouldOverlap = contacts.size > maxVisible

    // 重叠量：每个头像向左偏移重叠 1/3
    val overlapOffset = avatarSize / 3 * 2

    // 整体 scale 动画（展开时微微放大）—— 缩放不是位置/尺寸，用不过冲的 effects spring
    // transitionSpec 是非 @Composable lambda，只能在 Composable 作用域里取值后闭包捕获
    val reducedMotion = LocalReducedMotion.current
    // 瑞士风格：网格几何不许被交互扰动 —— 展开不缩放
    val swiss = LocalThemeStyle.current == ThemeStyle.Swiss
    // macOS 毛玻璃规范：动效只改颜色，不缩放
    val macos = LocalThemeStyle.current == ThemeStyle.MacOS
    val scale by animateFloatAsState(
        targetValue = if (isExpanded && !swiss && !macos) 1.05f else 1f,
        animationSpec = M3Motion.defaultEffects(),
        label = "scale"
    )

    // 展开/收起标签
    val expandLabel = if (isExpanded) stringResource(Res.string.collapse) else stringResource(Res.string.expand)

    Row(
        modifier = modifier
            .scale(scale),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar 区域
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .clip(RoundedCornerShape(avatarSize / 2))
                .clickable(
                    onClick = {
                        if (shouldOverlap) {
                            isExpanded = !isExpanded
                            if (!isExpanded) {
                                selectedContactId = null
                            }
                        }
                    },
                    onClickLabel = expandLabel
                )
                .padding(horizontal = if (shouldOverlap) 4.dp else 0.dp)
        ) {
            // 头像列表 - 带进入/退出动画
            AnimatedContent(
                targetState = isExpanded to contacts,
                transitionSpec = {
                    if (reducedMotion || macos) {
                        EnterTransition.None togetherWith ExitTransition.None
                    } else if (targetState.first) {
                        // 展开：旧内容先滑出，新内容从左滑入
                        (slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(M3Motion.MEDIUM1, easing = M3Motion.emphasizedDecelerate)
                        ) + fadeIn(
                            animationSpec = tween(M3Motion.SHORT3, delayMillis = M3Motion.SHORT1, easing = M3Motion.emphasizedDecelerate)
                        )).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(M3Motion.SHORT2, easing = M3Motion.emphasizedAccelerate)
                            ) + fadeOut(animationSpec = tween(M3Motion.SHORT2, easing = M3Motion.emphasizedAccelerate))
                        )
                    } else {
                        // 收起：新内容从右滑回，旧内容先让位
                        (slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(M3Motion.MEDIUM1, easing = M3Motion.emphasizedDecelerate)
                        ) + fadeIn(
                            animationSpec = tween(M3Motion.SHORT3, easing = M3Motion.emphasizedDecelerate)
                        )).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(M3Motion.MEDIUM1, easing = M3Motion.emphasizedAccelerate)
                            ) + fadeOut(animationSpec = tween(M3Motion.SHORT2, easing = M3Motion.emphasizedAccelerate))
                        )
                    }
                },
                label = "avatarContent"
            ) { (expanded, contactList) ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (expanded) {
                        // 展开模式：横向排列，不重叠
                        contactList.forEach { contact ->
                            AvatarItem(
                                contact = contact,
                                onClick = {
                                    // 点击头像：进入详情页，同时选中显示删除按钮
                                    selectedContactId = if (selectedContactId == contact.id) null else contact.id
                                    onContactClick(contact)
                                },
                                onRemoveClick = if (onRemoveContact != null) {
                                    {
                                        onRemoveContact(contact)
                                        selectedContactId = null
                                    }
                                } else null,
                                avatarSize = avatarSize,
                                offsetX = 0.dp,
                                showRemove = selectedContactId == contact.id && onRemoveContact != null
                            )
                        }
                    } else {
                        // Compact 模式
                        val visibleContacts = contactList.take(maxVisible)
                        visibleContacts.forEachIndexed { index, contact ->
                            AvatarItem(
                                contact = contact,
                                onClick = { onContactClick(contact) },
                                onRemoveClick = null,
                                avatarSize = avatarSize,
                                offsetX = if (shouldOverlap) -overlapOffset * index else 0.dp,
                                showRemove = false
                            )
                        }

                        // +N Badge
                        if (shouldOverlap) {
                            Badge(
                                count = hiddenCount,
                                avatarSize = avatarSize,
                                overlapOffset = overlapOffset,
                                onClick = { isExpanded = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 单个联系人头像项
 */
@Composable
private fun AvatarItem(
    contact: ContactItem,
    onClick: () -> Unit,
    onRemoveClick: (() -> Unit)?,
    avatarSize: Dp,
    offsetX: Dp,
    showRemove: Boolean
) {
    val clickLabel = if (showRemove) stringResource(Res.string.hide_remove_button) else contact.name
    val avatarDescription = stringResource(Res.string.avatar_content_description, contact.name)
    val reducedMotion = LocalReducedMotion.current
    val macos = LocalThemeStyle.current == ThemeStyle.MacOS

    Box(
        modifier = Modifier
            .offset(x = offsetX)
            .size(avatarSize + 8.dp) // 48dp 触控目标
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(
                    onClick = onClick,
                    onClickLabel = clickLabel
                )
                .semantics {
                    contentDescription = avatarDescription
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.firstOrNull()?.toString() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // 移除按钮（点击头像后显示，带缩放动画）
        if (onRemoveClick != null) {
            val removeDescription = stringResource(Res.string.remove)
            AnimatedVisibility(
                visible = showRemove,
                enter = if (reducedMotion || macos) EnterTransition.None else scaleIn(
                    animationSpec = M3Motion.defaultSpatial()
                ) + fadeIn(tween(M3Motion.SHORT2, easing = M3Motion.emphasizedDecelerate)),
                exit = if (reducedMotion || macos) ExitTransition.None else scaleOut(
                    animationSpec = tween(M3Motion.SHORT2, easing = M3Motion.emphasizedAccelerate)
                ) + fadeOut(tween(M3Motion.SHORT2, easing = M3Motion.emphasizedAccelerate))
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .clickable(onClick = onRemoveClick)
                        .semantics {
                            contentDescription = removeDescription
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * +N Badge - 显示超出数量的联系人，重叠在头像上面
 */
@Composable
private fun Badge(
    count: Int,
    avatarSize: Dp,
    overlapOffset: Dp,
    onClick: () -> Unit
) {
    val expandLabel = stringResource(Res.string.expand)
    val moreContactsDescription = stringResource(Res.string.show_more_contacts, count)

    Box(
        modifier = Modifier
            .offset(x = -overlapOffset)
            .size(avatarSize + 8.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable(
                    onClick = onClick,
                    onClickLabel = expandLabel
                )
                .semantics {
                    contentDescription = moreContactsDescription
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+$count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/**
 * Expanded Avatar Row - 展开模式下的横向滚动列表（备用组件）
 */
@Composable
fun ExpandedAvatarRow(
    contacts: List<ContactItem>,
    onContactClick: (ContactItem) -> Unit,
    onRemoveContact: ((ContactItem) -> Unit)? = null,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 40.dp
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        contacts.forEach { contact ->
            Surface(
                modifier = Modifier.clickable { onContactClick(contact) },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = themedElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val avatarDescription = stringResource(Res.string.avatar_content_description, contact.name)
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .semantics {
                                contentDescription = avatarDescription
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.name.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (onRemoveContact != null) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .clickable { onRemoveContact(contact) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.remove),
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 联系人数据类
 */
data class ContactItem(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val subtitle: String? = null
)

@Preview(showBackground = true)
@Composable
private fun AvatarStackPreview() {
    AppTheme {
        AvatarStack(
            contacts = DemoData.Components.avatarStackFour,
            onContactClick = {},
            onRemoveContact = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpandedAvatarRowPreview() {
    AppTheme {
        ExpandedAvatarRow(
            contacts = DemoData.Components.avatarStackThree,
            onContactClick = {},
            onRemoveContact = {}
        ) // ponytail: demo data only
    }
}

@Preview(name = "Avatar Item", showBackground = true)
@Composable
fun AvatarItemPreview() {
    AppTheme {
        AvatarItem(
            contact = DemoData.Components.avatarZhang,
            onClick = {},
            onRemoveClick = null,
            avatarSize = 40.dp,
            offsetX = 0.dp,
            showRemove = false
        ) // ponytail: demo data only
    }
}

@Preview(name = "Avatar Badge", showBackground = true)
@Composable
fun AvatarBadgePreview() {
    AppTheme {
        Badge(
            count = DemoData.States.mine.goalCount,
            avatarSize = 40.dp,
            overlapOffset = 26.dp,
            onClick = {}
        ) // ponytail: demo data only
    }
}
