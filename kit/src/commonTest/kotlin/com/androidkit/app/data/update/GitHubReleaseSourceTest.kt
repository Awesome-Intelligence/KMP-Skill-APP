package com.androidkit.app.data.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GitHubReleaseSourceTest {

    @Test
    fun `GitHub Release 映射成版本清单`() {
        val json = """
            {
              "tag_name": "v1.4.0",
              "html_url": "https://github.com/owner/repo/releases/tag/v1.4.0",
              "body": "## 更新内容\n- 修复问题",
              "published_at": "2026-11-10T12:00:00Z",
              "draft": false,
              "prerelease": false
            }
        """.trimIndent()

        val manifest = parseGitHubRelease(json)

        assertEquals("v1.4.0", manifest.latestVersion)
        assertEquals("https://github.com/owner/repo/releases/tag/v1.4.0", manifest.downloadUrl)
        assertEquals("## 更新内容\n- 修复问题", manifest.releaseNotes)
        assertEquals("2026-11-10T12:00:00Z", manifest.publishedAt)
        // GitHub Release 没有“最低支持版本”，所以不会强制更新
        assertEquals(null, manifest.minSupportedVersion)
    }

    @Test
    fun `缺少 tag_name 时失败`() {
        assertFailsWith<IllegalStateException> {
            parseGitHubRelease("""{"body": "没有 tag"}""")
        }
    }
}
