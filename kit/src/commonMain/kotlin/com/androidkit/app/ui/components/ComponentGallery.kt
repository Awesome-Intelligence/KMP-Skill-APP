package com.androidkit.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.theme.ThemeStyle
import kotlinx.coroutines.launch

/**
 * 单个样例的高度上限。
 *
 * 整页模板（AppShell / ListScreen / FormScreen / SettingsScreen）在目录里要有个"手机屏"的高度才量得出来；
 * 其余样例比它矮，按内容高度排版。
 */
private val SampleMaxHeight = 640.dp

/**
 * 组件目录页 —— 把 kit 的组件按分类列一遍。
 *
 * 上面一排 chip 是分类索引（点了跳到那一栏），下面按分类分段，每段一个粘性标题，
 * 段里每行是「组件名 + 样例」。
 *
 * 样例从哪来见 [showcaseCategories] —— 复用各组件文件里的 `*Preview` 函数。
 *
 * @param style 当前主题风格。四个风格家族各是一套组件，只显示当前那套。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ComponentGallery(
    style: ThemeStyle,
    modifier: Modifier = Modifier,
) {
    val categories = remember(style) { showcaseCategories(style) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 每个分类在 LazyColumn 里的起始下标（分类标题自己占一行）
    val starts = remember(categories) {
        var index = 0
        categories.map { category ->
            index.also { index += 1 + category.items.size }
        }
    }

    // 当前滚到哪一栏：最后一个已经露头的分类标题
    val current by remember(categories, starts) {
        derivedStateOf {
            val visible = listState.firstVisibleItemIndex
            starts.indexOfLast { it <= visible }.coerceAtLeast(0)
        }
    }

    Column(modifier = modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            itemsIndexed(categories) { index, category ->
                FilterChip(
                    title = category.title,
                    isSelected = index == current,
                    onClick = {
                        scope.launch { listState.animateScrollToItem(starts[index]) }
                    },
                )
            }
        }

        // weight 不是排版偏好，是硬要求：Column 给「没有 weight 的子项」的高度上限是无限大，
        // LazyColumn 量到无限高会直接抛 "measured with an infinity maximum height constraints"。
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            categories.forEach { category ->
                stickyHeader(key = category.title) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 8.dp),
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                items(category.items, key = { it.name }) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(6.dp))
                        // 样例也要有高度上限：LazyColumn 的 item 只约束宽度，高度是无限的，
                        // 而目录里有些样例自己就是可滚动容器（ChatMessageList 的 LazyColumn、
                        // MarkdownPreview 的 verticalScroll、整页模板的 fillMaxSize）——
                        // 它们拿到无限高会崩。heightIn(max) 把上限收成一个确定值，
                        // 内容比上限矮的样例照旧按内容高度排版，不受影响。
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = SampleMaxHeight),
                        ) {
                            item.sample()
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
