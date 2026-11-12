package com.androidkit.app.data.agent

import ai.koog.serialization.typeToken
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * skill 的体检规则本身要能测 —— 否则这套检查只会「一直说通过」，
 * 等某条规则写错了你也不知道。
 */
class SkillContractTest {

    @Serializable
    data class NoArgs(val unused: String = "")

    private class Tool(
        name: String,
        description: String,
    ) : SkillTool<NoArgs>(typeToken<NoArgs>(), name, description) {
        override suspend fun doExecute(args: NoArgs): ToolOutput = ToolOutput.Text("ok")
    }

    private fun skill(
        id: String = "todo_assistant",
        name: String = "待办助手",
        description: String = "管理待办",
        systemPrompt: String = "你是待办助手",
        tools: List<SkillTool<*>> = listOf(Tool("list_todos", "列出待办")),
        maxTurns: Int = 8,
        maxTokens: Int = 16_000,
    ) = SkillDefinition(
        id = id,
        name = name,
        description = description,
        systemPrompt = systemPrompt,
        tools = tools,
        maxTurns = maxTurns,
        maxTokens = maxTokens,
    )

    @Test
    fun 合格的_skill_没有违反项() {
        assertEquals(emptyList(), skill().violations())
    }

    @Test
    fun 空字段_各自被点名() {
        val v = skill(id = "", name = "", description = "", systemPrompt = "").violations()
        val rules = v.map { it.rule }.toSet()

        assertTrue(SkillViolation.Rule.EMPTY_ID in rules, "空 id 要被报出来：$v")
        assertTrue(SkillViolation.Rule.EMPTY_NAME in rules, "空 name 要被报出来：$v")
        assertTrue(SkillViolation.Rule.EMPTY_DESCRIPTION in rules, "空 description 要被报出来：$v")
        assertTrue(SkillViolation.Rule.EMPTY_SYSTEM_PROMPT in rules, "空 systemPrompt 要被报出来：$v")
    }

    @Test
    fun 非_snake_case_的_id_被点名() {
        val v = skill(id = "TodoAssistant").violations()
        assertTrue(SkillViolation.Rule.ID_NOT_SNAKE_CASE in v.map { it.rule }, "驼峰 id 要被报出来：$v")
    }

    @Test
    fun 重名的工具_被点名() {
        val v = skill(
            tools = listOf(Tool("list_todos", "列出待办"), Tool("list_todos", "也列出待办")),
        ).violations()
        assertTrue(SkillViolation.Rule.DUPLICATE_TOOL_NAME in v.map { it.rule }, "重名工具要被报出来：$v")
    }

    @Test
    fun 驼峰工具名_和_空描述_被点名() {
        val v = skill(tools = listOf(Tool("listTodos", ""))).violations()
        val rules = v.map { it.rule }.toSet()

        assertTrue(SkillViolation.Rule.TOOL_NAME_NOT_SNAKE_CASE in rules, "驼峰工具名要被报出来：$v")
        assertTrue(SkillViolation.Rule.EMPTY_TOOL_DESCRIPTION in rules, "空工具描述要被报出来：$v")
    }

    @Test
    fun 非正的_limit_被点名() {
        val v = skill(maxTurns = 0, maxTokens = 0).violations()
        assertTrue(SkillViolation.Rule.INVALID_LIMITS in v.map { it.rule }, "非正的 limit 要被报出来：$v")
    }
}
