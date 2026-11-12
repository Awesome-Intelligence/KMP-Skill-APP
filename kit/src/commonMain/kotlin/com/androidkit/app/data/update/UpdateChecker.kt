package com.androidkit.app.data.update

import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock

/**
 * “自动发现新版本”的唯一入口。
 *
 * 它只做三件事：**节流 → 拉清单 → 比版本**，不发通知、不弹窗、不下载 ——
 * 那些是 UI 和 App 的决定（见 `UpdateDialog` 和 `apps/demo` 的 `UpdateGate`）。
 *
 * ```kotlin
 * val checker = UpdateChecker(
 *     source = HttpUpdateSource(httpClient, "https://example.com/latest.json"),
 *     stateStore = FileUpdateStateStore(filesDir.toPath()),
 * )
 * // 冷启动/回到前台时调一次；节流会挡住高频调用
 * when (val result = checker.check(BuildConfig.VERSION_NAME)) {
 *     is UpdateResult.Available -> showUpdateDialog(result)
 *     else -> Unit   // 失败静默：更新检查不该打扰正在用 app 的人
 * }
 * ```
 *
 * `currentVersion` 是字符串参数而不是从 BuildConfig 读：kit 里没有 BuildConfig，
 * 也不做平台接口 —— 三端各自把版本号传进来（Android `versionName`、iOS
 * `CFBundleShortVersionString`、Desktop 打包版本）。
 *
 * [nowMillis] 可注入，测试里用来把时间钉死。
 */
class UpdateChecker(
    private val source: UpdateSource,
    private val stateStore: UpdateStateStore,
    private val policy: UpdateCheckPolicy = UpdateCheckPolicy(),
    private val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {

    /**
     * 检查一次。
     *
     * @param force 手动检查：绕过节流和“跳过此版本”。UI 的“检查更新”按钮传 true。
     */
    suspend fun check(currentVersion: String, force: Boolean = false): UpdateResult = try {
        checkInternal(currentVersion, force)
    } catch (e: CancellationException) {
        // 协程取消不是“检查失败”，原样抛出去，否则 UI 退出时会被当成错误收尾
        throw e
    } catch (e: Exception) {
        // 落盘失败之类的意外也不该把 app 弄崩
        UpdateResult.Failed("检查更新失败：${e.message ?: e::class.simpleName}")
    }

    private suspend fun checkInternal(currentVersion: String, force: Boolean): UpdateResult {
        val now = nowMillis()
        val state = stateStore.load()
        if (!force && isThrottled(state, now)) return UpdateResult.Throttled

        val current = AppVersion.parseOrNull(currentVersion)
            ?: return UpdateResult.Failed("当前版本号无法解析：$currentVersion")

        val manifest = try {
            source.fetch()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 失败也记时间：否则离线用户每次冷启动都要白等一次超时
            stateStore.save(state.copy(lastCheckedAtMillis = now))
            return UpdateResult.Failed("检查更新失败：${e.message ?: e::class.simpleName}")
        }
        stateStore.save(state.copy(lastCheckedAtMillis = now))

        val latest = AppVersion.parseOrNull(manifest.latestVersion)
            ?: return UpdateResult.Failed("远端版本号无法解析：${manifest.latestVersion}")
        if (latest <= current) return UpdateResult.UpToDate

        // 清单写错了就当作“不强制”，但仍然提示新版本 —— 不能因为一个字段让更新提示整个消失
        val minimum = manifest.minSupportedVersion?.let(AppVersion::parseOrNull)
        val mandatory = minimum != null && current < minimum

        // 手动检查时跳过记录不生效：用户点“检查更新”就是想看有没有新版
        if (!force && !mandatory && state.skippedVersion == manifest.latestVersion) {
            return UpdateResult.Skipped
        }
        return UpdateResult.Available(manifest, mandatory)
    }

    /** 用户点了“跳过此版本”：只影响这个版本号，出现更新的版本会自动重新提示。 */
    suspend fun skip(version: String) {
        stateStore.save(stateStore.load().copy(skippedVersion = version))
    }

    private fun isThrottled(state: UpdateState, now: Long): Boolean {
        if (state.lastCheckedAtMillis <= 0L) return false
        // 用户把系统时间往回调过：别让“上次检查”永远在未来，直接放行
        if (now <= state.lastCheckedAtMillis) return false
        return now - state.lastCheckedAtMillis < policy.minInterval.inWholeMilliseconds
    }
}
