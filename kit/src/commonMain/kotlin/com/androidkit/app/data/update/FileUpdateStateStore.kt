package com.androidkit.app.data.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path

/**
 * 把 [UpdateState] 落到一个 JSON 文件（app 内部存储，三端通用）。
 *
 * 目录由调用方给（Android 传 filesDir，iOS 传 Documents，Desktop 传用户目录），
 * 和 `SettingsStore` 一个路子。读坏了就退回默认状态 —— 检查更新失败不该影响 app 启动。
 */
class FileUpdateStateStore(private val rootDir: Path) : UpdateStateStore {

    private val fs = FileSystem.SYSTEM
    private val file = rootDir / FILE_NAME

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun load(): UpdateState = withContext(Dispatchers.IO) {
        if (!fs.exists(file)) return@withContext UpdateState()
        runCatching {
            json.decodeFromString(UpdateState.serializer(), fs.read(file) { readUtf8() })
        }.getOrDefault(UpdateState())
    }

    override suspend fun save(state: UpdateState): Unit = withContext(Dispatchers.IO) {
        file.parent?.let { fs.createDirectories(it) }
        fs.write(file) {
            writeUtf8(json.encodeToString(UpdateState.serializer(), state))
        }
    }

    private companion object {
        const val FILE_NAME = "update-state.json"
    }
}
