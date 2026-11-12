package com.androidkit.app.data.update

import kotlinx.serialization.Serializable

/**
 * 远端版本清单 —— “有没有新版本”的唯一数据契约。
 *
 * 自己托管一个静态 JSON 就行（CDN / GitHub Pages / 对象存储都可以），内容形如：
 * ```json
 * {
 *   "latestVersion": "1.4.0",
 *   "minSupportedVersion": "1.2.0",
 *   "downloadUrl": "https://example.com/app-1.4.0.apk",
 *   "releaseNotes": "- 修复…\n- 新增…",
 *   "publishedAt": "2026-11-10T12:00:00Z"
 * }
 * ```
 *
 * 字段含义：
 * - [latestVersion]：最新版本号，按 [AppVersion] 的规则比较；带不带 `v` 都行
 * - [minSupportedVersion]：低于它的版本**必须**更新（强制更新）；null / 不填表示不强制
 * - [downloadUrl]：下载页或安装包地址。kit 不负责打开它 —— 各端 App 自己决定
 *   （Android 跳商店/浏览器、iOS 跳 App Store、Desktop 跳下载页）
 * - [releaseNotes]：纯文本更新说明。**是不可信文本**，UI 只能当文本渲染，不要当 HTML/Markdown 执行
 * - [publishedAt]：ISO-8601 时间，仅展示用；不参与比较
 *
 * 刻意不做多平台字段：一个清单只描述“这个平台/渠道该更新到哪”。
 * 三端产物不同（apk / ipa / dmg），给每端各自的清单 URL（App 侧配）比在清单里塞一张平台表更简单。
 */
@Serializable
data class UpdateManifest(
    val latestVersion: String,
    val minSupportedVersion: String? = null,
    val downloadUrl: String? = null,
    val releaseNotes: String = "",
    val publishedAt: String? = null,
)
