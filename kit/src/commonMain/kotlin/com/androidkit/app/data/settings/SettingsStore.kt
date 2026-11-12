package com.androidkit.app.data.settings

import com.androidkit.app.ui.theme.AppearanceMode
import com.androidkit.app.ui.theme.ThemeStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path

/**
 * 应用设置 —— 就这几个开关，够底座用了。
 *
 * 想加自己的项：加字段（带默认值，老配置文件照样能读），再在设置页加一行。
 * 字段顺序无所谓，JSON 按名字匹配。
 */
@Serializable
data class AppSettings(
    /** 主题风格（单色 / 位图） */
    val themeStyle: ThemeStyle = ThemeStyle.Mono,
    /** 深浅色：跟随系统 / 浅色 / 深色 */
    val appearanceMode: AppearanceMode = AppearanceMode.System,
    /** 关掉动效（无障碍） */
    val reducedMotion: Boolean = false,
)

/**
 * 设置持久化：一个 JSON 文件（app 内部存储），三端通用（okio）。
 *
 * 目录由调用方给（Android 传 filesDir，iOS 传 Documents，Desktop 传用户目录），
 * 引擎只管读写这一个文件。
 *
 * ponytail: 不是通用 KV 库 —— 就一个文件、一份 [AppSettings]。
 * 真要存一堆东西，上 [com.androidkit.app.data.storage.EntityStore]。
 */
class SettingsStore(private val rootDir: Path) {

    private val fs = FileSystem.SYSTEM
    private val file = rootDir / FILE_NAME

    // 读不懂就用默认值：设置坏了不该让 app 起不来
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** 读设置；文件不存在或读坏了都返回默认值。 */
    suspend fun load(): AppSettings = withContext(Dispatchers.IO) {
        if (!fs.exists(file)) return@withContext AppSettings()
        runCatching {
            json.decodeFromString(AppSettings.serializer(), fs.read(file) { readUtf8() })
        }.getOrDefault(AppSettings())
    }

    /** 写设置（覆盖整个文件）。 */
    suspend fun save(settings: AppSettings): Unit = withContext(Dispatchers.IO) {
        file.parent?.let { fs.createDirectories(it) }
        fs.write(file) {
            writeUtf8(json.encodeToString(AppSettings.serializer(), settings))
        }
    }

    private companion object {
        const val FILE_NAME = "settings.json"
    }
}
