package com.androidkit.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.androidkit.app.demo.TodoChatPage
import com.androidkit.app.ui.theme.AppTheme

/**
 * Android 入口。
 *
 * ponytail: 状态栏配色原来在 AppTheme 里改（要拿 Activity），多平台后没法这么干 ——
 * 统一交给 edge-to-edge，主题只管颜色。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installCrashHandler()
        enableEdgeToEdge()
        setContent {
            AppTheme {
                // 业务 demo：待办助手（数据 + skill + 页面都在 apps/demo/ 里）
                TodoChatPage()
            }
        }
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
