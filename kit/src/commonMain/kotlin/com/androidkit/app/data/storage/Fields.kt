package com.androidkit.app.data.storage

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * 字段取值helper。
 *
 * 宽松：老数据里所有值都是字符串（`progress: "0.65"`），这里照样能取成数字/布尔，
 * 所以旧 md 文件不用迁移也能用新的类型化接口读。
 */

private fun JsonElement.textOrNull(): String? = when {
    this is JsonNull -> null
    this is JsonPrimitive -> content
    else -> toString()
}

private fun JsonElement.primitiveTextOrNull(): String? =
    if (this is JsonPrimitive && this !is JsonNull) content else null

fun Map<String, JsonElement>.str(key: String): String? = this[key]?.textOrNull()

fun Map<String, JsonElement>.strOrEmpty(key: String): String = str(key) ?: ""

fun Map<String, JsonElement>.intOr(key: String, default: Int): Int =
    str(key)?.trim()?.toIntOrNull() ?: default

fun Map<String, JsonElement>.longOr(key: String, default: Long): Long =
    str(key)?.trim()?.toLongOrNull() ?: default

fun Map<String, JsonElement>.longOrNull(key: String): Long? =
    str(key)?.trim()?.toLongOrNull()

fun Map<String, JsonElement>.doubleOr(key: String, default: Double): Double =
    str(key)?.trim()?.toDoubleOrNull() ?: default

fun Map<String, JsonElement>.boolOr(key: String, default: Boolean): Boolean =
    str(key)?.trim()?.toBooleanStrictOrNull() ?: default

/** 字符串列表：JSON 数组，或老格式的逗号分隔串（`"a,b"`）。 */
fun Map<String, JsonElement>.listOrEmpty(key: String): List<String> {
    val raw = str(key)?.trim() ?: return emptyList()
    if (raw.startsWith("[")) {
        runCatching {
            val arr = Json.parseToJsonElement(raw) as? JsonArray
            arr?.mapNotNull { it.primitiveTextOrNull() }
        }.getOrNull()?.let { return it }
    }
    return raw.removeSurrounding("[", "]")
        .split(",")
        .map { it.trim().removeSurrounding("\"", "'") }
        .filter { it.isNotEmpty() }
}

/**
 * 归档标记。兼容旧版本写入的 `isDeleted` 键，避免历史数据里的归档项被"复活"。
 */
fun Map<String, JsonElement>.archivedFlagOr(default: Boolean): Boolean {
    for (key in listOf("isArchived", "isDeleted")) {
        val v = str(key)?.trim() ?: continue
        v.toBooleanStrictOrNull()?.let { return it }
    }
    return default
}

/** 归档时间。兼容旧版本写入的 `deletedDate` 键。 */
fun Map<String, JsonElement>.archivedDateOrNull(): Long? =
    longOrNull("archivedDate") ?: longOrNull("deletedDate")
