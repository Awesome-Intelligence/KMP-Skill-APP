package com.androidkit.app.data.agent

import ai.koog.agents.core.tools.ToolCallMetadata
import ai.koog.agents.core.tools.ToolException
import ai.koog.serialization.typeToken
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SkillToolTest {

    @Serializable
    data class SearchArgs(val query: String, val limit: Int = 10)

    private class SearchTool : SkillTool<SearchArgs>(
        typeToken<SearchArgs>(), "search", "搜索本地数据",
    ) {
        var seen: SearchArgs? = null
        override suspend fun doExecute(args: SearchArgs): ToolOutput {
            seen = args
            return ToolOutput.Text("ok:${args.query}")
        }
    }

    @Test
    fun 入参_schema_从数据类自动生成() {
        val tool = SearchTool()
        assertEquals("search", tool.descriptor.name)
        assertTrue(tool.descriptor.requiredParameters.any { it.name == "query" })
    }

    @Test
    fun 不放行_工具不执行() {
        val tool = SearchTool()
        tool.bind(ToolApprover { _, _ -> ToolApproval.Deny })

        assertFailsWith<ToolException.ValidationFailure> {
            runBlocking { tool.execute(SearchArgs("kotlin"), ToolCallMetadata.EMPTY) }
        }
        assertEquals(null, tool.seen)
    }

    @Test
    fun 放行_工具执行_参数原样透传() {
        val tool = SearchTool()
        var approvedName: String? = null
        var approvedQuery: String? = null
        tool.bind(ToolApprover { name, input ->
            approvedName = name
            approvedQuery = input["query"]?.jsonPrimitive?.content
            ToolApproval.Allow
        })

        val result = runBlocking { tool.execute(SearchArgs("kotlin", 3), ToolCallMetadata.EMPTY) }

        assertEquals("ok:kotlin", result.text)
        assertEquals("search", approvedName)
        assertEquals("kotlin", approvedQuery)
        assertEquals(3, tool.seen?.limit)
    }
}
