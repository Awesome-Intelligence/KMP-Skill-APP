package com.androidkit.app.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.ui.components.AppBottomNavigation
import com.androidkit.app.ui.components.Header
import com.androidkit.app.ui.components.PrimaryButton
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme

/**
 * 页面外壳 —— **每个 app 的每个页面都该套它**。
 *
 * 它只干四件容易被每页重复踩的事：
 * 1. 系统栏避让：顶部/左右用 `safeDrawing`，底部按「键盘 > 底栏 > 手势条」的优先级只算一次
 * 2. 键盘弹起时收掉底栏（默认开）—— 底栏贴在窗口底、被键盘盖住却仍占高度，会留一条空隙
 * 3. IME 内边距：输入框/发送按钮跟着键盘走，不用每页自己算
 * 4. Snackbar 宿主：`snackbarHostState.showSnackbar(...)` 直接用，位置自动避开底栏和键盘
 *
 * 约定：
 * - **[topBar] 槽位不做系统 inset**。用 [Header]（自带状态栏内边距、底纹铺到屏幕顶）或自己处理；
 *   再由壳统一加一层会双重避让，标题会掉下去一截。
 * - **[bottomBar] 槽位也不做 inset**。用 `AppBottomNavigation`（`NavigationBar` 自带手势条内边距）。
 * - 内容区由壳负责避让，页面里**不要再写** `windowInsetsPadding` / `imePadding`。
 *
 * ```kotlin
 * AppShell(
 *     topBar = { Header(title = "待办") },
 *     bottomBar = { AppBottomNavigation(items, selected, onSelected) },
 * ) {
 *     TodoList(Modifier.fillMaxSize())
 * }
 * ```
 *
 * @param hideBottomBarWhenImeVisible 键盘弹起时是否收掉底栏（默认 true，聊天/表单页要开）
 * @param applyImePadding 是否由壳给内容补 IME 底部内边距。默认 true；
 *   如果 Activity 用了 `adjustResize` 且老 ROM 会「窗口先缩、又派发 ime inset」造成双倍留白，
 *   就把它关掉，交回窗口 resize（见 apps/demo 的历史注释）。
 */
@Composable
fun AppShell(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    hideBottomBarWhenImeVisible: Boolean = true,
    applyImePadding: Boolean = true,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    // 键盘在不在：读 ime inset 的底边高度。
    // 不用 WindowInsets.isImeVisible —— 那是 Android 专属 API，commonMain 里编不过；
    // 桌面/iOS 的键盘也走 ime inset，这条判断三端通用。
    val keyboardUp = WindowInsets.ime.getBottom(density) > 0

    // 底部只由一处负责，避免「手势条 + 键盘」叠加出一道空隙：
    // 键盘在 → 只补 ime；没底栏 → 补手势条；有底栏 → 底栏自己处理
    val bottomInset = when {
        keyboardUp && applyImePadding ->
            Modifier.windowInsetsPadding(WindowInsets.ime.only(WindowInsetsSides.Bottom))

        bottomBar == null ->
            Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))

        else -> Modifier
    }

    Scaffold(
        modifier = modifier,
        topBar = { topBar?.invoke() },
        // 键盘弹起时连槽位一起撤掉：只隐藏内容但保留高度，空隙还在
        bottomBar = { if (!keyboardUp || !hideBottomBarWhenImeVisible) bottomBar?.invoke() },
        floatingActionButton = { floatingActionButton?.invoke() },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.windowInsetsPadding(WindowInsets.ime.only(WindowInsetsSides.Bottom)),
            )
        },
        // 顶部/左右交给内容；底部由上面的 bottomInset 单独算
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .then(bottomInset),
        ) {
            content()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun AppShellPreview() {
    AppTheme {
        AppShell(
            topBar = { Header(title = "页面标题") },
            bottomBar = {
                AppBottomNavigation(
                    items = DemoData.Components.bottomNavItems,
                    selectedIndex = 0,
                    onItemSelected = {},
                )
            },
            floatingActionButton = { PrimaryButton(text = "动作", onClick = {}) },
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("内容区（已避开系统栏和键盘）")
            }
        }
    }
}
