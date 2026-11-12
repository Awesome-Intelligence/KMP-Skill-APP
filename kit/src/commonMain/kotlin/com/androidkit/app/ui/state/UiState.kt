package com.androidkit.app.ui.state

import kotlinx.coroutines.CancellationException

/**
 * 一次异步加载的 UI 状态 —— **底座只认这一套**。
 *
 * 为什么要有它：模板会派生很多 app，如果每个页面各写一个 `XxxUiState`
 * （`isLoading` / `data` / `error` 三个字段自己拼），分支处理和空态早晚分叉。
 * 这里给一个四态密封接口，页面 `when` 一下就能穷举。
 *
 * ```kotlin
 * val state = remember { mutableStateOf<UiState<List<Todo>>>(UiState.Loading) }
 *
 * when (val s = state.value) {
 *     UiState.Idle, UiState.Loading -> LoadingSkeleton()
 *     is UiState.Error -> ErrorState(message = s.message, onRetry = ::reload)
 *     is UiState.Success -> TodoList(s.data)
 * }
 * ```
 *
 * 它是**纯 UI 状态**，不携带仓库/网络类型；放在 data 层的加载结果由页面映射成它。
 * 和 [com.androidkit.app.ui.preview.DemoData] 一样，属于「组件怎么摆」的约定，不是业务模型。
 */
sealed interface UiState<out T> {

    /** 还没开始加载（首帧、或用户还没触发）。和 [Loading] 分开，是为了区分「空屏」和「转圈」。 */
    data object Idle : UiState<Nothing>

    /** 正在加载。 */
    data object Loading : UiState<Nothing>

    /** 成功，[data] 是这次的结果（可以为空集合，空集合不是 [Error]）。 */
    data class Success<T>(val data: T) : UiState<T>

    /**
     * 失败。[message] 是给用户看的一句话；[cause] 留着排查（日志/上报），别直接展示。
     */
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>
}

/** 成功时的数据；其余状态都是 null。 */
val <T> UiState<T>.dataOrNull: T?
    get() = (this as? UiState.Success)?.data

/** 是否正在加载（[UiState.Idle] 不算）。 */
val UiState<*>.isLoading: Boolean
    get() = this is UiState.Loading

/** 只映射成功值，其余状态原样透传 —— 用来把 `UiState<Entity>` 变成 `UiState<UiModel>`。 */
inline fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R> = when (this) {
    UiState.Idle -> UiState.Idle
    UiState.Loading -> UiState.Loading
    is UiState.Success -> UiState.Success(transform(data))
    is UiState.Error -> this
}

/** 成功拿数据，其余状态用 [fallback]（列表页常用：加载中先给空列表）。 */
inline fun <T> UiState<T>.orElse(fallback: () -> T): T = dataOrNull ?: fallback()

/**
 * 把一次挂起调用包成 [UiState]。
 *
 * [CancellationException] 必须原样抛出 —— 它是协程取消的信号，不是业务错误；
 * 吞掉会让「离开页面」变成「加载失败」。
 *
 * 文案兜底交给调用方：`Error.message` 可能为空，UI 用 `Res.string` 补一句更合适。
 */
suspend fun <T> uiStateOf(block: suspend () -> T): UiState<T> = try {
    UiState.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    UiState.Error(message = e.message.orEmpty(), cause = e)
}
