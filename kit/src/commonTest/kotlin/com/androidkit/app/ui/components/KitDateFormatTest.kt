package com.androidkit.app.ui.components

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals

/** KitDateFormat 的自检 —— 这块是手写的 pattern 解析器，坏了 UI 上全是乱日期。 */
class KitDateFormatTest {

    private val date = LocalDate(2026, 10, 14)

    @Test
    fun chinesePatterns() {
        assertEquals(
            "2026年10月",
            KitDateFormat.format(YearMonth(2026, 10), "yyyy'年'M'月'", KitLanguage.ZH)
        )
        assertEquals("10月14日", KitDateFormat.format(date, "M'月'd'日'", KitLanguage.ZH))
        assertEquals("2026年10月14日", KitDateFormat.format(date, "yyyy'年'M'月'd'日'", KitLanguage.ZH))
    }

    @Test
    fun englishPatterns() {
        assertEquals(
            "October 2026",
            KitDateFormat.format(YearMonth(2026, 10), "MMMM yyyy", KitLanguage.EN)
        )
        assertEquals("Oct 14, 2026", KitDateFormat.format(date, "MMM d, yyyy", KitLanguage.EN))
    }

    @Test
    fun zeroPaddedTokens() {
        assertEquals("2026-01-05", KitDateFormat.format(LocalDate(2026, 1, 5), "yyyy-MM-dd", KitLanguage.EN))
        assertEquals("26", KitDateFormat.format(date, "yy", KitLanguage.EN))
    }

    @Test
    fun weekdayAndMonthNames() {
        assertEquals("三", KitDateFormat.weekdayNarrow(DayOfWeek.WEDNESDAY, KitLanguage.ZH))
        assertEquals("W", KitDateFormat.weekdayNarrow(DayOfWeek.WEDNESDAY, KitLanguage.EN))
        assertEquals("日", KitDateFormat.weekdayNarrow(DayOfWeek.SUNDAY, KitLanguage.ZH))
        assertEquals("Oct", KitDateFormat.monthShort(10, KitLanguage.EN))
        assertEquals("10月", KitDateFormat.monthShort(10, KitLanguage.ZH))
    }
}
