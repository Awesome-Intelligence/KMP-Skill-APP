package com.androidkit.app.data.update

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * 自动检查的策略。
 *
 * [minInterval]：两次**自动**检查之间的最小间隔。默认 6 小时 ——
 * 冷启动频繁的 app 不该每次启动都打一次网络；手动检查（`check(force = true)`）不受它限制。
 */
data class UpdateCheckPolicy(
    val minInterval: Duration = 6.hours,
)
