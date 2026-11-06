package com.androidkit.app

import android.os.Bundle
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
        enableEdgeToEdge()
        setContent {
            AppTheme {
                // 业务 demo：待办助手（数据 + skill + 页面都在 apps/demo/ 里）
                TodoChatPage()
            }
        }
    }
}
