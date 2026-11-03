package com.androidkit.app.chat

import ai.koog.serialization.typeToken
import com.androidkit.app.data.agent.AgentEvent
import com.androidkit.app.data.agent.AgentSession
import com.androidkit.app.data.agent.KoogAgentRuntime
import com.androidkit.app.data.agent.SkillDefinition
import com.androidkit.app.data.agent.SkillTool
import com.androidkit.app.data.agent.ToolApprover
import com.androidkit.app.ui.theme.AppTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.time.LocalDateTime

/**
 * 桌面对话 demo：验证「前端 + skill + Koog」这条链路能不能跑通。
 *
 * 配置全走环境变量：ANTHROPIC_API_KEY（或 ANTHROPIC_AUTH_TOKEN）、ANTHROPIC_BASE_URL、ANTHROPIC_MODEL。
 */
fun main(args: Array<String>) {
    if (args.contains("--check")) {
        check()
        return
    }
    application {
        Window(onCloseRequest = ::exitApplication, title = "Chat demo") {
            AppTheme { ChatScreen() }
        }
    }
}

/** 无界面自检：跑一轮对话，把事件打出来。 */
private fun check() {
    val session = createSession() ?: error("没找到 API key：设置 ANTHROPIC_API_KEY 或 ANTHROPIC_AUTH_TOKEN")
    kotlinx.coroutines.runBlocking {
        session.send("现在几点？").collect { println(it) }
    }
}

private data class Msg(val fromUser: Boolean, val text: String)

@Composable
private fun ChatScreen() {
    val session = remember { createSession() }
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
        Text("Chat demo", style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(messages) { m ->
                val mine = m.fromUser
                Text(
                    text = m.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(10.dp),
                )
            }
        }
        if (session == null) {
            Text(
                "没找到 API key：设置 ANTHROPIC_API_KEY（或 ANTHROPIC_AUTH_TOKEN）后重启窗口",
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
                placeholder = { Text("说点什么…") },
                enabled = !busy,
                maxLines = 4,
            )
            Button(
                enabled = !busy && input.isNotBlank() && session != null,
                onClick = {
                    val text = input.trim()
                    input = ""
                    messages += Msg(true, text)
                    busy = true
                    status = ""
                    scope.launch {
                        var bubble = -1   // 当前气泡下标；-1 = 还没开始
                        session!!.send(text).collect { ev ->
                            when (ev) {
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
}

/** 从环境变量建会话。 */
private fun createSession(): AgentSession? {
    val key = env("ANTHROPIC_API_KEY") ?: env("ANTHROPIC_AUTH_TOKEN") ?: return null
    val baseUrl = env("ANTHROPIC_BASE_URL") ?: "https://api.anthropic.com"
    val model = env("ANTHROPIC_MODEL")
        ?: env("ANTHROPIC_DEFAULT_SONNET_MODEL")
        ?: "claude-opus-5-5"
    val skill = SkillDefinition(
        id = "chat_demo",
        name = "对话演示",
        description = "验证 agent 对话链路",
        systemPrompt = "你是一个用于验证 agent 框架的对话助手。回答简短。需要当前时间时调用 now 工具。",
        tools = listOf(NowTool()),
        model = model,
    )
    return KoogAgentRuntime(key, baseUrl).session(skill, ToolApprover.AllowAll)
}

private fun env(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

@Serializable
data class NowArgs(val note: String = "")

/** 演示工具：让模型能问当前时间，顺带验证工具调用链路。 */
class NowTool : SkillTool<NowArgs>(typeToken<NowArgs>(), "now", "获取当前时间") {
    override suspend fun doExecute(args: NowArgs): String = LocalDateTime.now().toString()
}
