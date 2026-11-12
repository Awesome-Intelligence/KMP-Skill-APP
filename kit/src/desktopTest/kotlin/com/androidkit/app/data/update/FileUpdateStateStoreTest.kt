package com.androidkit.app.data.update

import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 检查状态落盘的真跑一遍 —— 节流和“跳过此版本”都靠它，
 * 写坏了的表现是「每次启动都弹更新」或者「跳过永远不生效」。
 */
class FileUpdateStateStoreTest {

    private fun tempDir() = Files.createTempDirectory("update-state-test").toFile().absolutePath.toPath()

    @Test
    fun 没有文件时返回默认状态() = runBlocking {
        assertEquals(UpdateState(), FileUpdateStateStore(tempDir()).load())
    }

    @Test
    fun 写入后能读回来() = runBlocking {
        val dir = tempDir()
        val state = UpdateState(lastCheckedAtMillis = 1_800_000_000_000L, skippedVersion = "1.4.0")
        FileUpdateStateStore(dir).save(state)

        assertEquals(state, FileUpdateStateStore(dir).load())
    }

    @Test
    fun 文件损坏时退回默认状态() = runBlocking {
        val dir = tempDir()
        val store = FileUpdateStateStore(dir)
        store.save(UpdateState())
        okio.FileSystem.SYSTEM.write(dir / "update-state.json") { writeUtf8("not json at all") }

        assertEquals(UpdateState(), store.load())
    }
}
