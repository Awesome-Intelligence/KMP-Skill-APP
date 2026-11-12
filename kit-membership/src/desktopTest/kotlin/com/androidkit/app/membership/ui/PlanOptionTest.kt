package com.androidkit.app.membership.ui

import com.androidkit.app.membership.BillingProvider
import com.androidkit.app.membership.Product
import com.androidkit.app.membership.Plan
import com.androidkit.app.membership.PlanCatalog
import com.androidkit.app.membership.PurchaseResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/** 升级页的数据来源：商店商品 × 档位表。 */
class PlanOptionTest {

    private val catalog = PlanCatalog(
        listOf(
            Plan("free", 0),
            Plan("premium", 1),
            Plan("legacy", 2),
        ),
    )

    @Test
    fun onlyPlansWithAStoreProductShowUp() {
        val options = runBlocking { FakeProvider().planOptions(catalog) }

        assertEquals(listOf("premium"), options.map { it.plan.id })
        assertEquals("¥28 / 月", options.first().price)
    }

    @Test
    fun featuresComeFromThePlan() {
        val options = runBlocking {
            FakeProvider().planOptions(catalog) { plan ->
                listOf(PlanFeature("档位 ${plan.tier}"))
            }
        }

        assertEquals(listOf(PlanFeature("档位 1")), options.first().features)
    }

    private class FakeProvider : BillingProvider {
        override suspend fun products(): List<Product> =
            listOf(Product("premium", "高级版", "¥28 / 月"))

        override suspend fun purchase(productId: String): PurchaseResult =
            PurchaseResult.Failed("测试不真的买")

        override suspend fun restorePurchases(): PurchaseResult =
            PurchaseResult.Failed("测试不真的恢复")
    }
}
