package com.androidkit.app.membership.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.membership.res.*
import com.androidkit.app.ui.components.ConfirmDialog
import com.androidkit.app.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * 额度用完时的付费墙 —— 只说明「为什么被挡住」并把人送去升级页，档位对比不在这里重复。
 *
 * @param message 被挡住的原因（业务给，文案跟着业务走）
 */
@Composable
fun MembershipPaywall(
    message: String,
    onUpgradeClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ConfirmDialog(
        title = stringResource(Res.string.membership_paywall_title),
        message = message,
        confirmText = stringResource(Res.string.membership_paywall_upgrade),
        onConfirm = onUpgradeClick,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun MembershipPaywallPreview() {
    AppTheme {
        MembershipPaywall(
            message = "今天的 AI 次数已用完（当前档位每天 5 次），升级后可用更多次数。",
            onUpgradeClick = {},
            onDismiss = {}, // ponytail: demo data only
        )
    }
}
