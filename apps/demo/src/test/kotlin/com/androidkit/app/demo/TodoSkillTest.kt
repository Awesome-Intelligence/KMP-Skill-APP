package com.androidkit.app.demo

import com.androidkit.app.data.agent.SkillSample
import com.androidkit.app.data.agent.runSamples
import com.androidkit.app.data.agent.violations
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 待办 skill 的黄金样例 —— **skill 化没有编译期保障，样例就是它的「类型检查」**。
 *
 * 两道检查：
 * 1. [violations] —— skill 的静态体检（工具名、描述、重名…）
 * 2. [runSamples] —— 工具的黄金样例：模型应该发出哪个调用、该返回什么
 *
 * 换 app 的时候照抄这个文件，改成你自己的 skill 和样例。
 */
class TodoSkillTest {

    private val store = TodoStore()

    private val skill = todoSkill(store, "claude-opus-5-5")

    @Test
    fun `skill 符合契约`() {
        val violations = skill.violations()
        assertTrue("skill 体检没过：$violations", violations.isEmpty())
    }

    @Test
    fun `工具的黄金样例全部通过`() = runBlocking {
        val failures = skill.runSamples(
            listOf(
                // 空清单
                SkillSample("list_todos", "{}", "（还没有待办）"),
                // 新增一条
                SkillSample("add_todo", """{"title":"买牛奶"}""", "买牛奶"),
                // 删一条不存在的
                SkillSample("delete_todo", """{"id":"t999"}""", "没找到待办 t999"),
            ),
        )
        assertTrue("黄金样例没过：$failures", failures.isEmpty())
    }
}
