package com.androidkit.app.data.update

/**
 * 一次版本检查的结果 —— UI 只认这几种，不需要接异常。
 */
sealed interface UpdateResult {

    /** 当前已是最新（或比远端还新）。 */
    data object UpToDate : UpdateResult

    /** 距离上次检查太近，这次没联网。不是错误，别提示用户。 */
    data object Throttled : UpdateResult

    /** 用户主动跳过了这个版本，且没有更强的更新理由。 */
    data object Skipped : UpdateResult

    /**
     * 有新版本。
     *
     * [mandatory] 为 true 表示当前版本低于清单里的 `minSupportedVersion`，
     * UI 必须挡住入口（弹窗不可关闭）；否则可以让用户“稍后”或“跳过此版本”。
     */
    data class Available(
        val manifest: UpdateManifest,
        val mandatory: Boolean,
    ) : UpdateResult

    /** 检查失败（网络、清单格式…）。[message] 只用于日志/排查，不建议直接弹给用户。 */
    data class Failed(val message: String) : UpdateResult
}
