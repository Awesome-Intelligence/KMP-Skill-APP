package com.androidkit.app.data.storage

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StoreCodecTest {

    private val entity = StoredEntity(
        id = "goal-1",
        fields = mapOf(
            "name" to JsonPrimitive("完成马拉松"),
            "progress" to JsonPrimitive(0.65),
            "done" to JsonPrimitive(true),
            "tags" to JsonArray(listOf(JsonPrimitive("跑步"), JsonPrimitive("健身"))),
        ),
        body = "正文内容",
    )

    @Test
    fun markdown_往返_类型保持不变() {
        val codec = MarkdownCodec()
        val decoded = assertNotNull(codec.decode(entity.id, codec.encode(entity)))
        assertEquals(entity.id, decoded.id)
        assertEquals(entity.body, decoded.body)
        assertEquals(entity.fields, decoded.fields)
        assertEquals(0.65, decoded.fields.doubleOr("progress", 0.0))
        assertEquals(true, decoded.fields.boolOr("done", false))
        assertEquals(listOf("跑步", "健身"), decoded.fields.listOrEmpty("tags"))
    }

    @Test
    fun markdown_读旧数据_字符串值照样能取成数字和布尔() {
        val legacy = """
            ---
            id: "g1"
            progress: "0.65"
            done: "true"
            tags: [a, b]
            ---
            老正文
        """.trimIndent()
        val decoded = assertNotNull(MarkdownCodec().decode("g1", legacy))
        assertEquals("老正文", decoded.body)
        assertEquals(0.65, decoded.fields.doubleOr("progress", 0.0))
        assertEquals(true, decoded.fields.boolOr("done", false))
        assertEquals(listOf("a", "b"), decoded.fields.listOrEmpty("tags"))
    }

    @Test
    fun markdown_没有frontmatter_返回null() {
        assertTrue(MarkdownCodec().decode("x", "就是正文") == null)
    }

    @Test
    fun json_往返() {
        val codec = JsonCodec()
        assertEquals(".json", codec.extension)
        val decoded = assertNotNull(codec.decode(entity.id, codec.encode(entity)))
        assertEquals(entity.id, decoded.id)
        assertEquals(entity.body, decoded.body)
        assertEquals(entity.fields, decoded.fields)
    }
}
