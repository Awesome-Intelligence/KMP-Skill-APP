package com.androidkit.app.data.update

import kotlinx.serialization.Serializable

/**
 * 检查更新的本地状态 —— 只存两件事，够“自动发现”用了。
 *
 * - [lastCheckedAtMillis]：上次**尝试**检查的时间（失败也记）。节流靠它，
 *   否则离线的用户每次冷启动都要等一次超时
 * - [skippedVersion]：用户点过“跳过此版本”的版本号；出现更新的版本时自动失效
 */
@Serializable
data class UpdateState(
    val lastCheckedAtMillis: Long = 0L,
    val skippedVersion: String? = null,
)
