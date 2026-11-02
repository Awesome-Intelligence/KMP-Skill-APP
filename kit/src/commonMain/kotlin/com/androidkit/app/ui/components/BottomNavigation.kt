package com.androidkit.app.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.LocalReducedMotion
import com.androidkit.app.ui.theme.M3Motion

/**
 * Bottom Navigation Bar - 底部导航栏
 * 使用 M3 NavigationBarItem，indicatorColor 用 primary@12% 淡填充
 *
 * alwaysFilled 的项（如「快速添加」）是动作按钮而不是目的地：图标常驻一个圆形底色，
 * 点击不改变选中态。
 */
@Composable
fun AppBottomNavigation(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(index) },
                icon = {
                    if (item.alwaysFilled) {
                        // 常驻底色：primary 圆角长条 + onPrimary 图标，未选中也一直在
                        Box(
                            modifier = Modifier
                                .size(width = 48.dp, height = 32.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        BadgedBox(
                            badge = {
                                if (item.badgeCount != null && item.badgeCount > 0) {
                                    Badge {
                                        Text(if (item.badgeCount > 99) "99+" else item.badgeCount.toString())
                                    }
                                }
                            }
                        ) {
                            // 选中/未选中图标切换带淡入淡出（M3 SHORT2）；减动效直接切换
                            if (LocalReducedMotion.current) {
                                BottomNavIcon(item, selected)
                            } else {
                                Crossfade(
                                    targetState = selected,
                                    animationSpec = tween(M3Motion.SHORT2, easing = M3Motion.emphasizedDecelerate),
                                    label = "nav_icon"
                                ) { s ->
                                    BottomNavIcon(item, s)
                                }
                            }
                        }
                    }
                },
                // alwaysFilled 项是纯图标动作按钮：留一个空 Text 占位，
                // 否则 label 高度消失，整列垂直居中会把图标顶到和别项不一样高
                label = { if (item.alwaysFilled) Text("") else Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun BottomNavIcon(item: BottomNavItem, selected: Boolean) {
    Icon(
        imageVector = if (selected && item.selectedIcon != null) item.selectedIcon else item.icon,
        contentDescription = item.label,
        modifier = Modifier.size(24.dp)
    )
}

/**
 * Bottom Navigation Item - 底部导航项数据类
 * @param alwaysFilled 常驻圆形底色的动作按钮（点击不切换选中态）
 */
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val badgeCount: Int? = null,
    val alwaysFilled: Boolean = false
)

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AppBottomNavigationPreview() {
    AppTheme {
        AppBottomNavigation(
            items = DemoData.Components.bottomNavItems,
            selectedIndex = 0,
            onItemSelected = {}
        ) // ponytail: demo data only
    }
}

@Preview(name = "Bottom Nav Icon", showBackground = true)
@Composable
fun BottomNavIconPreview() {
    AppTheme {
        Row {
            BottomNavIcon(
                item = DemoData.Components.navHome,
                selected = false
            )
            BottomNavIcon(
                item = DemoData.Components.navHome,
                selected = true
            )
        } // ponytail: demo data only
    }
}
