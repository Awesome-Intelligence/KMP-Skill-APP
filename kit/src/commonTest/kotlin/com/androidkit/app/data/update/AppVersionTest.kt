package com.androidkit.app.data.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppVersionTest {

    @Test
    fun 解析带前缀和构建元数据的版本号() {
        val version = AppVersion.parseOrNull("v1.2.3+build.7")!!

        assertEquals(listOf(1, 2, 3), version.numbers)
        assertEquals("build.7", version.buildMetadata)
        assertEquals("1.2.3+build.7", version.toString())
    }

    @Test
    fun 解析预发布版本() {
        val version = AppVersion.parseOrNull("1.2.3-beta.2")!!

        assertEquals(listOf("beta", "2"), version.preRelease)
        assertEquals("1.2.3-beta.2", version.toString())
    }

    @Test
    fun 缺少的版本位补零() {
        assertEquals(0, AppVersion.parseOrNull("1.2")!!.compareTo(AppVersion.parseOrNull("1.2.0")!!))
        assertEquals(0, AppVersion.parseOrNull("v1")!!.compareTo(AppVersion.parseOrNull("1.0.0")!!))
    }

    @Test
    fun 按数字段比较而不是字典序() {
        assertTrue(AppVersion.parseOrNull("1.10.0")!! > AppVersion.parseOrNull("1.9.0")!!)
        assertTrue(AppVersion.parseOrNull("2.0.0")!! > AppVersion.parseOrNull("1.99.99")!!)
    }

    @Test
    fun 正式版大于同版本的预发布版() {
        assertTrue(AppVersion.parseOrNull("1.2.3")!! > AppVersion.parseOrNull("1.2.3-rc.1")!!)
        assertTrue(AppVersion.parseOrNull("1.2.3-rc.1")!! > AppVersion.parseOrNull("1.2.3-beta.9")!!)
    }

    @Test
    fun 预发布标识符按语义化规则比较() {
        // 数字段按数值比，不是按字符串
        assertTrue(AppVersion.parseOrNull("1.2.3-beta.10")!! > AppVersion.parseOrNull("1.2.3-beta.2")!!)
        // 数字 < 字母
        assertTrue(AppVersion.parseOrNull("1.2.3-1")!! < AppVersion.parseOrNull("1.2.3-alpha")!!)
        // 短的 pre-release 更小
        assertTrue(AppVersion.parseOrNull("1.2.3-alpha")!! < AppVersion.parseOrNull("1.2.3-alpha.1")!!)
    }

    @Test
    fun 构建元数据不参与比较() {
        assertEquals(
            0,
            AppVersion.parseOrNull("1.2.3+001")!!.compareTo(AppVersion.parseOrNull("1.2.3+002")!!),
        )
    }

    @Test
    fun 非法版本号返回空() {
        listOf("", "v", "1.a.3", "1..3", "1.2.3-", "1.2.3-alpha..2", "+build").forEach { raw ->
            assertNull(AppVersion.parseOrNull(raw), "「$raw」不该被解析成版本号")
        }
    }
}
