package com.androidkit.app.ui.components

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.onDay
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

/**
 * java.time 的加减日期 API 在 KMP 里没有 —— 这里补一份，调用点不用改。
 *
 * ponytail: 只补 kit 用到的这几个；需要更多再加。
 */

/** 加减天。 */
fun LocalDate.plusDays(n: Int): LocalDate = plus(n, DateTimeUnit.DAY)
fun LocalDate.minusDays(n: Int): LocalDate = minus(n, DateTimeUnit.DAY)

/** 加减周。 */
fun LocalDate.plusWeeks(n: Int): LocalDate = plus(n * 7, DateTimeUnit.DAY)
fun LocalDate.minusWeeks(n: Int): LocalDate = minus(n * 7, DateTimeUnit.DAY)

/** 加减月：月底自动收敛（1月31日 + 1月 = 2月28日），跟 java.time 一样。 */
fun LocalDate.plusMonths(n: Int): LocalDate {
    val ym = yearMonth.plus(n, DateTimeUnit.MONTH)
    return ym.onDay(minOf(day, ym.numberOfDays))
}

fun LocalDate.minusMonths(n: Int): LocalDate = plusMonths(-n)

/** YearMonth 加减月。 */
fun YearMonth.plusMonths(n: Int): YearMonth = plus(n, DateTimeUnit.MONTH)
fun YearMonth.minusMonths(n: Int): YearMonth = minus(n, DateTimeUnit.MONTH)
