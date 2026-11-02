package com.androidkit.app.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.androidkit.app.ui.theme.LocalReducedMotion

/**
 * 底部导航「已选中项再点一次」的联动：tick 变化时执行 scrollToTop（平滑回顶部/回第一个页签）。
 *
 * consumed 初始化为当前 tick —— 首次组合、从别的 tab 切回来都不触发，只有 reselect 那一刻触发。
 * MainActivity 在 reselect 时对 tick 自增，把同一个 tick 传给各顶层页面即可，页面各自决定怎么响应。
 */
@Composable
fun ScrollToTopEffect(tick: Int, scrollToTop: suspend () -> Unit) {
    var consumed by remember { mutableIntStateOf(tick) }
    LaunchedEffect(tick) {
        if (tick != consumed) {
            consumed = tick
            scrollToTop()
        }
    }
}

/** LazyColumn 版：平滑滚回首项，系统减动效时直接跳 */
@Composable
fun ScrollToTopEffect(tick: Int, listState: LazyListState) {
    val reducedMotion = LocalReducedMotion.current
    ScrollToTopEffect(tick) {
        if (reducedMotion) listState.scrollToItem(0) else listState.animateScrollToItem(0)
    }
}

/** LazyVerticalGrid 版 */
@Composable
fun ScrollToTopEffect(tick: Int, gridState: LazyGridState) {
    val reducedMotion = LocalReducedMotion.current
    ScrollToTopEffect(tick) {
        if (reducedMotion) gridState.scrollToItem(0) else gridState.animateScrollToItem(0)
    }
}

/** verticalScroll 版 */
@Composable
fun ScrollToTopEffect(tick: Int, scrollState: ScrollState) {
    val reducedMotion = LocalReducedMotion.current
    ScrollToTopEffect(tick) {
        if (reducedMotion) scrollState.scrollTo(0) else scrollState.animateScrollTo(0)
    }
}
