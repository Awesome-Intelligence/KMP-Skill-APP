package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 瑞士国际风格的布局骨架 —— 规范「Layout」段：
 *
 * - 十二栏网格，页面最大宽度 1200px，栏间距 24px
 * - 分区间距 96px，元素间距 16px
 * - 分区之间用整幅 1px 黑线划分，**不用卡片框**
 * - 一律左对齐，绝不居中
 *
 * 这些是**布局件**，业务页面自己挑用；kit 其它组件不强制改造成网格。
 */
object SwissLayout {
    /** 栏数 */
    const val COLUMNS = 12

    /** 栏间距 */
    val Gutter: Dp = 24.dp

    /** 页面最大宽度 */
    val MaxWidth: Dp = 1200.dp

    /** 分区间距 */
    val SectionGap: Dp = 96.dp

    /** 元素间距 */
    val ElementGap: Dp = 16.dp

    /** 窄于这个宽度就放弃十二栏，竖着排（规范要求窄屏不横向溢出） */
    val NarrowBreakpoint: Dp = 640.dp
}

/**
 * 页面容器 —— 宽度封顶 1200px，**左对齐**（不居中）。
 */
@Composable
fun SwissPage(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.widthIn(max = SwissLayout.MaxWidth), content = content)
}

/**
 * 栏位校验 —— 抽成纯函数好测（Compose 里没法直接断言 require 抛出）。
 */
internal fun validateGridSpans(spans: List<Int>) {
    require(spans.isNotEmpty()) { "SwissGrid 至少要一栏" }
    spans.forEach { span ->
        require(span in 1..SwissLayout.COLUMNS) { "span 必须在 1..${SwissLayout.COLUMNS} 之间，收到 $span" }
    }
    require(spans.sum() <= SwissLayout.COLUMNS) {
        "span 之和不能超过 ${SwissLayout.COLUMNS}，收到 ${spans.sum()}"
    }
}

/**
 * 十二栏网格 —— 每栏给一个 span（1..12），span 之和不超过 12。
 *
 * 窄屏自动竖排，绝不横向溢出。
 *
 * @param columns 栏定义：(span, 内容)
 */
@Composable
fun SwissGrid(
    columns: List<Pair<Int, @Composable () -> Unit>>,
    modifier: Modifier = Modifier,
    gutter: Dp = SwissLayout.Gutter
) {
    validateGridSpans(columns.map { it.first })

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth < SwissLayout.NarrowBreakpoint) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gutter)) {
                columns.forEach { (_, column) -> Box(Modifier.fillMaxWidth()) { column() } }
            }
        } else {
            val columnWidth = ((maxWidth - gutter * (SwissLayout.COLUMNS - 1)) / SwissLayout.COLUMNS)
                .coerceAtLeast(0.dp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gutter)) {
                columns.forEach { (span, column) ->
                    Box(Modifier.width(columnWidth * span + gutter * (span - 1))) { column() }
                }
            }
        }
    }
}

/**
 * 整幅栏线 —— 分区之间用 1px 黑线，不用卡片框。
 */
@Composable
fun SwissRule(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}

/**
 * 分区 —— 上面留 96px 空白，用一条整幅 1px 线划分，下面留 16px 再放内容。
 *
 * 规范：「Sections sit 96px apart, often divided by a single full-width 1px black rule.」
 */
@Composable
fun SwissSection(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier) {
        Spacer(Modifier.height(SwissLayout.SectionGap))
        SwissRule(color = color)
        Spacer(Modifier.height(SwissLayout.ElementGap))
        content()
    }
}
