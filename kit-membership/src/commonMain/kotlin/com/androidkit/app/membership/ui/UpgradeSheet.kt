package com.androidkit.app.membership.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.membership.Plan
import com.androidkit.app.membership.QuotaLimit
import com.androidkit.app.membership.QuotaPeriod
import com.androidkit.app.membership.res.*
import com.androidkit.app.ui.components.AppCard
import com.androidkit.app.ui.components.PrimaryButton
import com.androidkit.app.ui.components.TextActionButton
import com.androidkit.app.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * 升级页：档位对比 + 购买 + 恢复购买。
 *
 * 只管排版 —— 档位名、价格、权益文案由业务给的 [PlanOption] 决定；
 * 买了哪一单由 [onPurchase] 回调出去（框架不直接碰支付通道）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeSheet(
    options: List<PlanOption>,
    /** 当前生效的档位 id；等于某个 option 时那个档位显示「当前方案」。 */
    currentPlanId: String?,
    onPurchase: (String) -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            options.forEach { option ->
                PlanCard(
                    option = option,
                    isCurrent = option.plan.id == currentPlanId,
                    onPurchase = { onPurchase(option.plan.id) },
                )
            }
            TextActionButton(
                text = stringResource(Res.string.membership_restore),
                onClick = onRestore,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** 一个档位的卡片。 */
@Composable
private fun PlanCard(
    option: PlanOption,
    isCurrent: Boolean,
    onPurchase: () -> Unit,
) {
    AppCard {
        Text(option.title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = option.price,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        option.features.forEach { feature ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (feature.value.isEmpty()) feature.label else "${feature.label}：${feature.value}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = stringResource(
                if (isCurrent) Res.string.membership_current_plan else Res.string.membership_purchase,
            ),
            onClick = onPurchase,
            enabled = !isCurrent,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanCardPreview() {
    AppTheme {
        PlanCard(
            option = PlanOption(
                plan = Plan(
                    id = "premium",
                    tier = 1,
                    entitlements = setOf("export"),
                    limits = mapOf("ai" to QuotaLimit(20, QuotaPeriod.DAILY)),
                ),
                title = "高级版",
                price = "¥28 / 月",
                features = listOf(
                    PlanFeature("导出数据"),
                    PlanFeature("每天 AI 次数", "20"),
                ),
            ),
            isCurrent = false,
            onPurchase = {},
        )
    }
}
