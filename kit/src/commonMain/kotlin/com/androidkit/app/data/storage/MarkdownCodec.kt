package com.androidkit.app.data.storage

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * YAML-ish frontmatter + 正文的文件格式（`{id}.md`）。
 *
 * ```
 * ---
 * id: "goal-1"
 * progress: 0.65
 * done: true
 * tags: ["跑步","健身"]
 * ---
 * Markdown body.
 * ```
 *
 * 值的写法：
 * - 字符串 → 带引号（`"…"`，内部转义引号/换行）
 * - 数字 / 布尔 / null → 直接写裸字面量
 * - 数组 / 对象 → 紧凑 JSON 写一行
 *
 * 兼容旧数据：老版本把**所有**值都写成带引号的字符串，这里读回来仍是字符串；
 * 取值用 [intOr] / [doubleOr] / [boolOr]，字符串会被宽松地转成数字/布尔。
 */
class MarkdownCodec : StoreCodec {

    override val extension: String = ".md"

    private val json = Json

    override fun encode(entity: StoredEntity): String = buildString {
        appendLine(SEPARATOR)
        for ((k, v) in entity.fields) {
            append(k).append(": ").append(encodeValue(v)).appendLine()
        }
        appendLine(SEPARATOR)
        append(entity.body)
    }

    override fun decode(id: String, raw: String): StoredEntity? {
        var text = raw
        if (text.startsWith("﻿")) text = text.removePrefix("﻿") // BOM
        val lines = text.lineSequence().toList()
        if (lines.isEmpty() || lines[0].trim() != SEPARATOR) return null

        val fmLines = mutableListOf<String>()
        var bodyStart = -1
        for (i in 1 until lines.size) {
            if (lines[i].trim() == SEPARATOR) {
                bodyStart = i + 1
                break
            }
            fmLines.add(lines[i])
        }
        if (bodyStart < 0) return null

        val body = lines.subList(bodyStart, lines.size).joinToString("\n").removePrefix("\n")
        return StoredEntity(id = id, fields = parseFrontmatter(fmLines), body = body)
    }

    // ─── 值编解码 ────────────────────────────────────────────────────────────

    private fun encodeValue(value: JsonElement): String = when (value) {
        is JsonNull -> "null"
        is JsonPrimitive ->
            if (value.isString) "\"${escape(value.content)}\"" else value.content
        else -> value.toString() // 数组 / 对象 → 紧凑 JSON
    }

    private fun parseFrontmatter(lines: List<String>): Map<String, JsonElement> {
        val result = mutableMapOf<String, JsonElement>()
        for (line in lines) {
            val colonIdx = line.indexOf(':')
            if (colonIdx <= 0) continue
            val key = line.substring(0, colonIdx).trim()
            if (key.isEmpty()) continue
            val rest = line.substring(colonIdx + 1).trim()
            if (rest.isEmpty()) continue
            result[key] = parseValue(rest)
        }
        return result
    }

    private fun parseValue(rest: String): JsonElement {
        if (rest.startsWith("\"")) {
            val endQuote = rest.lastIndexOf('"')
            if (endQuote > 0) return JsonPrimitive(unescape(rest.substring(1, endQuote)))
        }
        runCatching { json.parseToJsonElement(rest) }.getOrNull()?.let { return it }
        // 老版本允许写不带引号的列表：`[a, b]`
        if (rest.startsWith("[")) {
            val items = rest.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"", "'") }
                .filter { it.isNotEmpty() }
            return JsonArray(items.map { JsonPrimitive(it) })
        }
        return JsonPrimitive(rest)
    }

    private fun escape(value: String): String = buildString(value.length) {
        for (c in value) {
            when (c) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(c)
            }
        }
    }

    private fun unescape(value: String): String = buildString(value.length) {
        var i = 0
        while (i < value.length) {
            val c = value[i]
            if (c == '\\' && i + 1 < value.length) {
                when (value[i + 1]) {
                    '\\' -> { append('\\'); i += 2 }
                    '"' -> { append('"'); i += 2 }
                    'n' -> { append('\n'); i += 2 }
                    'r' -> { append('\r'); i += 2 }
                    else -> { append(c); i += 1 }
                }
            } else {
                append(c)
                i += 1
            }
        }
    }

    private companion object {
        const val SEPARATOR = "---"
    }
}
