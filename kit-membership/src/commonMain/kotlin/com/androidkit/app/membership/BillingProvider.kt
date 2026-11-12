package com.androidkit.app.membership

/** 商店里的一个商品（订阅或买断）。 */
data class Product(
    val id: String,
    val title: String,
    /** 本地化价格文案（"¥28 / 月"）—— 价格用商店给的，别自己算。 */
    val price: String,
    val description: String = "",
)

/** 一次购买的结果。 */
sealed interface PurchaseResult {

    /** 买成功。[verified] 为 true 表示回执已验签 / 已跟服务端核对过。 */
    data class Success(
        val productId: String,
        val expiresAt: Long? = null,
        val verified: Boolean = false,
    ) : PurchaseResult

    /** 用户取消。 */
    data object Cancelled : PurchaseResult

    /** 还在处理中（家长确认、3DS 之类），稍后再查。 */
    data object Pending : PurchaseResult

    /** 失败。[reason] 是给日志看的原因。 */
    data class Failed(val reason: String) : PurchaseResult
}

/**
 * 平台支付通道 —— **各端 app 自己实现**（Android Play Billing / iOS StoreKit / 桌面的假实现）。
 *
 * 框架不碰平台 SDK，也不碰回执：
 * [PurchaseResult.Success.verified] 由实现方决定，没验过签的结果不给开会员
 * （见 [MembershipManager.purchase]）。
 */
interface BillingProvider {

    /** 当前可买的商品（价格文案用商店给的）。 */
    suspend fun products(): List<Product>

    /** 发起购买。 */
    suspend fun purchase(productId: String): PurchaseResult

    /** 恢复购买（换设备 / 重装后用）。 */
    suspend fun restorePurchases(): PurchaseResult
}
