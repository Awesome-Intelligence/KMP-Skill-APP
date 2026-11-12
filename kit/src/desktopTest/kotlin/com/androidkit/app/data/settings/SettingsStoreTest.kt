package com.androidkit.app.data.settings

import com.androidkit.app.ui.theme.AppearanceMode
import com.androidkit.app.ui.theme.ThemeStyle
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 设置落盘的真跑一遍 —— 这文件存的是用户选的主题/外观，
 * 写坏了就是「用户改了设置，重启后回到默认值」，值得有个回归。
 */
class SettingsStoreTest {

    private fun tempDir() = Files.createTempDirectory("settings-test").toFile().absolutePath.toPath()

    @Test
    fun 没有文件时返回默认设置() = runBlocking {
        val store = SettingsStore(tempDir())
        assertEquals(AppSettings(), store.load())
    }

    @Test
    fun 写入后能读回来() = runBlocking {
        val dir = tempDir()
        val settings = AppSettings(
            themeStyle = ThemeStyle.Swiss,
            appearanceMode = AppearanceMode.Dark,
            reducedMotion = true
        )
        SettingsStore(dir).save(settings)
        assertEquals(settings, SettingsStore(dir).load())
    }

    @Test
    fun 读不出来时退回默认设置() = runBlocking {
        val dir = tempDir()
        val store = SettingsStore(dir)
        store.save(AppSettings())
        // 模拟文件损坏：塞一段不是 JSON 的内容
        okio.FileSystem.SYSTEM.write(dir / "settings.json") { writeUtf8("not json at all") }
        assertEquals(AppSettings(), store.load())
    }
}
