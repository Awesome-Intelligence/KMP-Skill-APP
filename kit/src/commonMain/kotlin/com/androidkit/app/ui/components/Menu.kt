package com.androidkit.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * Simple string-based menu
 */
@Composable
fun SimpleMenuButton(
    items: List<SimpleMenuItem>,
    onItemSelected: (parentIndex: Int, subIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(Res.string.more_options)
) {
    var expanded by remember { mutableStateOf(false) }
    var activeSubMenu by remember { mutableStateOf<Int?>(null) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = contentDescription
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                activeSubMenu = null
            }
        ) {
            items.forEachIndexed { index, item ->
                if (item.isDivider) {
                    HorizontalDivider()
                } else if (item.isSubMenuParent) {
                    // 子菜单入口 - 点击切换子菜单展开状态
                    DropdownMenuItem(
                        text = { Text(item.title) },
                        onClick = {
                            activeSubMenu = if (activeSubMenu == index) null else index
                        },
                        leadingIcon = item.icon?.let { icon ->
                            { Icon(icon, contentDescription = null) }
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = stringResource(Res.string.more_options)
                            )
                        }
                    )
                    // 子菜单项直接显示在父菜单下方（平面展开）
                    if (activeSubMenu == index) {
                        item.subItems.forEachIndexed { subIndex, subItem ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 32.dp)
                                    ) {
                                        Text(subItem.title)
                                    }
                                },
                                onClick = {
                                    onItemSelected(index, subIndex)
                                    expanded = false
                                    activeSubMenu = null
                                },
                                leadingIcon = subItem.icon?.let { icon ->
                                    { Icon(icon, contentDescription = null) }
                                },
                                trailingIcon = subItem.trailingIcon?.let { icon ->
                                    { Icon(icon, contentDescription = null) }
                                }
                            )
                        }
                    }
                } else {
                    DropdownMenuItem(
                        text = { Text(item.title) },
                        onClick = {
                            onItemSelected(index, -1)
                            expanded = false
                            activeSubMenu = null
                        },
                        leadingIcon = item.icon?.let { icon ->
                            { Icon(icon, contentDescription = null) }
                        },
                        trailingIcon = item.trailingIcon?.let { icon ->
                            { Icon(icon, contentDescription = null) }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Simple menu item without generic
 * isDivider = true 时渲染为分割线，title/icon 被忽略，且不触发 onItemSelected
 * （onItemSelected 收到的 index 仍为 item 在列表中的位置，含分割线）
 * isSubMenuParent = true 时渲染为带子菜单的一级入口，subItems 提供子选项
 */
data class SimpleMenuItem(
    val title: String = "",
    val icon: ImageVector? = null,
    val trailingIcon: ImageVector? = null,
    val isDivider: Boolean = false,
    val isSubMenuParent: Boolean = false,
    val subItems: List<SimpleMenuItem> = emptyList()
)

/**
 * Row of actions (编辑、删除等) - 常用快捷菜单
 */
@Composable
fun ActionMenuRow(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        SimpleMenuButton(
            items = listOf(
                SimpleMenuItem(title = stringResource(Res.string.edit), icon = Icons.Default.Edit),
                SimpleMenuItem(title = stringResource(Res.string.delete), icon = Icons.Default.Delete)
            ),
            onItemSelected = { parentIndex, _ ->
                when (parentIndex) {
                    0 -> onEdit()
                    1 -> onDelete()
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun SimpleMenuButtonPreview() {
    AppTheme {
        SimpleMenuButton(
            items = DemoData.Components.menuItems, // ponytail: demo data only
            onItemSelected = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun ActionMenuRowPreview() {
    AppTheme {
        ActionMenuRow(onEdit = {}, onDelete = {})
    }
}
