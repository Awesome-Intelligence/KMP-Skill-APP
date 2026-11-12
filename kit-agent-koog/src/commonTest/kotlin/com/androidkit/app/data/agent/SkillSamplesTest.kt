package com.androidkit.app.data.agent

import ai.koog.serialization.typeToken
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 黄金样例的跑法本身要能测 —— 不然这套东西只会「一直说通过」，
 * 等哪天样例跑挂了也不知道是工具坏了还是样例坏了。
 */
class SkillSamplesTest {

    @Serializable
    data class AddArgs(val title: String)

    @Serializable
    data class NoArgs(val unused: String = "")

    private class AddTool : SkillTool<AddArgs>(typeToken<AddArgs>(), "add_todo", "新增一条") {
        override suspend fun doExecute(args: AddArgs): ToolOutput = ToolOutput.Text("已添加待办：${args.title}")
    }

    private class ListTool : SkillTool<NoArgs>(typeToken<NoArgs>(), "list_todos", "列出待办") {
        override suspend fun doExecute(args: NoArgs): ToolOutput = ToolOutput.Text("（还没有待办）")
    }

    private val skill = SkillDefinition(
        id = "todo_assistant",
        name = "待办助手",
        description = "管理待办",
        systemPrompt = "你是待办助手",
        tools = listOf(AddTool(), ListTool()),
    )

    @Test
    fun 样例全过_没有失败项() = runBlocking {
        val failures = skill.runSamples(
            listOf(
                SkillSample("add_todo", """{"title":"买牛奶"}""", "买牛奶"),
                SkillSample("list_todos", "{}", "（还没有待办）"),
            ),
        )
        assertEquals(emptyList(), failures)
    }

    @Test
    fun 工具名不存在_报出来() = runBlocking {
        val failures = skill.runSamples(listOf(SkillSample("add_task", "{}", "x")))
        assertEquals(1, failures.size)
        assertTrue(failures.first().reason.contains("add_task"), "要说清是哪个工具：${failures.first().reason}")
    }

    @Test
    fun 返回值对不上_报出来() = runBlocking {
        val failures = skill.runSamples(listOf(SkillSample("add_todo", """{"title":"买牛奶"}""", "买鸡蛋")))
        assertEquals(1, failures.size)
        assertTrue(failures.first().reason.contains("买鸡蛋"), "要说清期望什么：${failures.first().reason}")
    }

    @Test
    fun 入参解不开_报出来而不是崩掉() = runBlocking {
        val failures = skill.runSamples(listOf(SkillSample("add_todo", """{"wrong":"买牛奶"}""", "买牛奶")))
        assertEquals(1, failures.size)
        assertTrue(failures.first().reason.contains("入参"), "要说清是入参的问题：${failures.first().reason}")
    }
}
