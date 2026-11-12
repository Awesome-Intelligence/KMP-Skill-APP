package com.androidkit.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme

/** 触发付费墙的额度类型 */
enum class QuotaReason { GOAL, CONTACT, AI }

/**
 * 额度用完时的付费墙。
 *
 * 只说明"为什么被挡住"并把人送去高级版页 —— 档位对比在高级版页里展示，这里不重复。
 */
@Composable
fun QuotaPaywall(
    reason: QuotaReason,
    limit: Int,
    onUpgradeClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val message = when (reason) {
        QuotaReason.GOAL -> stringResource(Res.string.quota_goal_message, limit)
        QuotaReason.CONTACT -> stringResource(Res.string.quota_contact_message, limit)
        QuotaReason.AI -> stringResource(Res.string.quota_ai_message, limit)
    }

    ConfirmDialog(
        title = stringResource(Res.string.quota_paywall_title),
        message = message,
        confirmText = stringResource(Res.string.quota_paywall_upgrade),
        onConfirm = onUpgradeClick,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
internal fun QuotaPaywallPreview() {
    AppTheme {
        QuotaPaywall(
            reason = QuotaReason.AI,
            limit = DemoData.Value.INT_TEN,
            onUpgradeClick = {},
            onDismiss = {} // ponytail: demo data only
        )
    }
}
