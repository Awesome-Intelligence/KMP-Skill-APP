package com.androidkit.app.data.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path

/**
 * 文件存储引擎（app 内部存储，okio）。三端通用：Android / iOS / Desktop。
 *
 * 磁盘格式由 [codec] 决定，引擎本身跟格式无关：
 * `FileStorageEngine(rootDir)` 默认写 `.md`（frontmatter + 正文），
 * `FileStorageEngine(rootDir, JsonCodec())` 写 `.json`。
 *
 * ponytail: 原来用 java.io.File —— JVM 专属，iOS 编不了。换成 okio（KMP），
 * 一份实现三端通用；路径由各端传进来（Android 传 filesDir，iOS 传 Documents 目录）。
 *
 * @param rootDir the directory that holds all entity subdirectories
 * @param codec   实体 ↔ 文件文本的编解码器
 */
class FileStorageEngine(
    private val rootDir: Path,
    private val codec: StoreCodec = MarkdownCodec()
) : EntityStore {

    private val fs: FileSystem = FileSystem.SYSTEM

    /** 实体文件的后缀集合：当前 codec 的 + 旧版本遗留的 md。 */
    private val entityExtensions = setOf(codec.extension, ".md", ".markdown")

    override suspend fun write(subdir: String, entity: StoredEntity): Unit = withContext(Dispatchers.IO) {
        val file = fileIn(subdir, entity.id + codec.extension)
        file.parent?.let { fs.createDirectories(it) }
        fs.write(file) {
            writeUtf8(codec.encode(entity))
        }
    }

    override suspend fun read(subdir: String, entityId: String): StoredEntity? =
        withContext(Dispatchers.IO) {
            val file = fileIn(subdir, entityId + codec.extension)
            if (!fs.exists(file)) return@withContext null
            codec.decode(entityId, fs.read(file) { readUtf8() })
        }

    override suspend fun delete(subdir: String, entityId: String): Boolean =
        deleteFile(subdir, entityId + codec.extension)

    override suspend fun deleteFile(subdir: String, filename: String): Boolean =
        withContext(Dispatchers.IO) {
            val file = fileIn(subdir, filename)
            if (!fs.exists(file)) return@withContext false
            fs.delete(file)
            true
        }

    override suspend fun listIds(subdir: String): List<String> = withContext(Dispatchers.IO) {
        listFiles(subdir)
            .filter { it.name.endsWith(codec.extension) }
            .map { it.name.removeSuffix(codec.extension) }
    }

    override suspend fun listBinaryFilenames(subdir: String): List<String> = withContext(Dispatchers.IO) {
        listFiles(subdir)
            .filterNot { path -> entityExtensions.any { path.name.endsWith(it) } }
            .map { it.name }
    }

    override suspend fun loadAll(subdir: String): List<StoredEntity> =
        withContext(Dispatchers.IO) {
            listFiles(subdir)
                .filter { it.name.endsWith(codec.extension) }
                .sortedBy { it.name }
                .mapNotNull { file ->
                    val id = file.name.removeSuffix(codec.extension)
                    codec.decode(id, fs.read(file) { readUtf8() })
                }
        }

    override suspend fun writeBinary(subdir: String, filename: String, bytes: ByteArray, mimeType: String): Unit =
        withContext(Dispatchers.IO) {
            val file = fileIn(subdir, filename)
            file.parent?.let { fs.createDirectories(it) }
            fs.write(file) { write(bytes) }
        }

    override suspend fun readBinary(subdir: String, filename: String): ByteArray? =
        withContext(Dispatchers.IO) {
            val file = fileIn(subdir, filename)
            if (!fs.exists(file)) return@withContext null
            fs.read(file) { readByteArray() }
        }

    override fun imageUri(subdir: String, filename: String): String? {
        val file = fileIn(subdir, filename)
        return if (fs.exists(file)) file.toString() else null
    }

    // ─── Internal helpers ───────────────────────────────────────────────────

    /** 子目录下的普通文件（不含子目录）。 */
    private fun listFiles(subdir: String): List<Path> =
        fs.listOrNull(dirOf(subdir))
            .orEmpty()
            .filter { path -> fs.metadataOrNull(path)?.isRegularFile == true }

    private fun dirOf(subdir: String): Path = rootDir / subdir

    private fun fileIn(subdir: String, filename: String): Path = dirOf(subdir) / filename
}
