package com.androidkit.app.demo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 一条待办。 */
data class Todo(val id: String, val title: String)

/**
 * 业务数据：待办清单，**只存在手机本地**。
 *
 * 工具（skill 的 Tool）直接摸它，UI 也直接看它 —— 这就是"业务只出现在两个地方"里的业务那半边。
 *
 * ponytail: 内存版，app 退出即失忆。要跨进程留住就换成 kit 的 EntityStore（写文件 + codec）。
 */
class TodoStore {

    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()

    private var seq = 0

    fun add(title: String): Todo {
        val todo = Todo(id = "t${++seq}", title = title.trim())
        _todos.value = _todos.value + todo
        return todo
    }

    fun remove(id: String): Boolean {
        val before = _todos.value
        if (before.none { it.id == id }) return false
        _todos.value = before.filterNot { it.id == id }
        return true
    }

    fun find(id: String): Todo? = _todos.value.firstOrNull { it.id == id }

    /** 工具返回值：一行一条，纯文本。 */
    fun summary(): String =
        _todos.value.joinToString("\n") { "${it.id}. ${it.title}" }.ifEmpty { "（还没有待办）" }
}
