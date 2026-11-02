package com.androidkit.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.androidkit.app.ui.theme.AppTheme

/** Desktop 入口。 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Android-Kit"
    ) {
        AppTheme {
            App()
        }
    }
}
