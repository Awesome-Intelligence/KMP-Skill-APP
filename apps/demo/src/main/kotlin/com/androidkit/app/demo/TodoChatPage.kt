package com.androidkit.app.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.androidkit.app.BuildConfig
import com.androidkit.app.data.agent.AgentEvent
import com.androidkit.app.data.agent.AgentSession
import com.androidkit.app.data.agent.KoogAgentRuntime
import com.androidkit.app.data.agent.ToolApprover
import com.androidkit.app.ui.components.AppCard
import com.androidkit.app.ui.components.CardTitle
import com.androidkit.app.ui.components.ConfirmDialog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * 待办助手 —— 业务 demo 页。
 *
 * 这一页是**业务**：数据（TodoStore）、skill（TodoSkill）、界面（这里）。
 * 通用能力（组件、agent 运行时）全在 :kit 和 :kit-agent-koog 里，这里一行都不重复实现。
 */
@Composable
fun TodoChatPage() {
    val store = remember { TodoStore() }
    val approver = remember { UiApprover() }
    val session = remember(store, approver) { createSession(store, approver) }

    val todos by store.todos.collectAsState()
    val pending by approver.pending.collectAsState()

    val messages = remember { mutableStateListOf<Msg>() }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.scrollToItem(messages.lastIndex)
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            CardTitle(icon = Icons.Outlined.List, title = "待办（${todos.size}）")
            if (todos.isEmpty()) {
                Text(
                    "还没有待办 —— 在下面跟助手说「记一下买牛奶」",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            todos.forEach { todo ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = store.format(todo),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    // 删的是同一个 store.remove —— agent 删待办走的是同一条路，
                    // 拿到的也是同一句结果文案（见 TodoStore.kt 的「逻辑 api 化」注释）
                    TextButton(onClick = { status = store.remove(todo.id) }) { Text("删") }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(messages) { m ->
                Text(
                    text = m.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (m.fromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (m.fromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(10.dp),
                )
            }
        }

        if (session == null) {
            Text(
                "没配 API key：构建时传 -PanthropicApiKey=xxx（或设环境变量 ANTHROPIC_API_KEY）",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (status.isNotEmpty()) {
            Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("我今天有几件事？") },
                enabled = !busy,
                maxLines = 4,
            )
            Button(
                enabled = !busy && input.isNotBlank() && session != null,
                onClick = {
                    val s = session ?: return@Button
                    val text = input.trim()
                    input = ""
                    messages += Msg(true, text)
                    busy = true
                    status = ""
                    scope.launch {
                        var bubble = -1   // 当前气泡下标；-1 = 还没开始
                        s.send(text).collect { ev ->
                            when (ev) {
                                // 增量块：往当前气泡尾巴上追加（打字机效果）
                                is AgentEvent.Text ->
                                    if (bubble < 0) {
                                        messages += Msg(false, ev.text)
                                        bubble = messages.lastIndex
                                    } else {
                                        val m = messages[bubble]
                                        messages[bubble] = m.copy(text = m.text + ev.text)
                                    }
                                is AgentEvent.ToolUse -> status = "调用工具 ${ev.name}…"
                                // 正文已经由 Text 事件发过了，Result 只用来收尾
                                is AgentEvent.Result -> bubble = -1
                                is AgentEvent.Error -> {
                                    messages += Msg(false, "⚠️ ${ev.message}")
                                    bubble = -1
                                }
                            }
                        }
                        busy = false
                        status = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp),
            ) { Text("发送") }
        }
    }

    // 危险工具的确认闸门：agent 想删待办时，用户点"删"才真删
    pending?.let { req ->
        ConfirmDialog(
            title = "确认删除？",
            message = "助手想删除待办 ${req.target}",
            onConfirm = { approver.resolve(true) },
            onDismiss = { approver.resolve(false) },
        )
    }
}

private data class Msg(val fromUser: Boolean, val text: String)

/**
 * UI 侧的审批闸门：只读工具直接放行，危险工具弹窗等用户点头。
 *
 * agent 的工具执行在后台协程里，所以这里用 [CompletableDeferred] 把那次调用挂起，
 * 等用户在弹窗里给个答案。
 */
class UiApprover : ToolApprover {

    data class Request(val tool: String, val target: String, val answer: CompletableDeferred<Boolean>)

    private val _pending = MutableStateFlow<Request?>(null)
    val pending: StateFlow<Request?> = _pending.asStateFlow()

    override suspend fun approve(name: String, input: JsonObject): Boolean {
        // ponytail: 只对"删"这类危险工具弹窗。要更细就按工具名配一张白名单表。
        if (name != TOOL_DELETE_TODO) return true
        val answer = CompletableDeferred<Boolean>()
        _pending.value = Request(name, input["id"]?.jsonPrimitive?.contentOrNull ?: "?", answer)
        return try {
            answer.await()
        } finally {
            _pending.value = null
        }
    }

    fun resolve(ok: Boolean) {
        _pending.value?.answer?.complete(ok)
        _pending.value = null
    }
}

/** 从构建配置建会话。没 key 就返回 null，UI 会提示怎么配。 */
private fun createSession(store: TodoStore, approver: ToolApprover): AgentSession? {
    val key = BuildConfig.ANTHROPIC_API_KEY.ifBlank { return null }
    val runtime = KoogAgentRuntime(key, BuildConfig.ANTHROPIC_BASE_URL)
    return runtime.session(todoSkill(store, BuildConfig.ANTHROPIC_MODEL), approver)
}
