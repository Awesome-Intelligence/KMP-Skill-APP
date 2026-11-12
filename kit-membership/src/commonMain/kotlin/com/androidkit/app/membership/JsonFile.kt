package com.androidkit.app.membership

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path

/**
 * 一个 JSON 文件的读写（app 内部存储，三端通用）。
 *
 * 读不懂就用默认值 —— 会员文件坏了不该让 app 起不来（跟 kit 的 SettingsStore 一个路子）。
 */
internal class JsonFile<T>(
    private val path: Path,
    private val serializer: KSerializer<T>,
    private val default: T,
) {
    private val fs = FileSystem.SYSTEM
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** 读文件；文件不存在或读坏了都返回默认值。 */
    suspend fun read(): T = withContext(Dispatchers.IO) {
        if (!fs.exists(path)) return@withContext default
        runCatching {
            json.decodeFromString(serializer, fs.read(path) { readUtf8() })
        }.getOrDefault(default)
    }

    /** 写文件（覆盖整个文件）。 */
    suspend fun write(value: T) {
        withContext(Dispatchers.IO) {
            path.parent?.let { fs.createDirectories(it) }
            fs.write(path) {
                writeUtf8(json.encodeToString(serializer, value))
            }
        }
    }
}
