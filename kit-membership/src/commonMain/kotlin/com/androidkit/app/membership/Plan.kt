package com.androidkit.app.membership

/**
 * 额度的周期。
 *
 * - [DAILY]：每天重置（"今天最多问 20 次 AI"）
 * - [TOTAL]：永不过期（"最多保存 100 个目标"）
 */
enum class QuotaPeriod { DAILY, TOTAL }

/** 一项额度的上限。 */
data class QuotaLimit(val limit: Int, val period: QuotaPeriod)

/**
 * 一个档位（也对应商店里的一个商品）。
 *
 * 档位表是**业务侧的一张表**，写在代码里（编译期可查），不从服务器下发 ——
 * 下发的话额度上限能被随便改。
 *
 * @param id 商品 id，和商店里的商品 id 对齐（"premium_monthly"）
 * @param tier 档位等级，数字越大权益越高；0 = 免费档
 * @param entitlements 权益点（功能开关），key 由业务定义（"export"、"priority_support"）
 * @param limits 额度上限表：额度 key -> 上限。key 不在表里 = 不限
 */
data class Plan(
    val id: String,
    val tier: Int,
    val entitlements: Set<String> = emptySet(),
    val limits: Map<String, QuotaLimit> = emptyMap(),
)

/**
 * 档位表 —— 业务侧一张表配出来，框架只认这张表。
 *
 * @param plans 所有档位，必须有一个 tier = 0 的免费档
 */
class PlanCatalog(plans: List<Plan>) {

    /** 所有档位，按等级从低到高。 */
    val all: List<Plan> = plans.sortedBy { it.tier }

    /** 免费档。会员过期就回退到它。 */
    val free: Plan = all.firstOrNull { it.tier == 0 }
        ?: error("PlanCatalog 里必须有一个 tier = 0 的免费档")

    /** 付费档，按等级从低到高。 */
    val paid: List<Plan> = all.filter { it.tier > 0 }

    private val index: Map<String, Plan> = all.associateBy { it.id }

    /** 按 id 找档位；找不到返回 null。 */
    fun byId(id: String): Plan? = index[id]
}
