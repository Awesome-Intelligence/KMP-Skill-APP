package com.androidkit.app.ui.components

import com.androidkit.app.ui.theme.M3Motion
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme

/**
 * Search Bar - 搜索框（Google 风格）
 * M3 Filled SearchBar：56dp 高度，surfaceVariant 背景，无边框
 * 支持右侧筛选按钮
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(Res.string.search_hint),
    enabled: Boolean = true,
    onFilterClick: (() -> Unit)? = null,
    filterActive: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val showClearButton = query.isNotEmpty()
    val showFilterButton = onFilterClick != null
    // 根据是否有清除按钮和筛选按钮动态调整文本输入区域
    val endPadding = when {
        showClearButton && showFilterButton -> 96.dp
        showClearButton || showFilterButton -> 48.dp
        else -> 0.dp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
        contentAlignment = Alignment.CenterStart
    ) {
        // Search icon
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = stringResource(Res.string.search),
            modifier = Modifier
                .padding(start = 12.dp)
                .size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Text field
        androidx.compose.foundation.text.BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp, end = endPadding)
                .align(Alignment.Center),
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch()
                    focusManager.clearFocus()
                }
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )

        // Trailing icons row
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clear button
            AnimatedVisibility(
                visible = showClearButton,
                enter = M3Motion.appear(),
                exit = M3Motion.disappear()
            ) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = stringResource(Res.string.clear),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Filter button
            AnimatedVisibility(
                visible = showFilterButton,
                enter = M3Motion.appear(),
                exit = M3Motion.disappear()
            ) {
                IconButton(
                    onClick = {
                        if (onFilterClick != null) {
                            onFilterClick.invoke()
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapVert,
                        contentDescription = stringResource(Res.string.filter),
                        modifier = Modifier.size(24.dp),
                        tint = if (filterActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

/**
 * 搜索开关按钮 —— 顶栏 action 用。
 *
 * 未展开时是放大镜，展开后变成收起箭头（向上）：区别于 X 关闭，
 * 语义是"把搜索框收回去"，同时清空已输入的搜索词。
 */
@Composable
fun SearchToggleButton(
    isSearchActive: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            imageVector = if (isSearchActive) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.Search,
            contentDescription = stringResource(if (isSearchActive) Res.string.collapse else Res.string.search),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBarPreview() {
    AppTheme {
        SearchBar(
            query = DemoData.Text.GOAL,
            onQueryChange = {},
            onSearch = {},
            onFilterClick = {},
            filterActive = false
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchToggleButtonPreview() {
    AppTheme {
        SearchToggleButton(isSearchActive = false, onToggle = {}) // ponytail: demo data only
    }
}
