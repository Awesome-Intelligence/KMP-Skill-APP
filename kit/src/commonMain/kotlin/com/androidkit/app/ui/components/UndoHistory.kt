package com.androidkit.app.ui.components

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue

/**
 * 编辑器撤销/重做历史
 *
 * 存的是「文本 + 光标」快照：撤销一步 = 回到上一份快照，光标也跟着回去。
 * 连续输入在 [COALESCE_MS] 窗口内合并成一步（否则一次撤销只退一个字）。
 */
@Stable
class UndoHistory {

    companion object {
        /** 连续输入的合并窗口（毫秒）：两次输入间隔超过就另起一步 */
        private const val COALESCE_MS = 800L

        /** 最多保留多少步 */
        private const val MAX_STEPS = 50
    }

    /** 全部快照，[index] 指向当前所在的那一份 */
    private val states = mutableStateListOf<TextFieldValue>()
    private var index by mutableIntStateOf(0)

    /** 当前编辑会话标识：换了记录/x记 就变，历史要清空 */
    var sessionKey: String? = null
        private set

    private var lastPushAt = 0L
    private var lastWasTyping = false

    /** 还没有任何快照 */
    val isEmpty: Boolean get() = states.isEmpty()

    /** 能否撤销 */
    val canUndo: Boolean get() = index > 0

    /** 能否重做 */
    val canRedo: Boolean get() = index < states.lastIndex

    /** 开一个新会话：丢掉旧历史，只留当前内容 */
    fun startSession(key: String?, value: TextFieldValue) {
        sessionKey = key
        states.clear()
        states.add(value)
        index = 0
        lastPushAt = 0L
        lastWasTyping = false
    }

    /**
     * 记入一步新内容
     *
     * @param typing true = 逐字输入（会和上一步合并）；false = 整块操作（工具栏插入等），单独成步
     */
    fun push(value: TextFieldValue, typing: Boolean = false) {
        if (states.isEmpty()) {
            startSession(sessionKey, value)
            return
        }
        val now = System.currentTimeMillis()
        val merge = typing && lastWasTyping && now - lastPushAt < COALESCE_MS && index == states.lastIndex
        if (merge) {
            states[index] = value
        } else {
            // 新的一步：redo 分支作废
            while (states.size > index + 1) states.removeAt(states.lastIndex)
            states.add(value)
            index = states.size - 1
            while (states.size > MAX_STEPS) {
                states.removeAt(0)
                index--
            }
        }
        lastPushAt = now
        lastWasTyping = typing
    }

    /** 撤销一步，把目标快照交给 [onApply] 回填 */
    fun undo(onApply: (TextFieldValue) -> Unit) {
        if (!canUndo) return
        index--
        lastWasTyping = false
        onApply(states[index])
    }

    /** 重做一步，把目标快照交给 [onApply] 回填 */
    fun redo(onApply: (TextFieldValue) -> Unit) {
        if (!canRedo) return
        index++
        lastWasTyping = false
        onApply(states[index])
    }
}
