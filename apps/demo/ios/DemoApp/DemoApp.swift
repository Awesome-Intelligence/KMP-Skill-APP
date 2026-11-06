import SwiftUI

/// iOS 壳工程入口。
///
/// 里面装的不是 SwiftUI 页面，而是 Kotlin/Compose 那套（见 ComposeView.swift）。
/// 以后 iOS 上要做点 iOS 特有的东西（Share Sheet、原生导航栏），在这一层加 SwiftUI，
/// 不用动 Compose 那边。
@main
struct DemoApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeView()
                // Compose 自己管安全区（Android 那边是 edge-to-edge），这里同样铺满
                .ignoresSafeArea(.all)
        }
    }
}
