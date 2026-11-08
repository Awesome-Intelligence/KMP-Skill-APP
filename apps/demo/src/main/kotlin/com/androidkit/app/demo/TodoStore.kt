package com.androidkit.app.demo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 一条待办。 */
data class Todo(val id: String, val title: String)

/**
 * 业务数据 + 业务动作：待办清单，**只存在手机本地**。
 *
 * 这个类就是「逻辑 api 化」的落点 —— **UI 和 agent 的工具调的是同一批方法**：
 * - UI 的「删」按钮 → [remove]
 * - agent 的 delete_todo 工具 → [remove]
 *
 * 校验、分支、结果文案都在这一层；tool 只做单行透传（见 [TodoSkill.kt]）。
 * 这样 agent 删的待办和用户删的待办，行为和文案完全一致。
 *
 * ponytail: 内存版，app 退出即失忆。要跨进程留住就换成 kit 的 EntityStore（写文件 + codec）。
 */
class TodoStore {

    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()

    private var seq = 0

    /** 新增一条待办。返回一句结果文案 —— 用户看它，模型也看它，同一句。 */
    fun add(title: String): String {
        val t = title.trim()
        if (t.isEmpty()) return "没添加：待办内容是空的"
        val todo = Todo(id = "t${++seq}", title = t)
        _todos.value = _todos.value + todo
        return "已添加待办 ${todo.id}：${todo.title}"
    }

    /** 删除一条待办。返回一句结果文案。 */
    fun remove(id: String): String {
        val before = _todos.value
        if (before.none { it.id == id }) return "没找到待办 $id"
        _todos.value = before.filterNot { it.id == id }
        return "已删除待办 $id"
    }

    fun find(id: String): Todo? = _todos.value.firstOrNull { it.id == id }

    /** 一条待办的文本格式 —— **只有这一处**，用户看到的和模型看到的同一个格式。 */
    fun format(todo: Todo): String = "${todo.id}. ${todo.title}"

    /** 列表摘要（给模型看）：一行一条。 */
    fun summary(): String =
        _todos.value.joinToString("\n") { format(it) }.ifEmpty { "（还没有待办）" }
}
