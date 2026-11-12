package com.androidkit.app.membership

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import okio.Path
import okio.Path.Companion.toPath

/**
 * 会员 + 额度引擎的行为。
 *
 * 落在 desktopTest：要写文件，得有真实目录（commonTest 里没有 JVM 的 Files）。
 */
class MembershipManagerTest {

    private val catalog = PlanCatalog(
        listOf(
            Plan(
                id = "free",
                tier = 0,
                limits = mapOf(
                    "ai" to QuotaLimit(2, QuotaPeriod.DAILY),
                    "goals" to QuotaLimit(3, QuotaPeriod.TOTAL),
                ),
            ),
            Plan(
                id = "premium",
                tier = 1,
                entitlements = setOf("export"),
                limits = mapOf(
                    "ai" to QuotaLimit(100, QuotaPeriod.DAILY),
                    "goals" to QuotaLimit(100, QuotaPeriod.TOTAL),
                ),
            ),
        ),
    )

    /** 固定时钟，测试自己往前拨。 */
    private var now = 1_700_000_000_000L

    private fun newManager(dir: Path = tempDir()) = MembershipManager(
        catalog = catalog,
        store = MembershipStore(dir),
        usage = UsageTracker(dir),
        now = { now },
    )

    /** 相对路径落在模块的 build/ 下（Gradle 跑测试时 CWD = 模块目录）。 */
    private fun tempDir(): Path = "build/tmp/membership-test-${System.nanoTime()}".toPath()

    @Test
    fun freeTierStopsAtQuota() = runBlocking<Unit> {
        val manager = newManager()
        assertIs<Access.Allowed>(manager.check("ai"))
        assertIs<Access.Allowed>(manager.consume("ai"))
        assertIs<Access.Allowed>(manager.consume("ai"))

        val blocked = assertIs<Access.Blocked>(manager.consume("ai"))
        assertEquals(BlockReason.QUOTA_EXCEEDED, blocked.reason)
        assertEquals(2, blocked.limit)
    }

    @Test
    fun dailyQuotaResetsNextDay() = runBlocking<Unit> {
        val manager = newManager()
        manager.consume("ai")
        manager.consume("ai")
        assertIs<Access.Blocked>(manager.check("ai"))

        now += TWO_DAYS
        assertIs<Access.Allowed>(manager.check("ai"))
    }

    @Test
    fun totalQuotaNeverResets() = runBlocking<Unit> {
        val manager = newManager()
        repeat(3) { manager.consume("goals") }
        assertIs<Access.Blocked>(manager.check("goals"))

        now += TWO_DAYS
        assertIs<Access.Blocked>(manager.check("goals"))
    }

    @Test
    fun upgradeRaisesLimitAndGrantsEntitlement() = runBlocking<Unit> {
        val manager = newManager()
        repeat(2) { manager.consume("ai") }
        assertIs<Access.Blocked>(manager.check("ai"))

        manager.activate("premium")
        assertTrue(manager.has("export"))
        assertIs<Access.Allowed>(manager.check("ai"))
    }

    @Test
    fun expiredMembershipFallsBackToFree() = runBlocking<Unit> {
        val manager = newManager()
        manager.activate("premium", expiresAt = now + 1_000)
        assertTrue(manager.has("export"))

        now += 2_000
        assertEquals("free", manager.plan.id)
        assertIs<Access.Blocked>(manager.checkEntitlement("export"))
    }

    @Test
    fun unverifiedPurchaseDoesNotGrantMembership() = runBlocking<Unit> {
        val manager = newManager()
        val result = manager.purchase(
            FakeProvider(PurchaseResult.Success("premium", verified = false)),
            "premium",
        )

        assertIs<PurchaseResult.Failed>(result)
        assertEquals("free", manager.plan.id)
    }

    @Test
    fun verifiedPurchaseGrantsMembership() = runBlocking<Unit> {
        val manager = newManager()
        val result = manager.purchase(
            FakeProvider(PurchaseResult.Success("premium", expiresAt = now + 100_000, verified = true)),
            "premium",
        )

        assertIs<PurchaseResult.Success>(result)
        assertEquals("premium", manager.plan.id)
    }

    @Test
    fun membershipSurvivesRestart() = runBlocking<Unit> {
        val dir = tempDir()
        newManager(dir).activate("premium")

        val reopened = newManager(dir)
        reopened.refresh()
        assertEquals("premium", reopened.plan.id)
    }

    private class FakeProvider(private val result: PurchaseResult) : BillingProvider {
        override suspend fun products(): List<Product> = listOf(Product("premium", "高级版", "¥28 / 月"))

        override suspend fun purchase(productId: String): PurchaseResult = result

        override suspend fun restorePurchases(): PurchaseResult = result
    }

    private companion object {
        const val TWO_DAYS = 2 * 24 * 60 * 60 * 1000L
    }
}
