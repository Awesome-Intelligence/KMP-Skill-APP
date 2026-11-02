package com.androidkit.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.androidkit.app.res.*
import kotlin.time.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number

/**
 * 界面语言 —— 跟随 per-app 语言切换。
 *
 * ponytail: 原来读的是 Android 的 `LocalConfiguration.current.locales[0]`（拿不到 iOS/桌面语言）。
 * 公共层只认语言（zh/en 两套文案）；需要 per-app 切换时，App 层用
 * `CompositionLocalProvider(LocalKitLanguage provides KitLanguage.EN)` 覆盖即可。
 */
enum class KitLanguage {
    ZH,
    EN;

    companion object {
        fun of(locale: Locale): KitLanguage =
            if (locale.language.startsWith("zh")) ZH else EN
    }
}

/** 当前界面语言（App 层可覆盖）。 */
val LocalKitLanguage = staticCompositionLocalOf {
    KitLanguage.of(LocaleList.current.firstOrNull() ?: Locale("en"))
}

/** 当前界面语言（组合里读）。 */
val currentLanguage: KitLanguage
    @Composable get() = LocalKitLanguage.current

/** 年月标题：「2026年10月」/「October 2026」 */
@Composable
fun formatYearMonth(month: YearMonth): String =
    KitDateFormat.format(month, stringResource(Res.string.date_pattern_year_month), currentLanguage)

/** 短日期：「10月14日」/「Oct 14, 2026」 */
@Composable
fun formatShortDate(date: LocalDate): String =
    KitDateFormat.format(date, stringResource(Res.string.date_pattern_short_date), currentLanguage)

/** 完整日期：「2026年10月14日」/「Oct 14, 2026」 */
@Composable
fun formatFullDate(date: LocalDate): String =
    KitDateFormat.format(date, stringResource(Res.string.date_pattern_full_date), currentLanguage)

/** 相对时间：「刚刚」/「5 分钟前」/「3 小时前」/「2 天前」，超过一周显示短日期 */
@Composable
fun formatRelativeTime(timestamp: Long): String {
    val diff = Clock.System.now().toEpochMilliseconds() - timestamp
    return when {
        diff < 60_000L -> stringResource(Res.string.time_just_now)
        diff < 3_600_000L -> stringResource(Res.string.time_minutes_ago, diff / 60_000L)
        diff < 86_400_000L -> stringResource(Res.string.time_hours_ago, diff / 3_600_000L)
        diff < 7 * 86_400_000L -> stringResource(Res.string.time_days_ago, diff / 86_400_000L)
        else -> formatShortDate(
            Instant.fromEpochMilliseconds(timestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        )
    }
}

/**
 * 迷你日期格式化器 —— 替掉 `java.time.format.DateTimeFormatter`。
 *
 * java.time 是 JVM 专属，iOS/桌面编不了。Compose Multiplatform 也没有内置的
 * locale 感知格式化器（kotlinx-datetime 只有 ISO 输出），所以这里自己实现一小撮
 * DateTimeFormatter 风格的 pattern：
 *
 * - `yyyy` / `yy` / `y` —— 年
 * - `MMMM` / `MMM` —— 月名（英文全称/缩写，中文是「10月」）
 * - `MM` / `M` —— 月（补零/不补零）
 * - `dd` / `d` —— 日
 * - `'...'` —— 引号里的字面量（`'年'`）
 *
 * 只支持 kit 实际用到的这几个 token；要更全的 locale 格式化，等有人真需要再说。
 */
object KitDateFormat {

    private val EN_MONTHS = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    private val EN_MONTHS_SHORT = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    /** 星期窄名：周日=0 … 周六=6（跟 DateTimeFormatter.NARROW 一致，中文是「日一二三四五六」）。 */
    private val EN_WEEKDAYS_NARROW = listOf("S", "M", "T", "W", "T", "F", "S")
    private val ZH_WEEKDAYS_NARROW = listOf("日", "一", "二", "三", "四", "五", "六")

    fun format(date: LocalDate, pattern: String, language: KitLanguage): String =
        formatParts(date.year, date.month.number, date.dayOfMonth, pattern, language)

    fun format(month: YearMonth, pattern: String, language: KitLanguage): String =
        formatParts(month.year, month.month.number, null, pattern, language)

    /** 星期窄名（「一」「T」）。 */
    fun weekdayNarrow(dayOfWeek: DayOfWeek, language: KitLanguage): String {
        val index = dayOfWeek.isoDayNumber % 7 // kotlinx 的 DayOfWeek 是 ISO：周一=1 … 周日=7
        return when (language) {
            KitLanguage.ZH -> ZH_WEEKDAYS_NARROW[index]
            KitLanguage.EN -> EN_WEEKDAYS_NARROW[index]
        }
    }

    /** 月份缩写名（「10月」/「Oct」）。 */
    fun monthShort(monthNumber: Int, language: KitLanguage): String =
        monthName(monthNumber, language, full = false)

    private fun formatParts(
        year: Int,
        month: Int,
        day: Int?,
        pattern: String,
        language: KitLanguage
    ): String {
        val out = StringBuilder()
        var i = 0
        while (i < pattern.length) {
            val c = pattern[i]
            if (c == '\'') {
                i++
                if (i < pattern.length && pattern[i] == '\'') {
                    out.append('\'')
                    i++
                    continue
                }
                while (i < pattern.length && pattern[i] != '\'') {
                    out.append(pattern[i])
                    i++
                }
                if (i < pattern.length) i++ // 跳过收尾的 '
                continue
            }
            if (c.isLetter()) {
                var j = i
                while (j < pattern.length && pattern[j] == c) j++
                out.append(token(c, j - i, year, month, day, language))
                i = j
                continue
            }
            out.append(c)
            i++
        }
        return out.toString()
    }

    private fun token(
        c: Char,
        count: Int,
        year: Int,
        month: Int,
        day: Int?,
        language: KitLanguage
    ): String = when (c) {
        'y' -> if (count == 2) (year % 100).toString().padStart(2, '0') else year.toString()
        'M' -> when {
            count >= 4 -> monthName(month, language, full = true)
            count == 3 -> monthName(month, language, full = false)
            count == 2 -> month.toString().padStart(2, '0')
            else -> month.toString()
        }
        'd' -> {
            val d = day
            when {
                d == null -> ""
                count == 2 -> d.toString().padStart(2, '0')
                else -> d.toString()
            }
        }
        else -> ""
    }

    private fun monthName(month: Int, language: KitLanguage, full: Boolean): String = when (language) {
        KitLanguage.ZH -> "${month}月"
        KitLanguage.EN -> if (full) EN_MONTHS[month - 1] else EN_MONTHS_SHORT[month - 1]
    }
}
