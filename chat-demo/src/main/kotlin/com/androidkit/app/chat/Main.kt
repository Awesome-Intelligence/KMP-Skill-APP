package com.androidkit.app.chat

import ai.koog.serialization.typeToken
import com.androidkit.app.data.agent.AgentSession
import com.androidkit.app.data.agent.ChatLog
import com.androidkit.app.data.agent.FakeAgentRuntime
import com.androidkit.app.data.agent.FakeReply
import com.androidkit.app.data.agent.KoogAgentRuntime
import com.androidkit.app.data.agent.SkillDefinition
import com.androidkit.app.data.agent.SkillTool
import com.androidkit.app.data.agent.ToolApprover
import com.androidkit.app.data.agent.ToolOutput
import com.androidkit.app.ui.agent.ChatMessageList
import com.androidkit.app.ui.theme.AppTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
 * 加 `--fake` 走本地假运行时：不联网、不花钱、不需要 key，用来验证 UI 和事件流。
 */
fun main(args: Array<String>) {
    val fake = args.contains("--fake")
    if (args.contains("--check")) {
        check(fake)
        return
    }
    application {
        Window(onCloseRequest = ::exitApplication, title = "Chat demo") {
            AppTheme { ChatScreen(fake) }
        }
    }
}

/** 无界面自检：跑一轮对话，把事件打出来。`--fake` 时不需要 key。 */
private fun check(fake: Boolean) {
    val session = createSession(fake)
        ?: error("没找到 API key：设置 ANTHROPIC_API_KEY 或 ANTHROPIC_AUTH_TOKEN，或加 --fake")
    kotlinx.coroutines.runBlocking {
        session.send("现在几点？").collect { println(it) }
    }
}

@Composable
private fun ChatScreen(fake: Boolean) {
    val session = remember(fake) { createSession(fake) }
    val log = remember { ChatLog() }
    val messages by log.messages.collectAsState()
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Chat demo", style = MaterialTheme.typography.titleMedium)
        // 聊天组件直接用 :kit 的 —— app 里长什么样，这里就长什么样
        ChatMessageList(
            messages = messages,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp),
        )
        if (session == null) {
            Text(
                "没找到 API key：设置 ANTHROPIC_API_KEY（或 ANTHROPIC_AUTH_TOKEN）后重启窗口，或加 --fake 走假运行时",
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
                    log.user(text)
                    busy = true
                    status = ""
                    scope.launch {
                        session!!.send(text).collect { ev -> log.onEvent(ev) }
                        busy = false
                        status = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp),
            ) { Text("发送") }
        }
    }
}

/** 从环境变量建会话；`fake = true` 时用本地假运行时（不联网、不需要 key）。 */
private fun createSession(fake: Boolean): AgentSession? {
    if (fake) {
        return FakeAgentRuntime(
            replies = listOf(
                FakeReply.Text("（假运行时）我在。这一轮没有联网，也没有花 token。", chunkSize = 6),
                FakeReply.ToolCall(
                    name = "now",
                    then = "（假运行时）工具调用事件已经发给你了，但工具本身不会执行。",
                ),
            ),
        ).session(demoSkill(), ToolApprover.AllowAll)
    }

    val key = env("ANTHROPIC_API_KEY") ?: env("ANTHROPIC_AUTH_TOKEN") ?: return null
    val baseUrl = env("ANTHROPIC_BASE_URL") ?: "https://api.anthropic.com"
    val model = env("ANTHROPIC_MODEL")
        ?: env("ANTHROPIC_DEFAULT_SONNET_MODEL")
        ?: "claude-opus-5-5"
    return KoogAgentRuntime(key, baseUrl).session(demoSkill(model), ToolApprover.AllowAll)
}

private fun demoSkill(model: String = "claude-opus-5-5"): SkillDefinition = SkillDefinition(
    id = "chat_demo",
    name = "对话演示",
    description = "验证 agent 对话链路",
    systemPrompt = "你是一个用于验证 agent 框架的对话助手。回答简短。需要当前时间时调用 now 工具。",
    tools = listOf(NowTool()),
    model = model,
)

private fun env(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

@Serializable
data class NowArgs(val note: String = "")

/** 演示工具：让模型能问当前时间，顺带验证工具调用链路。 */
class NowTool : SkillTool<NowArgs>(typeToken<NowArgs>(), "now", "获取当前时间") {
    override suspend fun doExecute(args: NowArgs): ToolOutput = ToolOutput.Text(LocalDateTime.now().toString())
}
