package com.androidkit.app.data.update

/**
 * 版本信息的来源 —— 换实现就换“从哪知道有新版本”。
 *
 * 底座自带两个实现：
 * - [HttpUpdateSource]：读一个静态 JSON 清单（推荐，能控制强制更新）
 * - [GitHubReleaseSource]：直接读 GitHub Releases（零托管，适合开源项目）
 *
 * 失败请抛异常：由 [UpdateChecker] 统一兜成 [UpdateResult.Failed]，
 * UI 不需要处理异常。
 */
interface UpdateSource {
    suspend fun fetch(): UpdateManifest
}
