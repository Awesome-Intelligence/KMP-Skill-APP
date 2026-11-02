package com.androidkit.app.ui.templates

import com.androidkit.app.ui.theme.M3Motion
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.res.*
import com.androidkit.app.ui.components.AppCard
import com.androidkit.app.ui.components.EmptyStates
import com.androidkit.app.ui.components.FilterChip
import com.androidkit.app.ui.components.IllustratedEmptyState
import com.androidkit.app.ui.components.Header
import com.androidkit.app.ui.components.SearchBar
import com.androidkit.app.ui.components.AdaptiveGridCells
import com.androidkit.app.ui.components.rememberGridSpacing
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.res.Res.string as RString

/**
 * 列表页模板 - 搜索 + 筛选 + 列表的标准套路
 * 所有列表页实现须参考此模板结构
 *
 * **使用方式**：
 * ```
 * Scaffold(floatingActionButton = { ... }) {
 *     ListScreen(...)
 * }
 * ```
 * 不要在 Scaffold 和 ListScreen 之间加任何容器。
 *
 * M3 搜索规范：搜索框默认隐藏，点击搜索图标后展开
 *
 * @param headerContentTop 标题栏下方、搜索/筛选区域上方的额外内容（如日期导航栏）
 * @param headerContentBottom 筛选标签下方的额外内容（如第二个筛选行）
 * @param footerContent 列表下方的额外内容（如 FloatingToolbar）
 * @param itemContent 列表项渲染，接收 item，返回 Composable
 */
enum class ListViewMode { LIST, GALLERY, CALENDAR }

@Composable
fun <T> ListScreen(
    title: String,
    onSearchClick: () -> Unit,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilterIndex: Int,
    onFilterSelected: (Int) -> Unit,
    filterOptions: List<String>,
    items: List<T>,
    onItemClick: (T) -> Unit,
    itemContent: @Composable (T) -> Unit,
    emptyState: @Composable () -> Unit,
    noResultsState: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    // 扩展槽位
    headerContentTop: (@Composable () -> Unit)? = null,
    headerContentBottom: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
    // 标题栏右侧按钮
    action2: (@Composable () -> Unit)? = null,
    // 视图模式（默认列表，可切换画廊/日历）
    viewMode: ListViewMode = ListViewMode.LIST,
    // 画廊视图时的列数
    galleryColumns: Int = 2,
    // 日历视图内容（视图模式为 CALENDAR 时替代列表渲染，无视空状态逻辑）
    calendarContent: (@Composable () -> Unit)? = null
) {
    // 筛选与搜索由各页面的 ViewModel 负责，items 传入时已是过滤/排序结果，模板仅做展示
    val filteredItems = items

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Header(
            title = if (isSearchActive) "" else title,
            leading1 = if (isSearchActive) {
                {
                    IconButton(onClick = { onSearchActiveChange(false) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else null,
            action1 = if (!isSearchActive) {
                {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(Res.string.search),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else null,
            action2 = action2
        )

        Column {
            // 搜索区域（含 headerContentTop）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // SearchBar + FilterChip
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // SearchBar - 搜索框（M3 展开/收起动画）
                    // shrinkVertically + fadeOut 同时进行，搜索框在淡出时同步收缩布局，
                    // 避免点击列表项时动画未完成导致列表向上跳动
                    AnimatedVisibility(
                        visible = isSearchActive,
                        enter = M3Motion.expandSection(),
                        exit = M3Motion.collapseSection()
                    ) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = onSearchQueryChange,
                            onSearch = { }
                        )
                    }

                    AnimatedVisibility(
                        visible = !isSearchActive,
                        enter = M3Motion.expandSection(),
                        exit = M3Motion.collapseSection()
                    ) {
                        // FilterChip - 筛选标签
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filterOptions.size) { index ->
                                FilterChip(
                                    title = filterOptions[index],
                                    isSelected = selectedFilterIndex == index,
                                    onClick = { onFilterSelected(index) }
                                )
                            }
                        }
                    }
                }

                // 额外顶部内容（如日期导航栏）
                headerContentTop?.invoke()
            }

            // 额外底部筛选内容
            headerContentBottom?.invoke()

            // 列表内容
            if (viewMode == ListViewMode.CALENDAR && calendarContent != null) {
                // 日历视图始终渲染（空数据时日历本身仍有意义）
                calendarContent()
            } else {
                // 空态 ↔ 列表用 Fade Through 切换（M3：无关联内容替换，不走骨架硬切）
                val contentTransition = M3Motion.fadeThrough()
                AnimatedContent(
                    targetState = filteredItems.isEmpty(),
                    transitionSpec = { contentTransition },
                    label = "list_empty_toggle"
                ) { isEmpty ->
                    if (isEmpty) {
                        if (searchQuery.isNotEmpty() || selectedFilterIndex != 0) {
                            // IllustratedEmptyState - 空结果
                            noResultsState()
                        } else {
                            // EmptyStates - 空状态
                            emptyState()
                        }
                    } else {
                        when (viewMode) {
                            ListViewMode.GALLERY -> {
                                val gridSpacing = rememberGridSpacing()
                                LazyVerticalGrid(
                                    // 132dp：小屏(320dp)扣边距后仍排 2 列，大屏自动加列
                                    columns = AdaptiveGridCells(),
                                    contentPadding = PaddingValues(gridSpacing.margin),
                                    horizontalArrangement = Arrangement.spacedBy(gridSpacing.gutter),
                                    verticalArrangement = Arrangement.spacedBy(gridSpacing.gutter)
                                ) {
                                    items(
                                        items = filteredItems,
                                        key = { (it as Any).hashCode() }
                                    ) { item ->
                                        Box(
                                            modifier = Modifier.animateItem(
                                                fadeInSpec = null,
                                                placementSpec = M3Motion.itemPlacement(),
                                                fadeOutSpec = M3Motion.fastEffects()
                                            )
                                        ) {
                                            itemContent(item)
                                        }
                                    }
                                }
                            }
                            ListViewMode.LIST -> {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = filteredItems,
                                        key = { (it as Any).hashCode() }
                                    ) { item ->
                                        Box(
                                            modifier = Modifier.animateItem(
                                                fadeInSpec = null,
                                                placementSpec = M3Motion.itemPlacement(),
                                                fadeOutSpec = M3Motion.fastEffects()
                                            )
                                        ) {
                                            itemContent(item)
                                        }
                                    }
                                }
                            }
                            ListViewMode.CALENDAR -> {
                                // calendarContent 为空时退回列表渲染
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = filteredItems,
                                        key = { (it as Any).hashCode() }
                                    ) { item ->
                                        Box(
                                            modifier = Modifier.animateItem(
                                                fadeInSpec = null,
                                                placementSpec = M3Motion.itemPlacement(),
                                                fadeOutSpec = M3Motion.fastEffects()
                                            )
                                        ) {
                                            itemContent(item)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 额外底部内容（如 FloatingToolbar）
            footerContent?.invoke()
        }
    }
}

/**
 * 列表项数据模型
 */
data class ListItem(
    val id: String,
    val name: String,
    val category: String,
    val isEmergency: Boolean = false
)

/**
 * 列表项卡片
 */
@Composable
fun ListItemCard(
    item: ListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像/图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.name.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 紧急标识
            if (item.isEmergency) {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.template_emergency),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

/**
 * 列表页模板 Preview
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "List Screen", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun ListScreenPreview(onBackClick: (() -> Unit)? = null) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) }
    var isSearchActive by remember { mutableStateOf(false) }

    val filterOptions = listOf(
        stringResource(RString.filter_all_template),
        stringResource(RString.filter_family_template),
        stringResource(RString.filter_close_friend_template),
        stringResource(RString.filter_colleague_template),
        stringResource(RString.filter_other_template)
    )
    // ponytail: demo data only
    val items = listOf(
        ListItem("1", stringResource(Res.string.demo_item_1), stringResource(Res.string.demo_category_1), true),
        ListItem("2", stringResource(Res.string.demo_item_2), stringResource(Res.string.demo_category_2), false),
        ListItem("3", stringResource(Res.string.demo_item_3), stringResource(Res.string.demo_category_3), true),
        ListItem("4", stringResource(Res.string.demo_item_4), stringResource(Res.string.demo_category_4), false),
        ListItem("5", stringResource(Res.string.demo_item_5), stringResource(Res.string.demo_category_1), true),
        ListItem("6", stringResource(Res.string.demo_item_6), stringResource(Res.string.demo_category_4), false),
        ListItem("7", stringResource(Res.string.demo_item_7), stringResource(Res.string.demo_category_3), true),
        ListItem("8", stringResource(Res.string.demo_item_8), stringResource(Res.string.demo_category_4), false)
    )

    ListScreen(
        title = stringResource(Res.string.template),
        onSearchClick = { isSearchActive = true },
        isSearchActive = isSearchActive,
        onSearchActiveChange = { isSearchActive = it },
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        selectedFilterIndex = selectedFilter,
        onFilterSelected = { selectedFilter = it },
        filterOptions = filterOptions,
        items = items,
        onItemClick = { },
        itemContent = { item ->
            ListItemCard(item = item, onClick = { })
        },
        action2 = onBackClick?.let {
            {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.back),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        emptyState = {
            EmptyStates.Contacts()
        },
        noResultsState = {
            IllustratedEmptyState(
                icon = Icons.Outlined.Search,
                title = stringResource(Res.string.no_results_found_template),
                subtitle = stringResource(Res.string.try_different_search_template),
                tintColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Preview(name = "List Item Card", showBackground = true, widthDp = 360)
@Composable
fun ListItemCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ListItemCard(
                item = DemoData.Components.listItemZhang,
                onClick = {}
            )
            ListItemCard(
                item = DemoData.Components.listItemLi,
                onClick = {}
            )
            ListItemCard(
                item = DemoData.Components.listItemWang,
                onClick = {}
            )
        } // ponytail: demo data only
    }
}
