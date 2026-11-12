package com.androidkit.app.data.update

/**
 * 检查状态的持久化抽象。
 *
 * 抽成接口是为了测试：[UpdateChecker] 的节流/跳过逻辑不该依赖真实文件。
 * 生产用 [FileUpdateStateStore]（okio，三端通用）。
 */
interface UpdateStateStore {
    suspend fun load(): UpdateState
    suspend fun save(state: UpdateState)
}
