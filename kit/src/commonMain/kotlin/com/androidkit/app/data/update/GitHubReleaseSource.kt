package com.androidkit.app.data.update

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 直接读 GitHub Releases，适合开源项目：不用另建清单，发 Release 就等于发版本。
 *
 * 用的是 `/repos/{owner}/{repo}/releases/latest` —— GitHub 的这个端点**只返回已发布、
 * 非预发布**的最新 Release。所以 release.yml 里的 `draft: true` 草稿在手动发布前查不到，
 * 这是对的：草稿还没对外。
 *
 * [token] 可选：不填也能用（未登录限 60 次/小时/IP，配合 [UpdateCheckPolicy] 的节流足够）；
 * 私有仓库或高频检查再填 GitHub token。
 *
 * GitHub Release 没有“最低支持版本”这个概念，所以 [UpdateManifest.minSupportedVersion]
 * 恒为 null（不强制更新）。要强制更新就改用 [HttpUpdateSource] 的静态清单。
 */
class GitHubReleaseSource(
    private val client: HttpClient,
    private val repository: String,
    private val token: String? = null,
) : UpdateSource {

    override suspend fun fetch(): UpdateManifest {
        val response = client.get("$API_BASE/repos/$repository/releases/latest") {
            header(HttpHeaders.Accept, ACCEPT)
            header(HttpHeaders.UserAgent, USER_AGENT)
            header("X-GitHub-Api-Version", API_VERSION)
            if (!token.isNullOrBlank()) header(HttpHeaders.Authorization, "Bearer $token")
        }
        check(response.status.isSuccess()) { "GitHub Releases 请求失败：HTTP ${response.status.value}" }
        return parseGitHubRelease(response.bodyAsText())
    }

    private companion object {
        const val API_BASE = "https://api.github.com"
        const val ACCEPT = "application/vnd.github+json"
        const val API_VERSION = "2022-11-28"
        const val USER_AGENT = "Android-Kit-UpdateChecker"
    }
}

/**
 * GitHub Release JSON → [UpdateManifest]。
 *
 * 单独拆出来是为了不联网也能测（见 UpdateCheckerTest）。
 */
internal fun parseGitHubRelease(json: String): UpdateManifest {
    val release = gitHubJson.decodeFromString<GitHubRelease>(json)
    check(release.tagName.isNotBlank()) { "GitHub Release 缺少 tag_name" }
    return UpdateManifest(
        latestVersion = release.tagName,
        // latest 端点不会返回 draft / prerelease，这里不重复判断
        downloadUrl = release.htmlUrl,
        releaseNotes = release.body,
        publishedAt = release.publishedAt,
    )
}

@Serializable
private data class GitHubRelease(
    @SerialName("tag_name") val tagName: String = "",
    @SerialName("html_url") val htmlUrl: String? = null,
    @SerialName("body") val body: String = "",
    @SerialName("published_at") val publishedAt: String? = null,
)

private val gitHubJson = Json { ignoreUnknownKeys = true }
