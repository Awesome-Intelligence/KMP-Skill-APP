package com.androidkit.app.ui.agent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.res.*
import com.androidkit.app.ui.components.SmallSecondaryButton
import com.androidkit.app.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * 工具审批卡 —— agent 想干危险的事时弹给用户看的那张卡。
 *
 * 为什么是「卡」不是系统对话框：卡能显示**参数**（用户要看得见 agent 到底要动什么），
 * 而且能挂在聊天流里，不打断上下文。对话框只能塞一段字。
 *
 * 三个动作对应 [com.androidkit.app.data.agent.ToolApproval] 的三种结果：
 * 允许一次 / 本任务免确认（以后这个工具不再问）/ 拒绝。
 *
 * 这是纯展示组件：谁来弹、点了之后做什么，是 app 的事。
 */
@Composable
fun ToolApprovalCard(
    tool: String,
    args: String,
    onAllowOnce: () -> Unit,
    onAllowAlways: () -> Unit,
    onDeny: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(Res.string.chat_approval_title),
    risk: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            Text(
                text = tool,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )

            if (args.isNotBlank()) {
                Text(
                    text = args,
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            risk?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                SmallSecondaryButton(text = stringResource(Res.string.chat_approval_deny), onClick = onDeny)
                SmallSecondaryButton(
                    text = stringResource(Res.string.chat_approval_allow_always),
                    onClick = onAllowAlways,
                )
                SmallSecondaryButton(text = stringResource(Res.string.chat_approval_allow_once), onClick = onAllowOnce)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun ToolApprovalCardPreview() {
    AppTheme {
        ToolApprovalCard(
            tool = "delete_todo",
            args = """{"id":"42"}""",
            risk = "删除后无法恢复",
            onAllowOnce = {},
            onAllowAlways = {},
            onDeny = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}
