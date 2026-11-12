package com.androidkit.app.ui.components

import com.androidkit.app.ui.theme.LocalReducedMotion
import com.androidkit.app.ui.theme.M3Motion
import com.androidkit.app.ui.theme.LocalThemeStyle
import com.androidkit.app.ui.theme.ThemeStyle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.res.*
import com.androidkit.app.ui.theme.AppTheme
import kotlin.time.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.todayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.onDay
import kotlinx.datetime.yearMonth
import com.androidkit.app.ui.theme.themedCircleShape


/**
 * Calendar View Mode
 */
enum class CalendarViewMode {
    DAY, WEEK, MONTH, MONTH_WEEK_HIGHLIGHT
}

/**
 * Calendar Day - 单个日期数据模型
 */
data class CalendarDay(
    val date: LocalDate?,
    val dayNumber: String,
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val isWeekSelected: Boolean = false,
    val isCurrentMonth: Boolean = true
)

/**
 * App Calendar - Material 3 日历组件
 *
 * 符合 Material Design 3 规范：
 * - 圆角 12dp，Surface 背景
 * - 周视图 / 月视图切换，带滑动动画
 * - 导航箭头切换月份（48dp 触控区域）
 * - 今日高亮（使用 secondaryContainer 区分）
 * - 选中态使用 primary 背景色 + ripple 效果
 * - 周从周日开始
 * - 无障碍支持（contentDescription）
 *
 * @param selectedDate 当前选中的日期
 * @param onDateSelected 日期选中回调
 * @param viewMode 日历视图模式（周视图/月视图）
 * @param onViewModeChange 视图模式切换回调
 */
@Composable
fun AppCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier,
    viewMode: CalendarViewMode = CalendarViewMode.MONTH,
    onViewModeChange: ((CalendarViewMode) -> Unit)? = null,
    onWeekSelected: ((LocalDate) -> Unit)? = null
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    // 展示用的月份/周与选中日期解耦：箭头只翻页，选中某天才回调 onDateSelected
    var displayedDate by remember { mutableStateOf(selectedDate) }
    LaunchedEffect(selectedDate) { displayedDate = selectedDate }
    val currentYearMonth = remember(displayedDate) { displayedDate.yearMonth }
    // transitionSpec 是非 @Composable lambda，只能在 Composable 作用域里取值后闭包捕获
    val reducedMotion = LocalReducedMotion.current
    // macOS 毛玻璃规范：动效只改颜色，不位移
    val macos = LocalThemeStyle.current == ThemeStyle.MacOS

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Header: Month/Year + Navigation
        CalendarHeader(
            yearMonth = currentYearMonth,
            viewMode = viewMode,
            onViewModeChange = onViewModeChange,
            onPrevious = {
                displayedDate = when (viewMode) {
                    CalendarViewMode.WEEK -> displayedDate.minusWeeks(1)
                    else -> displayedDate.minusMonths(1)
                }
            },
            onNext = {
                displayedDate = when (viewMode) {
                    CalendarViewMode.WEEK -> displayedDate.plusWeeks(1)
                    else -> displayedDate.plusMonths(1)
                }
            },
            onTitleClick = {
                onViewModeChange?.let { callback ->
                    callback(
                        when (viewMode) {
                            CalendarViewMode.DAY -> CalendarViewMode.WEEK
                            CalendarViewMode.WEEK -> CalendarViewMode.MONTH_WEEK_HIGHLIGHT
                            CalendarViewMode.MONTH -> CalendarViewMode.DAY
                            CalendarViewMode.MONTH_WEEK_HIGHLIGHT -> CalendarViewMode.DAY
                        }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Weekday labels
        WeekdayLabels()

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid
        AnimatedContent(
            targetState = viewMode to currentYearMonth,
            transitionSpec = {
                if (reducedMotion || macos) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else if (initialState.first != targetState.first) {
                    // 日/周/月视图之间没有空间关系，用 fade through 而非滑动
                    fadeIn(tween(M3Motion.SHORT3, easing = M3Motion.emphasizedDecelerate)) togetherWith
                        fadeOut(tween(M3Motion.SHORT2, easing = M3Motion.emphasizedAccelerate))
                } else {
                    // 翻页：沿空间关系方向滑 —— 下个月从右进，上个月从左进
                    val forward = targetState.second > initialState.second
                    val enterOffset: (Int) -> Int = if (forward) { w -> w } else { w -> -w }
                    val exitOffset: (Int) -> Int = if (forward) { w -> -w } else { w -> w }
                    (slideInHorizontally(
                        animationSpec = tween(M3Motion.MEDIUM1, easing = M3Motion.emphasizedDecelerate),
                        initialOffsetX = enterOffset
                    ) + fadeIn(tween(M3Motion.SHORT3, delayMillis = M3Motion.SHORT1, easing = M3Motion.emphasizedDecelerate)))
                        .togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(M3Motion.MEDIUM1, easing = M3Motion.emphasizedAccelerate),
                                targetOffsetX = exitOffset
                            ) + fadeOut(tween(M3Motion.SHORT2, easing = M3Motion.emphasizedAccelerate))
                        )
                }
            },
            label = "calendar_content"
        ) { (mode, yearMonth) ->
            when (mode) {
                CalendarViewMode.DAY -> {
                    DayView(
                        yearMonth = yearMonth,
                        selectedDate = selectedDate,
                        today = today,
                        onDateSelected = onDateSelected
                    )
                }
                CalendarViewMode.WEEK -> {
                    WeekView(
                        weekAnchor = displayedDate,
                        selectedDate = selectedDate,
                        today = today,
                        onDateSelected = onDateSelected
                    )
                }
                CalendarViewMode.MONTH, CalendarViewMode.MONTH_WEEK_HIGHLIGHT -> {
                    MonthView(
                        yearMonth = yearMonth,
                        selectedDate = selectedDate,
                        today = today,
                        onDateSelected = onDateSelected,
                        onWeekSelected = if (mode == CalendarViewMode.MONTH_WEEK_HIGHLIGHT) onWeekSelected else null,
                        enableWeekHighlight = mode == CalendarViewMode.MONTH_WEEK_HIGHLIGHT
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    yearMonth: YearMonth,
    viewMode: CalendarViewMode,
    onViewModeChange: ((CalendarViewMode) -> Unit)?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTitleClick: () -> Unit
) {
    // M3 规范的日期格式：跟随系统语言
    val language = currentLanguage
    val yearMonthPattern = stringResource(Res.string.date_pattern_year_month)
    val title = remember(yearMonth, language, yearMonthPattern) {
        KitDateFormat.format(yearMonth, yearMonthPattern, language)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // M3 IconButton: 48dp 触控区域
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = stringResource(Res.string.previous_month)
            )
        }

        // 标题可点击切换视图模式
        val viewSwitchHint = if (viewMode == CalendarViewMode.MONTH) {
            stringResource(Res.string.month_view_switch_hint)
        } else {
            stringResource(Res.string.week_view_switch_hint)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    enabled = onViewModeChange != null,
                    onClick = onTitleClick
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .semantics { contentDescription = viewSwitchHint }
        )

        IconButton(
            onClick = onNext,
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = stringResource(Res.string.next_month)
            )
        }
    }
}

@Composable
private fun WeekdayLabels() {
    val weekdays = remember {
        listOf(
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
        )
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        weekdays.forEach { day ->
            Text(
                text = KitDateFormat.weekdayNarrow(day, currentLanguage),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MonthView(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onWeekSelected: ((LocalDate) -> Unit)? = null,
    enableWeekHighlight: Boolean = false
) {
    val days = remember(yearMonth) {
        generateMonthDays(yearMonth)
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        days.chunked(7).forEach { week ->
            WeekRow(
                week = week,
                selectedDate = selectedDate,
                today = today,
                onDateSelected = onDateSelected,
                onWeekSelected = onWeekSelected,
                enableWeekHighlight = enableWeekHighlight
            )
        }
    }
}

@Composable
private fun WeekRow(
    week: List<CalendarDay>,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onWeekSelected: ((LocalDate) -> Unit)? = null,
    enableWeekHighlight: Boolean = false
) {
    val weekDates = week.mapNotNull { it.date }
    val firstDayOfWeek = weekDates.firstOrNull()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onWeekSelected != null && firstDayOfWeek != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onWeekSelected(firstDayOfWeek)
                    }
                } else Modifier
            )
    ) {
        week.forEach { day ->
            val isInSelectedWeek = enableWeekHighlight && weekDates.contains(selectedDate)
            CalendarDayCell(
                day = day,
                isSelected = day.date == selectedDate,
                isWeekSelected = isInSelectedWeek,
                isToday = day.date == today,
                onClick = { day.date?.let { onDateSelected(it) } },
                modifier = Modifier.weight(1f)
            )
        }
        repeat(7 - week.size) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Day View: full month grid, only selected day is highlighted (no week highlight)
 */
@Composable
private fun DayView(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val days = remember(yearMonth) {
        generateMonthDays(yearMonth)
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        days.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                week.forEach { day ->
                    CalendarDayCell(
                        day = day,
                        isSelected = day.date == selectedDate,
                        isWeekSelected = false,
                        isToday = day.date == today,
                        onClick = { day.date?.let { onDateSelected(it) } },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WeekView(
    weekAnchor: LocalDate,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val weekDays = remember(weekAnchor) {
        generateWeekDays(weekAnchor)
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        weekDays.forEach { day ->
            CalendarDayCell(
                day = day,
                isSelected = day.date == selectedDate,
                isWeekSelected = true,
                isToday = day.date == today,
                onClick = { day.date?.let { onDateSelected(it) } },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    isWeekSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val date = day.date

    // Compute accessibility strings in @Composable context
    val monthDayStr = date?.let { stringResource(Res.string.date_month_day, it.month.number, it.dayOfMonth) } ?: ""
    val todaySuffix = if (isToday && date != null) stringResource(Res.string.today_suffix) else ""
    val selectedSuffix = if (isSelected && date != null) stringResource(Res.string.selected_suffix) else ""
    val weekSelectedSuffix = if (isWeekSelected && !isSelected && date != null) stringResource(Res.string.week_selected_suffix) else ""

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(themedCircleShape())
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isWeekSelected -> MaterialTheme.colorScheme.primaryContainer
                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            // M3: 使用 ripple 效果替代普通 clickable
            .clickable(
                interactionSource = interactionSource,
                enabled = day.date != null,
                onClick = onClick
            )
            .semantics {
                if (date != null) {
                    contentDescription = monthDayStr + todaySuffix + selectedSuffix + weekSelectedSuffix
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.dayNumber,
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isWeekSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }
        )
    }
}

/**
 * Generate days for a month with padding from previous/next months
 */
private fun generateMonthDays(yearMonth: YearMonth): List<CalendarDay> {
    val result = mutableListOf<CalendarDay>()

    val firstDayOfMonth = yearMonth.onDay(1)

    // 每周从周日开始：kotlinx 的 DayOfWeek.value 是 ISO（周一=1 … 周日=7），
    // 换算成「周日=1 … 周六=7」
    val firstWeekday = firstDayOfMonth.dayOfWeek.isoDayNumber % 7 + 1

    // Add days from previous month to fill the first week
    if (firstWeekday > 1) {
        val previousMonth = yearMonth.minusMonths(1)
        val daysInPreviousMonth = previousMonth.numberOfDays
        for (i in (daysInPreviousMonth - firstWeekday + 2)..daysInPreviousMonth) {
            result.add(
                CalendarDay(
                    date = previousMonth.onDay(i),
                    dayNumber = i.toString(),
                    isCurrentMonth = false
                )
            )
        }
    }

    // Add days of current month
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    for (day in 1..yearMonth.numberOfDays) {
        val date = yearMonth.onDay(day)
        result.add(
            CalendarDay(
                date = date,
                dayNumber = day.toString(),
                isToday = date == today
            )
        )
    }

    // Add days from next month to complete the last week
    val nextMonth = yearMonth.plusMonths(1)
    val remainingDays = 7 - (result.size % 7)
    if (remainingDays < 7) {
        for (day in 1..remainingDays) {
            result.add(
                CalendarDay(
                    date = nextMonth.onDay(day),
                    dayNumber = day.toString(),
                    isCurrentMonth = false
                )
            )
        }
    }

    return result
}

/**
 * Generate days for a week containing the selected date
 */
private fun generateWeekDays(selectedDate: LocalDate): List<CalendarDay> {
    val result = mutableListOf<CalendarDay>()

    // 本周从周日开始
    val daysSinceSunday = selectedDate.dayOfWeek.isoDayNumber % 7
    val sunday = selectedDate.minusDays(daysSinceSunday)

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    for (i in 0..6) {
        val date = sunday.plusDays(i)
        result.add(
            CalendarDay(
                date = date,
                dayNumber = date.dayOfMonth.toString(),
                isToday = date == today
            )
        )
    }

    return result
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AppCalendarPreview() {
    AppTheme {
        AppCalendar(
            selectedDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            onDateSelected = {}
        ) // ponytail: demo data only
    }
}

@Preview(name = "Month View", showBackground = true, widthDp = 360)
@Composable
fun MonthViewPreview() {
    AppTheme {
        MonthView(
            yearMonth = Clock.System.todayIn(TimeZone.currentSystemDefault()).yearMonth,
            selectedDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            today = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            onDateSelected = {}
        ) // ponytail: demo data only
    }
}

@Preview(name = "Calendar Header", showBackground = true, widthDp = 360)
@Composable
fun CalendarHeaderPreview() {
    AppTheme {
        CalendarHeader(
            yearMonth = Clock.System.todayIn(TimeZone.currentSystemDefault()).yearMonth,
            viewMode = CalendarViewMode.MONTH,
            onViewModeChange = { _ -> },
            onPrevious = {},
            onNext = {},
            onTitleClick = {}
        ) // ponytail: demo data only
    }
}

@Preview(name = "Weekday Labels", showBackground = true, widthDp = 360)
@Composable
fun WeekdayLabelsPreview() {
    AppTheme {
        WeekdayLabels() // ponytail: demo data only
    }
}

@Preview(name = "Week Row", showBackground = true, widthDp = 360)
@Composable
fun WeekRowPreview() {
    AppTheme {
        val monday = Clock.System.todayIn(TimeZone.currentSystemDefault()).let { it.minusDays(it.dayOfWeek.isoDayNumber - 1) }
        val week = (0..6).map { offset ->
            val date = monday.plusDays(offset)
            CalendarDay(
                date = date,
                dayNumber = date.dayOfMonth.toString(),
                isCurrentMonth = true
            )
        }
        WeekRow(
            week = week,
            selectedDate = monday.plusDays(2),
            today = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            onDateSelected = {},
            onWeekSelected = {},
            enableWeekHighlight = true
        ) // ponytail: demo data only
    }
}

@Preview(name = "Day View", showBackground = true, widthDp = 360)
@Composable
fun DayViewPreview() {
    AppTheme {
        DayView(
            yearMonth = Clock.System.todayIn(TimeZone.currentSystemDefault()).yearMonth,
            selectedDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            today = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            onDateSelected = {}
        ) // ponytail: demo data only
    }
}

@Preview(name = "Week View", showBackground = true, widthDp = 360)
@Composable
fun WeekViewPreview() {
    AppTheme {
        WeekView(
            weekAnchor = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            selectedDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            today = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            onDateSelected = {}
        ) // ponytail: demo data only
    }
}

@Preview(name = "Calendar Day Cell", showBackground = true, widthDp = 360)
@Composable
fun CalendarDayCellPreview() {
    AppTheme {
        val date = Clock.System.todayIn(TimeZone.currentSystemDefault())
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalendarDayCell(
                day = CalendarDay(date, date.dayOfMonth.toString(), isToday = true),
                isSelected = false,
                isWeekSelected = false,
                isToday = true,
                onClick = {},
                modifier = Modifier.size(48.dp)
            )
            CalendarDayCell(
                day = CalendarDay(date.plusDays(1), date.plusDays(1).dayOfMonth.toString()),
                isSelected = true,
                isWeekSelected = false,
                isToday = false,
                onClick = {},
                modifier = Modifier.size(48.dp)
            )
            CalendarDayCell(
                day = CalendarDay(date.plusDays(2), date.plusDays(2).dayOfMonth.toString()),
                isSelected = false,
                isWeekSelected = true,
                isToday = false,
                onClick = {},
                modifier = Modifier.size(48.dp)
            )
        } // ponytail: demo data only
    }
}
