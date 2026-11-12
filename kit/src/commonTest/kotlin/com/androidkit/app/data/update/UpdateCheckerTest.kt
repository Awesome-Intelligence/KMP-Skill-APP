package com.androidkit.app.data.update

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UpdateCheckerTest {

    private class FakeSource(private val onFetch: () -> UpdateManifest) : UpdateSource {
        override suspend fun fetch(): UpdateManifest = onFetch()
    }

    private class FakeStore(var state: UpdateState = UpdateState()) : UpdateStateStore {
        override suspend fun load(): UpdateState = state
        override suspend fun save(state: UpdateState) {
            this.state = state
        }
    }

    private val now = 1_800_000_000_000L

    private fun checker(
        source: UpdateSource,
        store: FakeStore,
        nowMillis: () -> Long = { now },
    ) = UpdateChecker(source = source, stateStore = store, nowMillis = nowMillis)

    private fun manifest(
        latest: String,
        minimum: String? = null,
    ) = UpdateManifest(
        latestVersion = latest,
        minSupportedVersion = minimum,
        downloadUrl = "https://example.com/app.apk",
        releaseNotes = "更新说明",
    )

    @Test
    fun 远端版本更高时返回可更新(): Unit = runBlocking {
        val store = FakeStore()
        val result = checker(FakeSource { manifest("1.3.0") }, store).check("1.2.0")

        val available = assertIs<UpdateResult.Available>(result)
        assertEquals("1.3.0", available.manifest.latestVersion)
        assertEquals(false, available.mandatory)
        assertEquals(now, store.state.lastCheckedAtMillis)
    }

    @Test
    fun 版本相同时返回已是最新(): Unit = runBlocking {
        val result = checker(FakeSource { manifest("1.2.0") }, FakeStore()).check("1.2.0")

        assertIs<UpdateResult.UpToDate>(result)
    }

    @Test
    fun 远端版本更低时也算已是最新(): Unit = runBlocking {
        val result = checker(FakeSource { manifest("1.1.0") }, FakeStore()).check("1.2.0")

        assertIs<UpdateResult.UpToDate>(result)
    }

    @Test
    fun 低于最低支持版本时强制更新(): Unit = runBlocking {
        val result = checker(FakeSource { manifest("2.0.0", minimum = "1.5.0") }, FakeStore()).check("1.2.0")

        val available = assertIs<UpdateResult.Available>(result)
        assertTrue(available.mandatory)
    }

    @Test
    fun 已跳过的版本不再自动提示(): Unit = runBlocking {
        val store = FakeStore(UpdateState(skippedVersion = "1.3.0"))
        val result = checker(FakeSource { manifest("1.3.0") }, store).check("1.2.0")

        assertIs<UpdateResult.Skipped>(result)
    }

    @Test
    fun 出现更新的版本时跳过记录失效(): Unit = runBlocking {
        val store = FakeStore(UpdateState(skippedVersion = "1.3.0"))
        val result = checker(FakeSource { manifest("1.4.0") }, store).check("1.2.0")

        assertIs<UpdateResult.Available>(result)
    }

    @Test
    fun 强制更新不受跳过记录影响(): Unit = runBlocking {
        val store = FakeStore(UpdateState(skippedVersion = "2.0.0"))
        val result = checker(
            FakeSource { manifest("2.0.0", minimum = "1.5.0") },
            store,
        ).check("1.2.0")

        val available = assertIs<UpdateResult.Available>(result)
        assertTrue(available.mandatory)
    }

    @Test
    fun 间隔内不重复检查(): Unit = runBlocking {
        val store = FakeStore(UpdateState(lastCheckedAtMillis = now - 60 * 60 * 1000L))
        val source = FakeSource { error("节流命中时不该联网") }

        assertIs<UpdateResult.Throttled>(checker(source, store).check("1.2.0"))
    }

    @Test
    fun 系统时间回拨时不会永久节流(): Unit = runBlocking {
        val store = FakeStore(UpdateState(lastCheckedAtMillis = now + 24 * 60 * 60 * 1000L))

        val result = checker(FakeSource { manifest("1.3.0") }, store).check("1.2.0")

        assertIs<UpdateResult.Available>(result)
    }

    @Test
    fun 手动检查绕过节流和跳过记录(): Unit = runBlocking {
        val store = FakeStore(
            UpdateState(
                lastCheckedAtMillis = now - 60 * 60 * 1000L,
                skippedVersion = "1.3.0",
            )
        )

        val result = checker(FakeSource { manifest("1.3.0") }, store).check("1.2.0", force = true)

        assertIs<UpdateResult.Available>(result)
    }

    @Test
    fun 拉取失败返回错误并记录检查时间(): Unit = runBlocking {
        val store = FakeStore()
        val source = FakeSource { error("网断了") }

        val result = checker(source, store).check("1.2.0")

        val failed = assertIs<UpdateResult.Failed>(result)
        assertTrue(failed.message.contains("网断了"))
        assertEquals(now, store.state.lastCheckedAtMillis)
    }

    @Test
    fun 当前版本号非法时返回错误(): Unit = runBlocking {
        val source = FakeSource { error("不该联网") }

        assertIs<UpdateResult.Failed>(checker(source, FakeStore()).check("不是版本号"))
    }

    @Test
    fun 远端版本号非法时返回错误(): Unit = runBlocking {
        val result = checker(FakeSource { manifest("latest") }, FakeStore()).check("1.2.0")

        assertIs<UpdateResult.Failed>(result)
    }

    @Test
    fun 跳过操作会落盘(): Unit = runBlocking {
        val store = FakeStore()
        val checker = checker(FakeSource { manifest("1.3.0") }, store)

        checker.skip("1.3.0")

        assertEquals("1.3.0", store.state.skippedVersion)
        assertIs<UpdateResult.Skipped>(checker.check("1.2.0"))
    }
}
