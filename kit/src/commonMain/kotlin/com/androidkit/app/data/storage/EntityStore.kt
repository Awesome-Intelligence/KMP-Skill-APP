package com.androidkit.app.data.storage

import kotlinx.serialization.json.JsonElement

/**
 * 一个存下来的实体：id + 结构化字段 + 正文。
 *
 * 字段用 JSON 值（`JsonElement`）表示，所以数字、布尔、数组、嵌套对象都是**一等公民**，
 * 不用再塞进 `Map<String, String>` 里靠 `intOr`/`boolOr` 猜类型。
 */
data class StoredEntity(
    val id: String,
    val fields: Map<String, JsonElement> = emptyMap(),
    val body: String = "",
)

/**
 * 存储抽象：**不绑定任何文件格式**。
 *
 * 引擎只管「字节往哪写、怎么列出来」，实体长什么样由 [StoreCodec] 决定。
 * 换存储（文件 → 数据库）或换格式（markdown → json）都只动实现，调用方不变。
 *
 * 实现：
 * - [FileStorageEngine] — app 内部存储（okio，Android / iOS / Desktop）
 * - `SafStorageEngine` — Android SAF 用户自选目录（androidMain）
 */
interface EntityStore {

    /** 写入（新建或覆盖）`{subdir}/{entityId}{codec 后缀}`。 */
    suspend fun write(subdir: String, entity: StoredEntity)

    /** 读取实体，不存在返回 null。 */
    suspend fun read(subdir: String, entityId: String): StoredEntity?

    /** 删除实体；本来就不存在返回 false。 */
    suspend fun delete(subdir: String, entityId: String): Boolean

    /** 按精确文件名删任意文件（附件等）；不存在返回 false。 */
    suspend fun deleteFile(subdir: String, filename: String): Boolean

    /** 列出子目录下的实体 id。 */
    suspend fun listIds(subdir: String): List<String>

    /** 列出子目录下的二进制（附件）文件名。 */
    suspend fun listBinaryFilenames(subdir: String): List<String>

    /** 读出子目录下所有实体。 */
    suspend fun loadAll(subdir: String): List<StoredEntity>

    /** 写二进制附件。 */
    suspend fun writeBinary(subdir: String, filename: String, bytes: ByteArray, mimeType: String)

    /** 读二进制附件，不存在返回 null。 */
    suspend fun readBinary(subdir: String, filename: String): ByteArray?

    /**
     * 图片的可加载标识，null 表示还没有这张图。
     *
     * - [FileStorageEngine]：绝对路径（`/data/.../pic.png`）
     * - SAF 引擎（Android）：`content://` URI
     *
     * 各端再把它转成自己的图片加载模型（JVM 上 Coil 吃 File，iOS 上吃 NSURL）。
     */
    fun imageUri(subdir: String, filename: String): String?
}
