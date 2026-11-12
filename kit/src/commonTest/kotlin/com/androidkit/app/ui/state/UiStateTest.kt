package com.androidkit.app.ui.state

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UiStateTest {

    @Test
    fun 只有成功态能取到数据() {
        assertEquals(listOf(1, 2), UiState.Success(listOf(1, 2)).dataOrNull)
        assertNull(UiState.Idle.dataOrNull)
        assertNull(UiState.Loading.dataOrNull)
        assertNull(UiState.Error("boom").dataOrNull)
    }

    @Test
    fun isLoading_只认Loading() {
        assertTrue(UiState.Loading.isLoading)
        assertTrue(!UiState.Idle.isLoading)
        assertTrue(!UiState.Success(1).isLoading)
        assertTrue(!UiState.Error("boom").isLoading)
    }

    @Test
    fun map_只映射成功值_错误原样透传() {
        val cause = IllegalStateException("boom")
        val error: UiState<Int> = UiState.Error("坏了", cause)

        assertEquals(UiState.Success("2"), UiState.Success(2).map { it.toString() })
        assertTrue(UiState.Loading.map { it.toString() } is UiState.Loading)
        assertTrue(UiState.Idle.map { it.toString() } is UiState.Idle)

        val mappedError = error.map { it.toString() }
        assertTrue(mappedError is UiState.Error)
        assertEquals("坏了", mappedError.message)
        assertEquals(cause, mappedError.cause)
    }

    @Test
    fun orElse_成功用数据_其余用兜底() {
        assertEquals(7, UiState.Success(7).orElse { 0 })
        assertEquals(0, UiState.Loading.orElse { 0 })
        assertEquals(0, UiState.Error("boom").orElse { 0 })
    }

    @Test
    fun uiStateOf_成功包成Success() = runBlocking {
        assertEquals(UiState.Success("ok"), uiStateOf { "ok" })
    }

    @Test
    fun uiStateOf_异常包成Error并保留cause() = runBlocking {
        val boom = IllegalStateException("断网了")
        val state = uiStateOf<String> { throw boom }

        assertTrue(state is UiState.Error)
        assertEquals("断网了", state.message)
        assertEquals(boom, state.cause)
    }

    /**
     * 取消不是业务错误：吞掉它，页面退出会变成「加载失败」，
     * 而且协程的取消传播会被打断。
     */
    @Test
    fun uiStateOf_取消异常必须原样抛出() = runBlocking {
        var caught: CancellationException? = null
        try {
            uiStateOf<String> { throw CancellationException("离开页面") }
        } catch (e: CancellationException) {
            caught = e
        }

        assertEquals("离开页面", caught?.message)
    }
}
