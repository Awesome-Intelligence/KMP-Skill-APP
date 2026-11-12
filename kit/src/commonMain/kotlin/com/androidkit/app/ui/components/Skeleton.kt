package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import com.androidkit.app.ui.theme.themedCorner
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.rememberBreathingAlpha

/**
 * 骨架占位 - 符合 M3 规范
 * - 基础色：surfaceContainerHighest（深色模式下用 surfaceContainerLow）
 * - 动效：呼吸脉动（LONG1 + emphasized，与 [rememberBreathingAlpha] 同源）
 *   不用 M2 的 shimmer 缓动；系统关闭动画时不脉动
 */
@Composable
private fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small
) {
    val breathing = rememberBreathingAlpha(minAlpha = 0.55f)

    // M3 规范：浅色用 surfaceContainerHighest，深色用 surfaceContainerLow
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val baseColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(baseColor)
            .alpha(breathing)
    )
}

/**
 * 计算颜色亮度
 */
private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.299f * r + 0.587f * g + 0.114f * b
}

/**
 * 骨架文本行 - 用于替代 Text
 */
@Composable
fun SkeletonTextLine(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    lineHeight: Dp = 16.dp,
    cornerRadius: Dp = 8.dp
) {
    ShimmerBox(
        modifier = modifier
            .then(
                if (width != Dp.Unspecified) Modifier.width(width) else Modifier.fillMaxWidth()
            )
            .height(lineHeight),
        shape = themedCorner(cornerRadius)
    )
}

/**
 * 骨架圆角矩形 - 用于替代各种内容块
 */
@Composable
fun SkeletonRect(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    boxHeight: Dp = 60.dp,
    cornerRadius: Dp = 12.dp
) {
    ShimmerBox(
        modifier = modifier
            .then(
                if (width != Dp.Unspecified) Modifier.width(width) else Modifier.fillMaxWidth()
            )
            .height(boxHeight),
        shape = themedCorner(cornerRadius)
    )
}

/**
 * 骨架圆形头像
 */
@Composable
fun SkeletonCircle(
    modifier: Modifier = Modifier,
    circleSize: Dp = 48.dp
) {
    ShimmerBox(
        modifier = modifier.size(circleSize),
        shape = CircleShape
    )
}

/**
 * 骨架列表项
 */
@Composable
fun SkeletonListItem(
    modifier: Modifier = Modifier,
    showAvatar: Boolean = true,
    showSubtitle: Boolean = true,
    avatarSize: Dp = 48.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showAvatar) {
            SkeletonCircle(circleSize = avatarSize)
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonTextLine(lineHeight = 14.dp)
            if (showSubtitle) {
                SkeletonTextLine(lineHeight = 12.dp, width = 120.dp)
            }
        }
    }
}

/**
 * 骨架卡片
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    cardPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(cardPadding),
        content = content
    )
}

/**
 * 骨架网格项
 */
@Composable
fun SkeletonGridItem(
    modifier: Modifier = Modifier,
    itemHeight: Dp = 64.dp
) {
    ShimmerBox(
        modifier = modifier
            .fillMaxWidth()
            .height(itemHeight),
        shape = MaterialTheme.shapes.large
    )
}

// ==================== 预制骨架屏组合 ====================

/**
 * 首页收藏目标骨架屏
 */
@Composable
fun HomeFavoriteGoalsSkeleton(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SkeletonTextLine(width = 80.dp, lineHeight = 18.dp)
        Spacer(modifier = Modifier.height(12.dp))
        SkeletonCard {
            repeat(3) { index ->
                SkeletonListItem(showSubtitle = false)
                if (index < 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .padding(horizontal = 16.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

/**
 * 完整首页骨架屏
 */
@Composable
fun HomeScreenSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        HomeFavoriteGoalsSkeleton()
        Spacer(modifier = Modifier.height(100.dp))
    }
}

/**
 * 目标列表骨架屏
 */
@Composable
fun GoalListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 6
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 搜索框骨架
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.extraLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        // 标签栏骨架
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                SkeletonRect(
                    modifier = Modifier.width(60.dp),
                    boxHeight = 32.dp,
                    cornerRadius = 16.dp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // 列表项骨架
        repeat(itemCount) {
            SkeletonGridItem(itemHeight = 100.dp)
        }
    }
}

/**
 * 记录列表骨架屏
 */
@Composable
fun RecordListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 5
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 日期导航骨架
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            SkeletonTextLine(width = 150.dp, lineHeight = 24.dp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        // 标签栏骨架
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(5) {
                SkeletonRect(
                    modifier = Modifier.width(50.dp),
                    boxHeight = 32.dp,
                    cornerRadius = 16.dp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // 列表项骨架
        repeat(itemCount) {
            SkeletonCard {
                SkeletonTextLine(width = 120.dp, lineHeight = 12.dp)
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonTextLine(lineHeight = 16.dp)
                Spacer(modifier = Modifier.height(4.dp))
                SkeletonTextLine(lineHeight = 16.dp, width = 200.dp)
            }
        }
    }
}

/**
 * 联系人列表骨架屏
 */
@Composable
fun ContactListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 8
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 搜索框骨架
        SkeletonRect(
            modifier = Modifier.fillMaxWidth(),
            boxHeight = 56.dp,
            cornerRadius = 28.dp
        )
        Spacer(modifier = Modifier.height(12.dp))
        // 标签栏骨架
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(5) {
                SkeletonRect(
                    modifier = Modifier.width(55.dp),
                    boxHeight = 32.dp,
                    cornerRadius = 16.dp
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // 列表项骨架
        repeat(itemCount) {
            SkeletonListItem(
                showSubtitle = true,
                avatarSize = 48.dp
            )
        }
    }
}

/**
 * 目标详情骨架屏
 */
@Composable
fun GoalDetailSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 背景图骨架
        SkeletonRect(
            modifier = Modifier.fillMaxWidth(),
            boxHeight = 120.dp,
            cornerRadius = 12.dp
        )
        // 描述骨架
        SkeletonCard {
            SkeletonTextLine()
            Spacer(modifier = Modifier.height(4.dp))
            SkeletonTextLine(width = 200.dp)
        }
        // 标签栏骨架
        SkeletonCard {
            SkeletonTextLine(width = 60.dp, lineHeight = 12.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) {
                    SkeletonRect(
                        modifier = Modifier.width(70.dp),
                        boxHeight = 32.dp,
                        cornerRadius = 8.dp
                    )
                }
            }
        }
        // 子任务骨架
        SkeletonCard {
            SkeletonTextLine(width = 80.dp, lineHeight = 14.dp)
            Spacer(modifier = Modifier.height(8.dp))
            repeat(3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonCircle(circleSize = 24.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    SkeletonTextLine(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 联系人详情骨架屏
 */
@Composable
fun ContactDetailSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 头像骨架
        SkeletonCircle(circleSize = 120.dp)
        // 姓名骨架
        SkeletonTextLine(width = 150.dp, lineHeight = 24.dp)
        // 标签骨架
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(2) {
                SkeletonRect(
                    modifier = Modifier.width(60.dp),
                    boxHeight = 28.dp,
                    cornerRadius = 14.dp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // 备注骨架
        SkeletonCard {
            SkeletonTextLine(width = 60.dp, lineHeight = 12.dp)
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonTextLine()
            Spacer(modifier = Modifier.height(4.dp))
            SkeletonTextLine(width = 180.dp)
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun SkeletonTextLinePreview() {
    AppTheme {
        SkeletonTextLine(width = 160.dp) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun SkeletonRectPreview() {
    AppTheme {
        SkeletonRect(width = 160.dp) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun SkeletonCirclePreview() {
    AppTheme {
        SkeletonCircle() // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun SkeletonListItemPreview() {
    AppTheme {
        SkeletonListItem() // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun SkeletonCardPreview() {
    AppTheme {
        SkeletonCard {
            SkeletonTextLine()
            SkeletonTextLine(width = 120.dp)
        } // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun SkeletonGridItemPreview() {
    AppTheme {
        SkeletonGridItem() // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun HomeFavoriteGoalsSkeletonPreview() {
    AppTheme {
        HomeFavoriteGoalsSkeleton() // ponytail: demo data only
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun HomeScreenSkeletonPreview() {
    AppTheme {
        HomeScreenSkeleton() // ponytail: demo data only
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun GoalListSkeletonPreview() {
    AppTheme {
        GoalListSkeleton() // ponytail: demo data only
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun RecordListSkeletonPreview() {
    AppTheme {
        RecordListSkeleton() // ponytail: demo data only
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun ContactListSkeletonPreview() {
    AppTheme {
        ContactListSkeleton() // ponytail: demo data only
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun GoalDetailSkeletonPreview() {
    AppTheme {
        GoalDetailSkeleton() // ponytail: demo data only
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun ContactDetailSkeletonPreview() {
    AppTheme {
        ContactDetailSkeleton() // ponytail: demo data only
    }
}

@Preview(name = "Shimmer Box", showBackground = true, widthDp = 360)
@Composable
fun ShimmerBoxPreview() {
    AppTheme {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = MaterialTheme.shapes.medium
        ) // ponytail: demo data only
    }
}
