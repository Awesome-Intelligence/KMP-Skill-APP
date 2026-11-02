package com.androidkit.app.data.storage

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * 每个实体一个 JSON 文件（`{id}.json`）：
 *
 * ```json
 * { "id": "goal-1", "fields": { "progress": 0.65 }, "body": "..." }
 * ```
 *
 * 字段结构完整（数字/布尔/数组/嵌套对象都保真），适合不需要人类可读、只要机器读的场景。
 */
class JsonCodec(private val json: Json = Json { prettyPrint = true }) : StoreCodec {

    override val extension: String = ".json"

    override fun encode(entity: StoredEntity): String = json.encodeToString(
        JsonObject.serializer(),
        buildJsonObject {
            put("id", JsonPrimitive(entity.id))
            put("fields", JsonObject(entity.fields))
            put("body", JsonPrimitive(entity.body))
        }
    )

    override fun decode(id: String, raw: String): StoredEntity? = runCatching {
        val obj = json.parseToJsonElement(raw) as? JsonObject ?: return null
        val fields = (obj["fields"] as? JsonObject)?.toMap() ?: emptyMap()
        val body = (obj["body"] as? JsonPrimitive)?.content ?: ""
        val entityId = (obj["id"] as? JsonPrimitive)?.content ?: id
        StoredEntity(id = entityId, fields = fields, body = body)
    }.getOrNull()
}
