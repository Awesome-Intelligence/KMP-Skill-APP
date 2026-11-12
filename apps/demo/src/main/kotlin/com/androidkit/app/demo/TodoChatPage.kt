package com.androidkit.app.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.androidkit.app.BuildConfig
import com.androidkit.app.data.agent.AgentSession
import com.androidkit.app.data.agent.ChatLog
import com.androidkit.app.data.agent.KoogAgentRuntime
import com.androidkit.app.data.agent.ToolApproval
import com.androidkit.app.data.agent.ToolApprover
import com.androidkit.app.ui.agent.ChatMessageList
import com.androidkit.app.ui.agent.ToolApprovalCard
import com.androidkit.app.ui.components.AppBottomNavigation
import com.androidkit.app.ui.components.AppCard
import com.androidkit.app.ui.components.BottomNavItem
import com.androidkit.app.ui.components.CardTitle
import com.androidkit.app.ui.components.Header
import com.androidkit.app.ui.shell.AppShell
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

// ponytail: 五个页签沿 BatPlan 的底栏结构（首页/目标/快速添加/人脉/我的），但页面和图标
// 都还没定 —— 图标统一用问号占位；真页面定了换 icon、接内容
private val navItems = listOf(
    BottomNavItem("首页", Icons.Outlined.HelpOutline),
    BottomNavItem("目标", Icons.Outlined.HelpOutline),
    BottomNavItem("快速添加", Icons.Outlined.HelpOutline, alwaysFilled = true),
    BottomNavItem("人脉", Icons.Outlined.HelpOutline),
    BottomNavItem("我的", Icons.Outlined.HelpOutline),
)

/**
 * 待办助手 —— 业务 demo 页。
 *
 * 这一页是**业务**：数据（TodoStore）、skill（TodoSkill）、界面（这里）。
 * 通用能力（聊天组件、agent 运行时）全在 :kit 和 :kit-agent-koog 里，这里一行都不重复实现。
 *
 * 页面外壳走 [AppShell]：状态栏/手势条/键盘/底栏避让只在壳里写一次，
 * 业务页不再自己算 inset（以前这里有一整套 `windowInsetsPadding` + 键盘判断）。
 */
@Composable
fun TodoChatPage(
    onSettingsClick: () -> Unit = {},
    composerRequest: ComposerRequest? = null,
    onComposerRequestHandled: () -> Unit = {},
) {
    val store = remember { TodoStore() }
    val approver = remember { UiApprover() }
    val session = remember(store, approver) { createSession(store, approver) }
    // 聊天记录：agent 事件 → 消息列表，映射写在 ChatLog 里（通用能力）
    val log = remember { ChatLog() }

    val todos by store.todos.collectAsState()
    val pending by approver.pending.collectAsState()
    val messages by log.messages.collectAsState()

    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    // 桌面快捷方式请求「把光标放进输入框」：先切回聊天页签，再等一帧 ——
    // 输入框还没上树时 requestFocus 会抛 IllegalStateException
    LaunchedEffect(composerRequest) {
        val request = composerRequest ?: return@LaunchedEffect
        selectedTab = 0
        if (request.draft != null) input = request.draft
        withFrameNanos { }
        focusRequester.requestFocus()
        keyboard?.show()
        // 用完就通知 Activity 清掉：否则用户去设置页再回来、页面重新入栈时又预填一次，
        // 把已经打了一半的内容盖掉
        onComposerRequestHandled()
    }

    AppShell(
        topBar = {
            Header(
                title = "待办助手",
                action1 = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = "设置")
                    }
                },
            )
        },
        bottomBar = {
            AppBottomNavigation(
                items = navItems,
                selectedIndex = selectedTab,
                onItemSelected = { selectedTab = it },
            )
        },
    ) {
        Box(
            Modifier
                .fillMaxSize()
            ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
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

                // 聊天区：气泡 + 过程折叠（工具调用折成一行，展开看详情）
                ChatMessageList(
                    messages = messages,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )

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

                // 危险工具的确认卡：agent 想删待办时，用户点"允许一次"才真删
                pending?.let { req ->
                    ToolApprovalCard(
                        tool = req.tool,
                        args = req.args,
                        risk = "删除后无法恢复",
                        onAllowOnce = { approver.resolve(ToolApproval.Allow) },
                        onAllowAlways = { approver.resolve(ToolApproval.AllowAlways) },
                        onDeny = { approver.resolve(ToolApproval.Deny) },
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        // 快捷方式进来时靠这个把光标送进来
                        modifier = Modifier.weight(1f).focusRequester(focusRequester),
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
                            log.user(text)
                            busy = true
                            status = ""
                            scope.launch {
                                s.send(text).collect { ev -> log.onEvent(ev) }
                                busy = false
                                status = ""
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp),
                    ) { Text("发送") }
                }
            }

            // 其余页签还没有真页面：盖一层占位提示；聊天页常驻 composition，切页签不丢消息
            if (selectedTab != 0) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "「${navItems[selectedTab].label}」还没实现",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * 「把光标放进输入框」的请求 —— 桌面快捷方式（或以后的深链）由 Activity 传进来。
 *
 * [seq] 是自增序号：同一个动作连点两次也要能再执行一遍，而 Compose 的 [LaunchedEffect]
 * 按 equals 比较 key，两个内容一样的请求会被当成同一个状态吞掉。
 * [draft] 预填进输入框的草稿；null = 只聚焦，不改用户已经打了一半的内容。
 */
data class ComposerRequest(val seq: Long, val draft: String?)

/**
 * UI 侧的审批闸门：只读工具直接放行，危险工具弹卡等用户点头。
 *
 * agent 的工具执行在后台协程里，所以这里用 [CompletableDeferred] 把那次调用挂起，
 * 等用户在卡上给个答案。
 */
class UiApprover : ToolApprover {

    data class Request(val tool: String, val args: String, val answer: CompletableDeferred<ToolApproval>)

    private val _pending = MutableStateFlow<Request?>(null)
    val pending: StateFlow<Request?> = _pending.asStateFlow()

    override suspend fun approve(name: String, input: JsonObject): ToolApproval {
        // ponytail: 只对"删"这类危险工具弹卡。要更细就按工具名配一张白名单表。
        if (name != TOOL_DELETE_TODO) return ToolApproval.Allow
        val answer = CompletableDeferred<ToolApproval>()
        _pending.value = Request(name, input.toString(), answer)
        return try {
            answer.await()
        } finally {
            _pending.value = null
        }
    }

    fun resolve(decision: ToolApproval) {
        _pending.value?.answer?.complete(decision)
        _pending.value = null
    }
}

/** 从构建配置建会话。没 key 就返回 null，UI 会提示怎么配。 */
private fun createSession(store: TodoStore, approver: ToolApprover): AgentSession? {
    val key = BuildConfig.ANTHROPIC_API_KEY.ifBlank { return null }
    val runtime = KoogAgentRuntime(key, BuildConfig.ANTHROPIC_BASE_URL)
    return runtime.session(todoSkill(store, BuildConfig.ANTHROPIC_MODEL), approver)
}
