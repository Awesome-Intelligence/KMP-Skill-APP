package com.androidkit.app.membership

import kotlinx.serialization.Serializable

/**
 * 会员状态（本地持久化的那点东西）。
 *
 * 权益和额度**不在这里** —— 那些在 [PlanCatalog] 的档位表里；
 * 状态只记「当前是哪个档、到什么时候为止、这笔买没验过签」。
 */
@Serializable
data class Membership(
    /** 当前生效的档位 id。 */
    val planId: String,
    /** 到期时间（毫秒时间戳）；null = 不过期（免费档 / 买断）。 */
    val expiresAt: Long? = null,
    /** 购买结果是否通过校验（回执验签 / 跟服务端核对过）。没校验过的购买不给开会员。 */
    val verified: Boolean = false,
) {
    /** 到期前都算有效。 */
    fun isActive(now: Long): Boolean = expiresAt == null || expiresAt > now

    companion object {
        /** 免费档的初始状态。 */
        fun free(planId: String = "free"): Membership =
            Membership(planId = planId, expiresAt = null, verified = true)
    }
}
