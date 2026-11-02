package com.androidkit.app.data.storage

/**
 * 实体 ↔ 文件文本的编解码器。
 *
 * 存储引擎只读写字节，「这个实体在磁盘上长什么样」全归 codec。
 * 加一种新格式（比如 csv / 二进制）= 实现这一个接口，引擎一行都不用改。
 */
interface StoreCodec {

    /** 实体文件后缀（含点，如 `.md`）。引擎按它拼文件名、过滤文件。 */
    val extension: String

    /** 实体 → 文件全文。 */
    fun encode(entity: StoredEntity): String

    /** 文件全文 → 实体；解析不出来返回 null（引擎跳过该文件）。 */
    fun decode(id: String, raw: String): StoredEntity?
}
