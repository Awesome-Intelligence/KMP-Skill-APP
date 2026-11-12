package com.androidkit.app.membership

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock

/** 能不能用：放行 / 被挡住。 */
sealed interface Access {

    /** 放行。 */
    data object Allowed : Access

    /**
     * 被挡住。
     *
     * @param key 被挡的权益点或额度 key
     * @param limit 这一档的上限（无上限的权益点为 -1）
     * @param reason 挡住的原因
     */
    data class Blocked(
        val key: String,
        val limit: Int,
        val reason: BlockReason,
    ) : Access
}

/** 挡住的原因。 */
enum class BlockReason { NO_ENTITLEMENT, QUOTA_EXCEEDED }

/**
 * 会员 + 额度的门面 —— 业务代码只跟这一层打交道。
 *
 * 规矩：
 * - 会员资格只认**已验签**的状态（[Membership.verified]）
 * - 过期 / 没验签 / 找不到档位 → 一律按免费档算
 * - 额度用 [consume] 扣，别自己加（扣了才知道超没超）
 */
class MembershipManager(
    private val catalog: PlanCatalog,
    private val store: MembershipStore,
    private val usage: UsageTracker,
    /** 时钟（测试可以注入假的）。 */
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {

    private val _membership = MutableStateFlow(Membership.free())

    /** 当前会员状态（UI 可以直接 collect）。 */
    val membership: StateFlow<Membership> = _membership.asStateFlow()

    /** 当前生效的档位（过期就回免费档）。 */
    val plan: Plan get() = effectivePlan(_membership.value)

    /** 读本地状态（app 启动时调一次）。 */
    suspend fun refresh() {
        _membership.value = store.load()
    }

    /** 有没有这个权益点。 */
    fun has(entitlement: String): Boolean = plan.entitlements.contains(entitlement)

    /** 要某个权益点：没有就返回 [Access.Blocked]。 */
    fun checkEntitlement(entitlement: String): Access =
        if (has(entitlement)) Access.Allowed
        else Access.Blocked(entitlement, NO_LIMIT, BlockReason.NO_ENTITLEMENT)

    /** 查额度：没超放行，超了返回 [Access.Blocked]。 */
    suspend fun check(key: String): Access {
        val limit = plan.limits[key] ?: return Access.Allowed
        val used = usage.used(key, limit.period, now())
        return if (used >= limit.limit) {
            Access.Blocked(key, limit.limit, BlockReason.QUOTA_EXCEEDED)
        } else {
            Access.Allowed
        }
    }

    /** 扣一次额度：没超就扣并放行，超了不动计数。 */
    suspend fun consume(key: String, amount: Int = 1): Access {
        val access = check(key)
        if (access is Access.Blocked) return access
        val limit = plan.limits[key]
        if (limit != null) usage.consume(key, limit.period, amount, now())
        return Access.Allowed
    }

    /** 直接设置会员状态（试用码、运营发码、支付成功后都用它）。 */
    suspend fun activate(planId: String, expiresAt: Long? = null, verified: Boolean = true) {
        val next = Membership(planId = planId, expiresAt = expiresAt, verified = verified)
        store.save(next)
        _membership.value = next
    }

    /** 走通道买一单：只有「成功且验过签」才开会员。 */
    suspend fun purchase(provider: BillingProvider, productId: String): PurchaseResult =
        handle(provider.purchase(productId))

    /** 恢复购买：同样只有验过签才开会员。 */
    suspend fun restore(provider: BillingProvider): PurchaseResult =
        handle(provider.restorePurchases())

    private suspend fun handle(result: PurchaseResult): PurchaseResult = when (result) {
        is PurchaseResult.Success ->
            if (result.verified) {
                activate(result.productId, result.expiresAt)
                result
            } else {
                PurchaseResult.Failed("购买结果未通过校验")
            }
        else -> result
    }

    private fun effectivePlan(membership: Membership): Plan {
        val plan = catalog.byId(membership.planId) ?: return catalog.free
        if (!membership.isActive(now())) return catalog.free
        // 付费档必须验过签，否则按免费算
        if (plan.tier > 0 && !membership.verified) return catalog.free
        return plan
    }

    private companion object {
        /** 无上限的权益点用这个占位。 */
        const val NO_LIMIT = -1
    }
}
