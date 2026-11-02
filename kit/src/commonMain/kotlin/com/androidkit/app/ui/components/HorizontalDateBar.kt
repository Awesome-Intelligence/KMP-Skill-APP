package com.androidkit.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.M3Motion
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.todayIn
import kotlinx.coroutines.launch
import kotlinx.datetime.yearMonth
import kotlinx.datetime.number

/**
 * 横向日期条 —— 日程 App 常见的连续日期选择器。
 *
 * 每格 = 星期几小字 + 日号圆点：
 * - 选中：primary 实心圆 + onPrimary 文字
 * - 今天（未选中）：secondaryContainer 圆底
 * - markedDates 里的日期下方带一个小圆点，表示当天有安排
 *
 * 组件不绑定业务语义，具体"当天有什么"由调用方决定。
 *
 * 标题跟随可见范围：可见区间在同一个月显示「2026年10月」，跨月显示「2026年10月 – 11月」；
 * 日期条离开今天时，标题右侧出现「今天」按钮。
 *
 * @param selectedDate 当前选中日期
 * @param onDateSelected 点击日期回调
 * @param markedDates 需要标记（有安排）的日期
 * @param rangeDays 今天前后各多少天，合计 2*rangeDays+1 天
 * @param onCalendarClick 非空时标题行右侧显示「日历视图」入口按钮
 */
@Composable
fun HorizontalDateBar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    markedDates: Set<LocalDate> = emptySet(),
    rangeDays: Int = 365,
    onCalendarClick: (() -> Unit)? = null
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val dates = remember(today, rangeDays) {
        val start = today.minusDays(rangeDays)
        (0 until rangeDays * 2 + 1).map { start.plusDays(it) }
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val language = currentLanguage
    val yearMonthPattern = stringResource(Res.string.date_pattern_year_month)
    val monthPattern = stringResource(Res.string.date_pattern_month)
    val rangeTemplate = stringResource(Res.string.date_range_year_month)

    // 打开时定位到今天（index == rangeDays）
    LaunchedEffect(listState, today) {
        listState.scrollToItem(rangeDays)
    }

    // 当前可见范围 —— 月份标题和「今天」按钮都跟着它走
    var visibleRange by remember { mutableStateOf(IntRange(rangeDays, rangeDays)) }
    LaunchedEffect(listState, dates) {
        snapshotFlow {
            val visible = listState.layoutInfo.visibleItemsInfo
            val first = visible.firstOrNull()?.index ?: -1
            val last = visible.lastOrNull()?.index ?: -1
            if (first >= 0 && last >= first) first..last else null
        }.collect { range -> if (range != null) visibleRange = range }
    }

    val title = remember(visibleRange, dates, language) {
        val first = dates.getOrNull(visibleRange.first)
        val last = dates.getOrNull(visibleRange.last)
        if (first == null || last == null) {
            ""
        } else {
            val ymFirst = first.yearMonth
            val ymLast = last.yearMonth
            if (ymFirst == ymLast) {
                KitDateFormat.format(ymFirst, yearMonthPattern, language)
            } else {
                val secondPattern =
                    if (ymFirst.year == ymLast.year) monthPattern else yearMonthPattern
                rangeTemplate.format(
                    KitDateFormat.format(ymFirst, yearMonthPattern, language),
                    KitDateFormat.format(ymLast, secondPattern, language)
                )
            }
        }
    }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            // 离开今天时才给回程入口
            if (rangeDays !in visibleRange) {
                TextActionButton(
                    text = stringResource(Res.string.today),
                    onClick = {
                        scope.launch { listState.animateScrollToItem(rangeDays) }
                        onDateSelected(today)
                    }
                )
            }
            // 月历入口（调用方可选）
            if (onCalendarClick != null) {
                IconButton(onClick = onCalendarClick) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = stringResource(Res.string.calendar_view),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(dates, key = { it.toString() }) { date ->
                DateCell(
                    date = date,
                    isToday = date == today,
                    isSelected = date == selectedDate,
                    isMarked = date in markedDates,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

@Composable
private fun DateCell(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    isMarked: Boolean,
    onClick: () -> Unit
) {
    val weekday = KitDateFormat.weekdayNarrow(date.dayOfWeek, currentLanguage)
    val monthDayStr = stringResource(Res.string.date_month_day, date.month.number, date.dayOfMonth)
    val todaySuffix = if (isToday) stringResource(Res.string.today_suffix) else ""
    val selectedSuffix = if (isSelected) stringResource(Res.string.selected_suffix) else ""

    Column(
        modifier = Modifier
            .width(48.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .semantics { contentDescription = monthDayStr + todaySuffix + selectedSuffix },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = weekday,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        // 选中/今天底色平滑过渡（M3 effects：颜色/透明度不过冲）
        val circleColor by animateColorAsState(
            targetValue = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isToday -> MaterialTheme.colorScheme.secondaryContainer
                else -> androidx.compose.ui.graphics.Color.Transparent
            },
            animationSpec = M3Motion.fastEffects(),
            label = "date_cell_bg"
        )
        val dayTextColor by animateColorAsState(
            targetValue = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            },
            animationSpec = M3Motion.fastEffects(),
            label = "date_cell_text"
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(circleColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal,
                color = dayTextColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // 有安排的日期打点
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(
                    if (isMarked) {
                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    } else {
                        androidx.compose.ui.graphics.Color.Transparent
                    }
                )
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun HorizontalDateBarPreview() {
    AppTheme {
        HorizontalDateBar(
            selectedDate = DemoData.Value.DATE_TODAY,
            onDateSelected = {},
            markedDates = setOf(
                DemoData.Value.DATE_TODAY_PLUS_2,
                DemoData.Value.DATE_TODAY_PLUS_5
            ), // ponytail: demo data only
            rangeDays = DemoData.Value.INT_FIFTEEN
        )
    }
}

@Preview(name = "Date Cell", showBackground = true)
@Composable
fun DateCellPreview() {
    AppTheme {
        DateCell(
            date = DemoData.Value.DATE_TODAY,
            isToday = true,
            isSelected = false,
            isMarked = true,
            onClick = {}
        ) // ponytail: demo data only
    }
}
