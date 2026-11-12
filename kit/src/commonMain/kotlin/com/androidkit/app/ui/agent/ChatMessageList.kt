package com.androidkit.app.ui.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.res.*
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.themedCorner
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * 聊天消息列表 —— 气泡 + 过程折叠。
 *
 * 过程（工具调用）折叠成一行是刻意的：agent 一轮里可能调五六个工具，
 * 全摊开会把正文挤没。折叠行只说"做了几件事"，点开才看得到每一步的参数和结果。
 *
 * 新的消息进来会自动滚到底（[listState] 可以传进来自己控制）。
 */
@Composable
fun ChatMessageList(
    messages: List<ChatMessage>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        items(messages) { message -> ChatMessageRow(message) }
    }
}

/** 一条消息。 */
@Composable
fun ChatMessageRow(message: ChatMessage, modifier: Modifier = Modifier) {
    when (message) {
        is ChatMessage.User -> ChatBubble(
            modifier = modifier,
            container = MaterialTheme.colorScheme.primaryContainer,
            alignEnd = true,
        ) { Text(message.text, style = MaterialTheme.typography.bodyMedium) }

        is ChatMessage.Assistant -> Column(modifier.fillMaxWidth()) {
            // 过程在前、正文在后：工具先发生，话后说
            if (message.steps.isNotEmpty()) ChatStepsFold(message.steps)
            if (message.text.isNotEmpty()) {
                ChatBubble(
                    container = MaterialTheme.colorScheme.surfaceVariant,
                    alignEnd = false,
                ) { Text(message.text, style = MaterialTheme.typography.bodyMedium) }
            }
        }

        is ChatMessage.System -> Text(
            text = message.text,
            modifier = modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        is ChatMessage.Error -> Text(
            text = "⚠️ ${message.text}",
            modifier = modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

/**
 * 过程折叠条 —— 收起时一行，展开时每步一行。
 *
 * 折叠状态记在组件里（没提到上层）：换一轮对话就该重新收起，
 * 上层不需要管这个。
 */
@Composable
fun ChatStepsFold(
    steps: List<ChatStep>,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                modifier = Modifier.padding(end = 2.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(Res.string.chat_process_count, steps.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (expanded) {
            steps.forEach { step -> ChatStepRow(step) }
        }
    }
}

/** 折叠展开后的其中一步：工具名 + 状态 + 参数 + 结果。 */
@Composable
fun ChatStepRow(step: ChatStep, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(start = 20.dp, top = 2.dp, bottom = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = step.tool,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = " · " + stringResource(step.status.labelRes()),
                style = MaterialTheme.typography.labelSmall,
                color = step.status.color(),
            )
        }
        if (step.args.isNotBlank()) {
            Text(
                text = step.args,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        step.result?.takeIf { it.isNotBlank() }?.let { result ->
            Text(
                text = result,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** 一个气泡。用户的靠右，助手的靠左。 */
@Composable
private fun ChatBubble(
    container: Color,
    alignEnd: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .background(container, themedCorner(12.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) { content() }
    }
}

/** 状态对应的文案。 */
private fun ChatStepStatus.labelRes(): StringResource = when (this) {
    ChatStepStatus.Running -> Res.string.chat_step_running
    ChatStepStatus.Done -> Res.string.chat_step_done
    ChatStepStatus.Failed -> Res.string.chat_step_failed
}

/** 状态对应的颜色：跑着的低调，完成正常，失败报错。 */
@Composable
private fun ChatStepStatus.color(): Color = when (this) {
    ChatStepStatus.Running -> MaterialTheme.colorScheme.onSurfaceVariant
    ChatStepStatus.Done -> MaterialTheme.colorScheme.primary
    ChatStepStatus.Failed -> MaterialTheme.colorScheme.error
}

@Preview(showBackground = true)
@Composable
private fun ChatMessageListPreview() {
    AppTheme {
        ChatMessageList(
            messages = listOf(
                ChatMessage.User("我今天有几件事？"),
                ChatMessage.Assistant(
                    text = "你有 3 件待办，其中 1 件今天到期。",
                    steps = listOf(
                        ChatStep("list_todos", "{}", ChatStepStatus.Done, "共 3 条"),
                        ChatStep("add_todo", """{"title":"买牛奶"}""", ChatStepStatus.Running),
                    ),
                ),
                ChatMessage.Error("网络断了"),
            ),
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
