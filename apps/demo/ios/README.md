# iOS 壳工程

Swift 只写一层薄壳，**里面的界面全是 Kotlin/Compose 那套** —— 跟安卓端同一份组件代码。

```
DemoApp/
├── DemoApp.swift      @main 入口（SwiftUI）
├── ComposeView.swift  把 Compose 的 UIViewController 套进 SwiftUI
├── Info.plist
└── Assets.xcassets/
```

Xcode 工程是 **XcodeGen** 生成的（`project.yml`），不手改 `.xcodeproj` —— 工程定义就几十行 YAML，能 diff、能 review。

## 跑起来（macOS）

```bash
# 一次性：装 XcodeGen
brew install xcodegen

# 全流程（编 framework → 生成工程 → 编译）
bash apps/demo/ios/build.sh

# 然后在 Xcode 里打开
open apps/demo/ios/DemoApp.xcodeproj
```

在 Xcode 里按 ⌘R 就能跑模拟器。**改了 Kotlin 代码不用手动跑 build.sh** —— 工程里挂了个脚本阶段，
每次构建前会自动重编 framework（第一次会慢，Gradle 缓存后就快了）。

## 这套东西是怎么串起来的

```
:kit (Kotlin) ──linkDebugFrameworkIos*──► Kit.framework（每个架构一份）
                                            │
                              lipo 合成 + 手写清单
                                            ▼
                                     Kit.xcframework（真机 + 模拟器）
                                            │
                                            ▼
                       Xcode 工程 ──链接──► Swift: import Kit
                                            │
                                            ▼
                       MainViewControllerKt.MainViewController()
```

`MainViewController()` 在 `kit/src/iosMain/kotlin/com/androidkit/app/MainViewController.kt`，
里面就是 `ComposeUIViewController { AppTheme { App() } }`。

## 已知边界

| 事 | 状态 |
|---|---|
| 界面组件（52 个）三端共用 | ✅ |
| agent（Koog）跑在 iOS 上 | ❌ **还没接** —— Koog 没打成 framework，而且 `Flow`/`List<SkillTool<*>>` 这些泛型接口 Swift 调不动，得先加一层 ObjC 友好的封装 |
| `Res.string` 资源 | ⚠️ 用到的话要把 `Kit.bundle` 加进 Copy Bundle Resources（现在 demo 页没用到） |
| App 图标 | ⚠️ `AppIcon.appiconset` 是空的，上架前得填 |
| 真机运行 | 需要在 Xcode 里选自己的开发者账号（Signing & Capabilities → Team） |

## 常见问题

**`No such module 'Kit'`**
framework 没编出来。跑一次 `bash apps/demo/ios/build.sh --framework-only`，
或者看 Xcode 构建日志最前面那个脚本阶段有没有红叉。

**`Failed to build Kotlin/Native`** / 找不到 `linkDebugFrameworkIosX64`
Kotlin/Native 的 iOS 目标只能在 macOS 上编，Windows/Linux 不行。

**模拟器报 `Library not loaded`**
模拟器切片是 fat 的（arm64 + x86_64），如果报架构不对，说明 `lipo` 那步没跑成功 ——
删掉 `Frameworks/` 重跑 `build.sh`。

**改完 Kotlin 代码，Xcode 里还是旧的**
脚本阶段默认会重编。如果嫌慢，可以把它在 Xcode 里改成手动运行（Build Phases → 右边铅笔），
或者直接跑 `bash apps/demo/ios/build.sh --framework-only` 再回 Xcode ⌘R。
