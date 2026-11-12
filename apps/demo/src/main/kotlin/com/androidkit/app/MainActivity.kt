package com.androidkit.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.androidkit.app.data.settings.AppSettings
import com.androidkit.app.data.settings.SettingsStore
import com.androidkit.app.demo.ComposerRequest
import com.androidkit.app.demo.TodoChatPage
import com.androidkit.app.demo.UpdateGate
import com.androidkit.app.shortcuts.AppShortcut
import com.androidkit.app.shortcuts.ShortcutLaunch
import com.androidkit.app.shortcuts.reportUsage
import com.androidkit.app.shortcuts.shortcutOrNull
import com.androidkit.app.ui.settings.SettingsScreen
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.M3Motion
import com.androidkit.app.ui.theme.resolveDark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath

/**
 * Android 入口。
 *
 * ponytail: 状态栏配色原来在 AppTheme 里改（要拿 Activity），多平台后没法这么干 ——
 * 统一交给 edge-to-edge，主题只管颜色。
 */
class MainActivity : ComponentActivity() {

    /**
     * 快捷方式进来的一次启动。用 StateFlow 而不是普通字段：
     * [onNewIntent] 跑在 composition 之外，得有个能安全跨界传递的容器。
     */
    private val shortcutLaunches = MutableStateFlow<ShortcutLaunch?>(null)
    private var shortcutSeq = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installCrashHandler()
        enableEdgeToEdge()
        // 只有冷启动才吃 Intent：旋转 / 切换深色重建时系统会把同一个 Intent 再送一遍，
        // 照吃就会把「新建待办」这类动作重放一次
        if (savedInstanceState == null) consumeShortcut(intent)
        setContent {
            val context = LocalContext.current
            // 设置落在 app 内部存储（各端目录不同：Android 传 filesDir）
            val store = remember { SettingsStore(context.filesDir.absolutePath.toPath()) }
            var settings by remember { mutableStateOf(AppSettings()) }
            var showSettings by remember { mutableStateOf(false) }
            var composerRequest by remember { mutableStateOf<ComposerRequest?>(null) }
            val shortcutLaunch by shortcutLaunches.collectAsState()
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) { settings = store.load() }

            // 桌面长按菜单 → 页面动作。聊天类的两条都要先退出设置页，再把光标送进输入框；
            // key 是 shortcutLaunch（含自增 seq），所以同一个动作连点两次也会再执行一遍
            LaunchedEffect(shortcutLaunch) {
                val current = shortcutLaunch ?: return@LaunchedEffect
                when (current.shortcut) {
                    AppShortcut.NEW_TODO -> {
                        showSettings = false
                        composerRequest = ComposerRequest(current.seq, draft = "记一下：")
                    }

                    AppShortcut.ASK -> {
                        showSettings = false
                        composerRequest = ComposerRequest(current.seq, draft = null)
                    }

                    AppShortcut.SETTINGS -> showSettings = true
                }
            }

            val dark = settings.appearanceMode.resolveDark(isSystemInDarkTheme())

            // 系统栏图标对比度跟着「生效的」深浅色走 —— 用户在 app 内强制深色时，
            // 系统可能还是浅色，enableEdgeToEdge() 的默认探测就错了
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    // android.graphics 的 Color（SystemBarStyle 要 int）
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT,
                    ) { dark },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT,
                    ) { dark },
                )
                onDispose { }
            }

            AppTheme(
                style = settings.themeStyle,
                darkTheme = dark,
                reducedMotion = settings.reducedMotion,
            ) {
                // 全屏铺主题底色：页面没设背景时露出的就是窗口底（XML 主题的浅色），
                // 深色主题下会花 —— 以这里的颜色为准
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    // 页面级转场在组合期取：AnimatedContent 的 transitionSpec lambda 不是 @Composable，
                    // 读不到 LocalReducedMotion（同 ListScreen 的做法）
                    val screenTransition = M3Motion.fadeThrough()
                    // 启动自动查一次新版本；有新版本时在页面之上弹 UpdateDialog
                    UpdateGate {
                        // 设置页 ↔ 聊天页：M3 Fade Through（同层级、无空间关系）。
                        // 旧页面 90ms 淡出，新页面隔 90ms 从 92% 放大淡入 —— 两段不重叠，
                        // 不会出现两层页面叠影。减动效由 M3Motion.fadeThrough() 内部处理。
                        AnimatedContent(
                            targetState = showSettings,
                            transitionSpec = { screenTransition },
                            label = "settings_toggle"
                        ) { isSettings ->
                            if (isSettings) {
                                SettingsScreen(
                                    settings = settings,
                                    onSettingsChange = { next ->
                                        settings = next
                                        scope.launch { store.save(next) }
                                    },
                                    onBackClick = { showSettings = false },
                                )
                            } else {
                                TodoChatPage(
                                    onSettingsClick = { showSettings = true },
                                    composerRequest = composerRequest,
                                    onComposerRequestHandled = { composerRequest = null },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * app 已在前台时从长按菜单进来走这里（manifest 里 launchMode=singleTop）。
     * 不新建 Activity —— 聊天记录、设置页停留在原处，只执行快捷方式那一个动作。
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeShortcut(intent)
    }

    /** 认快捷方式 → 上报使用（影响桌面排序）→ 交给 Compose 执行。不是快捷方式启动就什么都不做。 */
    private fun consumeShortcut(intent: Intent?) {
        val shortcut = intent?.shortcutOrNull() ?: return
        shortcut.reportUsage(this)
        shortcutLaunches.value = ShortcutLaunch(shortcut, ++shortcutSeq)
    }
}

/**
 * 崩溃兜底：写一行结构化日志，再交回系统默认 handler（系统的崩溃对话框照常弹）。
 *
 * 为什么不接 Firebase Crashlytics 之类：那是第三方 SDK，要联网、要把用户设备信息发到外网，
 * 而且这个仓库是**模板** —— 该不该上报、上报去哪，由每个 app 自己决定。
 * 模板只给最小可用的一层：崩溃现场（栈 + 线程 + 设备信息）落在 logcat 里，
 * 要接第三方就在这里换实现。
 *
 * 红线（见 SECURITY.md）：只记崩溃栈和设备信息，**不记用户数据**。
 */
private fun installCrashHandler() {
    val previous = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, error ->
        Log.e(
            "Crash",
            // 一行 JSON，方便 logcat 里 grep / 上传给日志系统
            """{"event":"crash","thread":"${thread.name}","exception":"${error.javaClass.name}","message":"${error.message}"}""",
            error
        )
        previous?.uncaughtException(thread, error)
    }
}
