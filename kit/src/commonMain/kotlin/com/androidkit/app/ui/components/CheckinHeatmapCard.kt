package com.androidkit.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.androidkit.app.ui.theme.themedCorner
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.M3Motion
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number

/** 热力图展示的周数（最近 3 个月） */
private const val HEATMAP_WEEKS = 13

/** 网格左上角日期：当前周周一往前推 (HEATMAP_WEEKS - 1) 周 */
private fun gridStart(today: LocalDate): LocalDate =
    today.minusDays(today.dayOfWeek.isoDayNumber - 1).minusWeeks(HEATMAP_WEEKS - 1)

/** 生成 weeks × 7 的网格，左上角对齐 [gridStart]，按时间从早到晚 */
private fun buildGrid(today: LocalDate, valueOf: (LocalDate) -> Int): List<List<Int>> =
    buildList {
        var weekStart = gridStart(today)
        repeat(HEATMAP_WEEKS) {
            add((0..6).map { day -> valueOf(weekStart.plusDays(day)) })
            weekStart = weekStart.plusWeeks(1)
        }
    }

/**
 * Check-in Heatmap Card - 打卡热力图卡片
 * 展示最近 13 周（3个月）的打卡记录
 */
@Composable
fun CheckinHeatmapCard(
    checkInRecords: List<LocalDate>,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    aggregateMode: Boolean = false
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val weeks = HEATMAP_WEEKS

    val heatmapData = remember(checkInRecords, aggregateMode) {
        val counts = checkInRecords
            .filter { it <= today }
            .groupingBy { it }
            .eachCount()

        buildGrid(today) { date ->
            val count = counts[date] ?: 0
            // 聚合模式按当天打卡次数分级，非聚合模式只关心有没有打卡
            if (aggregateMode) count else minOf(count, 1)
        }
    }

    val stats = remember(checkInRecords) {
        val totalDays = checkInRecords.size
        val currentStreak = calculateCurrentStreak(checkInRecords)
        val longestStreak = calculateLongestStreak(checkInRecords)
        Triple(totalDays, currentStreak, longestStreak)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatChip(
                label = stringResource(Res.string.current_streak),
                value = stats.second.toString()
            )
            StatChip(
                label = stringResource(Res.string.total_checkins),
                value = stats.first.toString()
            )
        }

        HeatmapGrid(
            data = heatmapData,
            accentColor = accentColor,
            weeks = weeks,
            aggregateMode = aggregateMode
        )
    }
}

@Composable
private fun HeatmapGrid(
    data: List<List<Int>>,
    accentColor: Color,
    weeks: Int,
    aggregateMode: Boolean,
    modifier: Modifier = Modifier
) {
    val cellSpacing = 4.dp
    // 空格子用 surface（浅色下比卡片底色浅、深色下更深），填充格用主题 primary
    val emptyColor = MaterialTheme.colorScheme.surface
    val levelColors = remember(accentColor, emptyColor, aggregateMode) {
        if (aggregateMode) {
            // 聚合模式：颜色深浅 = 当天打卡次数（1~4+ 次）
            listOf(
                emptyColor,
                accentColor.copy(alpha = 0.25f),
                accentColor.copy(alpha = 0.5f),
                accentColor.copy(alpha = 0.75f),
                accentColor
            )
        } else {
            // 单习惯只有「没打卡 / 打卡」两态，用满色，25% 淡色几乎看不出来
            listOf(emptyColor, accentColor, accentColor, accentColor, accentColor)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val totalSpacing = cellSpacing * (weeks - 1)
        val cellSize = (maxWidth - totalSpacing) / weeks

        Column(
            verticalArrangement = Arrangement.spacedBy(cellSpacing)
        ) {
            // 月份标签行：按周列定位，避免固定宽度标签溢出网格
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clipToBounds()
            ) {
                var prevMonth = -1
                data.forEachIndexed { weekIndex, _ ->
                    val firstDate = gridStart(Clock.System.todayIn(TimeZone.currentSystemDefault())).plusDays(weekIndex * 7)
                    val monthValue = firstDate.month.number
                    if (monthValue != prevMonth) {
                        Text(
                            text = KitDateFormat.monthShort(monthValue, currentLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.offset(x = (cellSize + cellSpacing) * weekIndex)
                        )
                        prevMonth = monthValue
                    }
                }
            }

            // 热力图网格
            Row(
                horizontalArrangement = Arrangement.spacedBy(cellSpacing)
            ) {
                data.forEach { week ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(cellSpacing)
                    ) {
                        week.forEach { count ->
                            val level = when {
                                count == 0 -> 0
                                count == 1 -> 1
                                count == 2 -> 2
                                count == 3 -> 3
                                else -> 4
                            }
                            // 打卡/取消打卡时格子颜色平滑过渡，不硬切
                            val cellColor by animateColorAsState(
                                targetValue = levelColors[level],
                                animationSpec = M3Motion.fastEffects(),
                                label = "heatmap_cell"
                            )
                            Box(
                                modifier = Modifier
                                    .size(cellSize)
                                    .clip(themedCorner(2.dp))
                                    .background(cellColor)
                            )
                        }
                    }
                }
            }
            // 图例：颜色越深 = 当天打卡次数越多（仅聚合模式有分级）
            if (aggregateMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.less),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    levelColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(themedCorner(2.dp))
                                .background(color)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(Res.string.more),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 连续打卡天数：今天或昨天有记录才起算，中断即归零 */
internal fun calculateCurrentStreak(records: List<LocalDate>): Int {
    if (records.isEmpty()) return 0

    val sortedDates = records.distinct().sortedDescending()

    if (sortedDates.isEmpty()) return 0

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val yesterday = today.minusDays(1)

    val startDate = if (sortedDates.first() == today || sortedDates.first() == yesterday) {
        sortedDates.first()
    } else {
        return 0
    }

    var streak = 0
    var currentDate = startDate

    for (date in sortedDates) {
        if (date == currentDate) {
            streak++
            currentDate = currentDate.minusDays(1)
        } else if (date < currentDate) {
            break
        }
    }

    return streak
}

private fun calculateLongestStreak(records: List<LocalDate>): Int {
    if (records.isEmpty()) return 0

    val sortedDates = records.distinct().sortedDescending()

    if (sortedDates.isEmpty()) return 0

    var longestStreak = 1
    var currentStreak = 1
    var prevDate = sortedDates.first()

    for (i in 1 until sortedDates.size) {
        val date = sortedDates[i]
        if (prevDate.minusDays(1) == date) {
            currentStreak++
            longestStreak = maxOf(longestStreak, currentStreak)
        } else if (prevDate != date) {
            currentStreak = 1
        }
        prevDate = date
    }

    return longestStreak
}

@Preview(showBackground = true)
@Composable
private fun CheckinHeatmapCardPreview() {
    AppTheme {
        CheckinHeatmapCard(
            checkInRecords = listOf(
                DemoData.Value.DATE_TODAY,
                DemoData.Value.DATE_TODAY_MINUS_1,
                DemoData.Value.DATE_TODAY_MINUS_2,
                DemoData.Value.DATE_TODAY_MINUS_2,
                DemoData.Value.DATE_TODAY_MINUS_4
            ),
            aggregateMode = true
        ) // ponytail: demo data only
    }
}

@Preview(name = "Heatmap Grid", showBackground = true, widthDp = 360)
@Composable
fun HeatmapGridPreview() {
    AppTheme {
        HeatmapGrid(
            data = List(DemoData.Value.INT_TWELVE) { week ->
                List(7) { day -> (week + day) % 4 }
            },
            accentColor = DemoData.Palette.primary,
            weeks = DemoData.Value.INT_TWELVE,
            aggregateMode = false
        ) // ponytail: demo data only
    }
}

@Preview(name = "Stat Chip", showBackground = true)
@Composable
fun StatChipPreview() {
    AppTheme {
        StatChip(label = DemoData.Text.STREAK, value = DemoData.Text.TWELVE_DAYS)
    }
}
