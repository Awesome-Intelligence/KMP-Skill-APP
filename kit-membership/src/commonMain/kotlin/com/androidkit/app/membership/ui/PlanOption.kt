package com.androidkit.app.membership.ui

import com.androidkit.app.membership.BillingProvider
import com.androidkit.app.membership.Plan
import com.androidkit.app.membership.PlanCatalog
import com.androidkit.app.membership.QuotaLimit

/** 可展示的一行权益/额度说明 —— 文案由业务给，框架只管排版。 */
data class PlanFeature(
    val label: String,
    val value: String = "",
)

/** 一个档位的展示形态。 */
data class PlanOption(
    val plan: Plan,
    val title: String,
    /** 价格文案（"¥28 / 月"），用商店给的。 */
    val price: String,
    val features: List<PlanFeature> = emptyList(),
)

/**
 * 把档位表翻成可展示的条目。
 *
 * @param entitlementLabels 权益 key -> 显示名（没给的 key 直接显示 key）
 * @param quotaLabel 额度 key -> 文案（框架把上限传进去，排版由业务决定）
 */
fun Plan.toFeatures(
    entitlementLabels: Map<String, String> = emptyMap(),
    quotaLabel: (key: String, limit: QuotaLimit) -> String = { key, limit -> "$key: ${limit.limit}" },
): List<PlanFeature> =
    entitlements.sorted().map { PlanFeature(entitlementLabels[it] ?: it) } +
        limits.toSortedMap().map { (key, limit) -> PlanFeature(quotaLabel(key, limit)) }

/**
 * 把商店商品和档位表对上 —— 升级页直接用这个列表。
 *
 * 只有**在商店里买得到**的付费档才出现（免费档没有商品）；
 * 标题和价格用商店给的（本地化过的），别自己拼。
 */
suspend fun BillingProvider.planOptions(
    catalog: PlanCatalog,
    features: (Plan) -> List<PlanFeature> = { it.toFeatures() },
): List<PlanOption> {
    val products = products().associateBy { it.id }
    return catalog.paid.mapNotNull { plan ->
        val product = products[plan.id] ?: return@mapNotNull null
        PlanOption(
            plan = plan,
            title = product.title,
            price = product.price,
            features = features(plan),
        )
    }
}
