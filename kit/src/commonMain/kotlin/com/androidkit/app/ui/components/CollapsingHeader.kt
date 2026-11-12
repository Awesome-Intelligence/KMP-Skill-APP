package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme

/**
 * 标题行高度 = 收起后剩下的高度。
 */
private val TitleRowHeight = 64.dp

/**
 * 收起态头像槽宽度（「我的」页头像按这个尺寸画，两边一致）。
 */
val CollapsedIconSlotWidth = 32.dp

/**
 * 收起态头像实际大小（比槽位略小，视觉上更秀气）。
 */
val CollapsedAvatarSize = 24.dp

/** 头像与标题之间的间距 */
private val CollapsedIconGap = 8.dp

/**
 * 可折叠的大头部状态。
 *
 * 高度单位是 px：nested scroll 给的位移量就是 px，避免每帧做单位换算。
 */
@Stable
class CollapsingHeaderState {
    /** 展开高度兜底（px）：展开内容还没量出来时用 */
    var fallbackExpandedHeightPx: Float by mutableFloatStateOf(0f)

    /** 收起高度（px），也就是标题行高度 */
    var collapsedHeightPx: Float by mutableFloatStateOf(0f)

    /** 展开内容实测高度（px） */
    var contentHeightPx: Float by mutableFloatStateOf(0f)

    /** 已收起的高度（px）：0 = 完全展开 */
    var collapsedPx: Float by mutableFloatStateOf(0f)

    /**
     * 展开总高度（px）：标题行 + 内容实测。
     *
     * 量出内容后以实测为准 —— 兜底值只在首帧（还没量出来）用，
     * 不然卡片改矮后头部仍被兜底值撑着，卡片和页签之间多出一段空白。
     */
    val expandedHeightPx: Float
        get() = if (contentHeightPx > 0f) {
            collapsedHeightPx + contentHeightPx
        } else {
            maxOf(fallbackExpandedHeightPx, collapsedHeightPx + contentHeightPx)
        }

    /** 收起进度：0 = 展开，1 = 收起 */
    val progress: Float
        get() {
            val range = expandedHeightPx - collapsedHeightPx
            return if (range <= 0f) 0f else (collapsedPx / range).coerceIn(0f, 1f)
        }

    /** 当前头部高度（px） */
    val heightPx: Float get() = (expandedHeightPx - collapsedPx).coerceAtLeast(collapsedHeightPx)
}

/**
 * 头部收缩状态。
 *
 * [expandedHeight] 是展开高度的兜底值；实际高度由展开内容实测决定。
 * 换值（例如从"近期"切到 x记）会重建状态 —— 换模式即重新展开。
 */
@Composable
fun rememberCollapsingHeaderState(
    expandedHeight: Dp,
    collapsedHeight: Dp = TitleRowHeight
): CollapsingHeaderState {
    val density = LocalDensity.current
    return remember(expandedHeight, collapsedHeight) {
        CollapsingHeaderState().apply {
            fallbackExpandedHeightPx = with(density) { expandedHeight.toPx() }
            collapsedHeightPx = with(density) { collapsedHeight.toPx() }
        }
    }
}

/**
 * 可折叠的大头部。
 *
 * 展开时标题行下面挂 [expandedContent]（个人卡片等）；列表上滑时头部高度收到
 * [CollapsingHeaderState.collapsedHeightPx]，只留标题行。展开内容同时淡出，
 * [collapsedIcon]（如用户头像）在收起过程中淡入，落到标题左侧紧挨着。
 *
 * 收缩由 [Modifier.collapseHeaderOnScroll] 驱动。
 */
@Composable
fun CollapsingHeader(
    title: String,
    state: CollapsingHeaderState,
    modifier: Modifier = Modifier,
    titleIcon: (@Composable () -> Unit)? = null,
    collapsedIcon: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    expandedContent: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val density = LocalDensity.current
    val height = with(density) { state.heightPx.toDp() }
    val titleRowHeight = with(density) { state.collapsedHeightPx.toDp() }
    val progress = state.progress

    // 没有展开内容时清零实测高度，避免切页后头部还留着旧高度
    SideEffect {
        if (expandedContent == null) state.contentHeightPx = 0f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clipToBounds()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 标题行：收起后剩下的就是这一行 —— 标题始终居中，图标/动作钉在两侧
            // 插槽各自带 4dp 内边距，不给整行加水平 padding：
            // 整行 padding 会和 IconButton 自带的 12dp 叠加，图形离边 28dp，看着缩在里面。
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(titleRowHeight)
            ) {
                // 最左侧常驻插槽（如菜单按钮），展开/收起都在
                if (leading != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 4.dp)
                    ) {
                        leading()
                    }
                }

                if (collapsedIcon == null) {
                    // 左侧槽：标题图标钉在左侧，标题居中
                    if (titleIcon != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 4.dp)
                        ) {
                            titleIcon()
                        }
                    }
                    HeaderTitle(title = title, modifier = Modifier.align(Alignment.Center))
                } else {
                    // 收起态：头像紧挨标题左侧。
                    // 右边补一段等宽空位抵掉左边的头像槽，标题仍停在正中。
                    val collapseAlpha = ((progress - 0.5f) * 2f).coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 头像贴槽右缘：槽比头像宽，剩余空隙留在左侧，头像才挨着标题
                        Box(
                            modifier = Modifier.width(CollapsedIconSlotWidth),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (titleIcon != null) {
                                Box(modifier = Modifier.alpha(1f - progress)) {
                                    titleIcon()
                                }
                            }
                            Box(modifier = Modifier.alpha(collapseAlpha)) {
                                collapsedIcon()
                            }
                        }
                        Spacer(modifier = Modifier.width(CollapsedIconGap))
                        HeaderTitle(title = title)
                        Spacer(modifier = Modifier.width(CollapsedIconGap))
                        Spacer(modifier = Modifier.width(CollapsedIconSlotWidth))
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actions()
                }
            }

            if (expandedContent != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        // 不限高度地测量：大字号时内容不会被父级固定高度裁掉
                        .wrapContentHeight(unbounded = true)
                        .onSizeChanged { state.contentHeightPx = it.height.toFloat() }
                        .alpha((1f - progress * 1.5f).coerceIn(0f, 1f))
                        // 头部自己也能上下滑：手势落在头部时列表是兄弟节点、收不到手势，
                        // 没有这段就变成"滑卡片不动、滑下面才动"
                        .pointerInput(state) {
                            detectVerticalDragGestures { _, dragAmount ->
                                val range = state.expandedHeightPx - state.collapsedHeightPx
                                if (range <= 0f) return@detectVerticalDragGestures
                                // 手指上滑（dragAmount 为负）→ 收起；下滑 → 展开
                                state.collapsedPx =
                                    (state.collapsedPx - dragAmount).coerceIn(0f, range)
                            }
                        }
                ) {
                    expandedContent()
                }
            }
        }
    }
}

/**
 * 标题行上的文字（展开/收起两种布局共用一份样式）。
 */
@Composable
private fun HeaderTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * 挂到滚动容器上：列表在顶部时上滑收头部，列表滚回顶部后下滑展开头部。
 *
 * 收起的位移优先被头部吃掉（列表本身不动），头部变矮后内容整体上移 —— 跟手，不跑偏。
 */
fun Modifier.collapseHeaderOnScroll(
    state: CollapsingHeaderState,
    listState: LazyListState
): Modifier = nestedScroll(collapsingHeaderConnection(state, listState))

/**
 * 瀑布流版本（"近期"页签）：网格滚动状态同样只提供"能不能往回滚"。
 */
fun Modifier.collapseHeaderOnScroll(
    state: CollapsingHeaderState,
    gridState: LazyStaggeredGridState
): Modifier = nestedScroll(collapsingHeaderConnection(state) { gridState.canScrollBackward })

/**
 * 头部的嵌套滚动逻辑（抽出来是为了能直接测，不用手势）。
 */
internal fun collapsingHeaderConnection(
    state: CollapsingHeaderState,
    listState: LazyListState
): NestedScrollConnection = collapsingHeaderConnection(state) { listState.canScrollBackward }

internal fun collapsingHeaderConnection(
    state: CollapsingHeaderState,
    canScrollBackward: () -> Boolean
): NestedScrollConnection = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y
        if (delta == 0f) return Offset.Zero

        val range = state.expandedHeightPx - state.collapsedHeightPx
        if (range <= 0f) return Offset.Zero

        return if (delta < 0f) {
            // 上滑：列表已经在顶部才收头部，否则让列表自己滚
            if (canScrollBackward()) {
                Offset.Zero
            } else {
                // 还能收多少 = 总范围 - 已收
                val consume = minOf(-delta, range - state.collapsedPx)
                state.collapsedPx += consume
                Offset(0f, -consume)
            }
        } else {
            // 下滑：列表在顶部且头部没完全展开才展开头部
            if (state.collapsedPx <= 0f || canScrollBackward()) {
                Offset.Zero
            } else {
                val consume = minOf(delta, state.collapsedPx)
                state.collapsedPx -= consume
                Offset(0f, consume)
            }
        }
    }

    /**
     * 列表滚回顶部后剩下的位移拿来展开头部。
     *
     * 没有这段：一次下滑手势把列表拉回顶部后，手势就结束了，头部还收着 —— 得再滑一次才展开。
     */
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (available.y <= 0f) return Offset.Zero
        if (state.collapsedPx <= 0f || canScrollBackward()) return Offset.Zero
        val consume = minOf(available.y, state.collapsedPx)
        state.collapsedPx -= consume
        return Offset(0f, consume)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun CollapsingHeaderPreview() {
    AppTheme {
        val state = rememberCollapsingHeaderState(expandedHeight = 200.dp)
        CollapsingHeader(
            title = DemoData.Text.MINE,
            state = state,
            expandedContent = {
                Text(
                    text = DemoData.Text.EXPANDED_CONTENT,
                    modifier = Modifier.padding(16.dp)
                )
            }
        ) // ponytail: demo data only
    }
}

@Preview(name = "Header Title", showBackground = true, widthDp = 360)
@Composable
fun HeaderTitlePreview() {
    AppTheme {
        HeaderTitle(title = DemoData.Text.MINE) // ponytail: demo data only
    }
}
