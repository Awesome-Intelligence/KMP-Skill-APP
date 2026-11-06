import SwiftUI
import UIKit
import Kit

/// 把 Kotlin 侧的 Compose 页面套进 SwiftUI。
///
/// - `Kit`：`:kit` 模块产出的 framework（baseName = "Kit"）
/// - `MainViewControllerKt.MainViewController()`：kit/src/iosMain 里的入口，
///   里面是 `ComposeUIViewController { AppTheme { App() } }`
///
/// 52 个 Compose 组件一行都不用改，iOS 和 Android 长得一模一样。
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ controller: UIViewController, context: Context) {}
}
