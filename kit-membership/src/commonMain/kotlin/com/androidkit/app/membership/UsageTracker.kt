package com.androidkit.app.membership

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import okio.Path

/** 一项计数：累计次数 + 计数所属的那一天（按天重置的额度用）。 */
@Serializable
internal data class UsageRecord(
    val count: Int = 0,
    val day: Long = 0,
)

/**
 * 额度计数器：记「用了多少次」，让额度引擎能判断超没超。
 *
 * - [QuotaPeriod.DAILY]：本地时区的自然日一变，计数归零
 * - [QuotaPeriod.TOTAL]：一直累加
 *
 * 落盘在一个 JSON 文件里（app 内部存储，三端通用）。
 */
class UsageTracker(rootDir: Path) {

    private val file = JsonFile(
        rootDir / FILE_NAME,
        MapSerializer(String.serializer(), UsageRecord.serializer()),
        emptyMap(),
    )

    /** 已用次数（按天重置的额度只算当天）。 */
    suspend fun used(key: String, period: QuotaPeriod, now: Long): Int {
        val record = file.read()[key] ?: return 0
        return if (period == QuotaPeriod.DAILY && record.day != today(now)) 0 else record.count
    }

    /** 记一次消耗，返回累计次数。 */
    suspend fun consume(key: String, period: QuotaPeriod, amount: Int = 1, now: Long): Int {
        val records = file.read().toMutableMap()
        val old = records[key]
        val count = when {
            old == null -> amount
            period == QuotaPeriod.DAILY && old.day != today(now) -> amount
            else -> old.count + amount
        }
        records[key] = UsageRecord(count = count, day = today(now))
        file.write(records)
        return count
    }

    /** 清掉一项计数（升级会员后重置，或业务主动重置）。 */
    suspend fun reset(key: String) {
        val records = file.read().toMutableMap()
        records.remove(key)
        file.write(records)
    }

    /** 本地时区的今天（epochDay）。 */
    private fun today(now: Long): Long =
        Instant.fromEpochMilliseconds(now)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toEpochDays()

    private companion object {
        const val FILE_NAME = "usage.json"
    }
}
