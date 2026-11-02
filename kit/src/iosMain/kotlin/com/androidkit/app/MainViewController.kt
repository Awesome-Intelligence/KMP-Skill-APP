package com.androidkit.app

import androidx.compose.ui.window.ComposeUIViewController
import com.androidkit.app.ui.theme.AppTheme
import platform.UIKit.UIViewController

/**
 * iOS 入口 —— Swift 侧这样接：
 *
 * ```swift
 * struct ComposeView: UIViewControllerRepresentable {
 *     func makeUIViewController(context: Context) -> UIViewController {
 *         MainViewControllerKt.MainViewController()
 *     }
 *     func updateUIViewController(_ controller: UIViewController, context: Context) {}
 * }
 * ```
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    AppTheme {
        App()
    }
}
