# Android-Kit

**Compose Multiplatform** 开发套件 —— 规则、视觉规范、组件层、构建配置。
一套代码同时出 **Android / iOS / Desktop** 三端。

**不搬业务架构**：没有 `data/model`、没有 `data/repository`、没有 `ui/screens`，
每个新项目自己写业务，kit 只给「不用重新发明」的那部分。

用途：**独立仓库 + 复制粘贴**。新项目各自建仓，从这里拷需要的部分。

---

## 目录

```
Android-Kit/
├── README.md                 ← 你在这
├── ARCHITECTURE.md           ← ★ agent 应用通用架构：backend + kit + app 怎么拼成一个 app
├── settings.gradle.kts       ← KMP 工程根配置（插件 + 仓库，国内镜像优先）
├── build.gradle.kts          ← 插件版本声明
├── gradle.properties         ← JVM 参数、AndroidX、Kotlin
├── rules/                    ← 规则与规范
│   ├── CLAUDE.template.md      新项目的 agent 规则（已修正 5 条规则漂移）
│   ├── GOOGLE_STYLE.md         Google 品牌视觉规范（数值级别，原样可用）
│   ├── COMPONENT_SPEC.md       设计令牌 + 组件规格 + 动效
│   └── KIT-NOTES.md            ★ 边界在哪、为什么这么取舍
├── kit/                     ← ★ KMP 库模块（组件本体，三端共用）
│   ├── build.gradle.kts        android / desktop / ios 三目标 + compose-resources
│   └── src/
│       ├── commonMain/kotlin/com/androidkit/app/
│       │   ├── ui/theme/       Mono 主题、Type、Motion、TagColors、扩展语义色
│       │   ├── ui/components/  通用组件（AppCard / Button / Dialog / SearchBar …）
│       │   ├── ui/templates/   ListScreen / FormScreen 两个页面骨架
│       │   ├── ui/preview/     精简版 DemoData（预览用）
│       │   ├── data/storage/   存储层（okio 引擎 + 可插拔格式 codec，三端通用）
│       │   └── (agent 契约与 Koog 运行时在 kit-agent-koog/)
│       ├── commonMain/composeResources/values{,-en}/strings.xml  ← 文案（compose-resources）
│       ├── commonTest/          KitDateFormat 单测
│       ├── androidMain/kotlin/  SpeechInput（语音）、SAF 存储（SafStorageEngine）
│       ├── desktopMain/kotlin/  main.kt（application/Window 入口）
│       └── iosMain/kotlin/      MainViewController.kt
├── kit-agent-koog/         ← agent 运行时（Koog 1.3.0）：AgentRuntime 契约 + SkillDefinition/SkillTool + Koog 实现
├── apps/demo/               ← 示例 app（壳工程 + 业务 skill + 页面）。fork 后你的 app 放 apps/<name>/
│   └── ios/                   iOS 壳工程：SwiftUI 薄壳 + Compose 内核（XcodeGen 生成 Xcode 工程）
├── chat-demo/               ← 桌面对话 demo（验证 agent 对话链路，见下）
├── backend/                 ← 可选的 agent 网关（Node/TS + Claude Agent SDK）；默认 agent 跑在手机端，见 ARCHITECTURE.md
├── .github/workflows/       ← GitHub Actions：Android / Desktop / iOS 三条构建线 + 静态检查 + backend 网关自检 + release
├── config/detekt/          ← detekt 规则集（只查能抓真 bug 的那批规则）
├── api/                    ← 公开 API 的二进制基线（binary-compatibility-validator 生成的 .api 快照）
├── skills/                  ← Google 官方 android/skills 子集 + M3 对照 skill
└── scripts/
    ├── install-kit.sh           装进新项目（kit + agent 模块、包名替换、strings 合并）
    ├── merge_strings.py         strings.xml 合并（只补缺失 key）
    ├── verify-kit.py            ★ 静态自检：import / Res.string / DemoData 三类断裂
    ├── verify-skills.py         ★ 静态自检：skill 工具有没有把业务逻辑写进 tool（只警告）
    └── diff-ownership.sh        多 Claude 并行时的文件归属判定
```

---

## KMP 约定（重要）

| 约定 | 为什么 |
|---|---|
| `commonMain` 里**禁止** `import android.*` / `import java.*` | 一写就三端编不过。平台差异下沉到 `androidMain` |
| **不写 expect/actual 平台接口** | kit 只给纯 UI + 纯逻辑。平台能力由各端项目自己实现 |
| 文案走 **compose-resources**（`Res.string.x`），不是 `R.string.x` | `R` 是 Android 专属的 |
| `@Preview` 在 `androidx.compose.ui.tooling.preview.Preview` | CMP 版包名变了，但注解类位置没变 |
| **iOS 目标只在 macOS host 上能编** | Kotlin/Native 限制。Windows 本地编不了，CI 的 macOS job 负责 |
| `java.time` → `kotlinx-datetime` + 本地 `KitDateFormat` | `java.time` 是 JVM 专属 |
| 文件存储用 **okio**，不用 `java.io.File` | 一份实现三端通用 |
| HTTP 用 **Ktor**，引擎按平台挂（android/desktop okhttp、ios darwin） | Retrofit 是 JVM 专属 |
|  Timber（5.0.1）**没有 KMP 产物** | 公共层改用 `println` |

---

## 存储层（不锁死 markdown）

存储拆成两层：**引擎**管字节往哪写，**codec** 管实体在磁盘上长什么样。两层都能换。

| 层 | 现有实现 | 加新的要做什么 |
|---|---|---|
| `EntityStore` | `FileStorageEngine`（okio，app 内部存储）、`SafStorageEngine`（Android，用户自选目录） | 实现接口 —— 数据库引擎也在这层，调用方不变 |
| `StoreCodec` | `MarkdownCodec`（frontmatter + 正文，`.md`）、`JsonCodec`（`.json`） | 实现接口 |

字段是**类型化的**（`Map<String, JsonElement>`）：数字、布尔、数组、嵌套对象都是一等公民，
不用再塞进字符串里靠 `intOr` 猜类型。

```kotlin
val store: EntityStore = FileStorageEngine(rootDir, JsonCodec())  // 想写 .md 就换回 MarkdownCodec()
store.write("goals", StoredEntity(
    id = "goal-1",
    fields = mapOf("progress" to JsonPrimitive(0.65), "done" to JsonPrimitive(false)),
    body = "正文",
))
val fields = store.read("goals", "goal-1")!!.fields
fields.doubleOr("progress", 0.0)   // 0.65
```

旧数据（值全是字符串的老 md）**不用迁移**：`doubleOr` / `boolOr` / `listOrEmpty` 宽松取值。

---

## 怎么用

### 起一个新 app（推荐：fork 本仓库）

本仓库是**底座**。新 app 不把代码拷走，而是 fork（或 clone）整个仓库，在 `apps/` 里长出来，
靠 `git pull upstream main` 持续跟进底座更新：

```bash
git clone <本仓库地址> myapp && cd myapp
git remote rename origin upstream   # 以后 git pull upstream main
```

1. 照抄 `apps/demo/` → `apps/<name>/`，改四处：`build.gradle.kts` 的 namespace / applicationId、
   `strings.xml` 的 app_name、主题名（`Theme.AndroidKit` → 你的）。
   `settings.gradle.kts` **自动 include `apps/` 下所有模块**（不用注册），CI 自动逐个构建。
2. 写业务 skill + 页面，见 `ARCHITECTURE.md`「写一个新 app 的步骤」。
3. 发布时把 `.github/workflows/release.yml` 拷成自己的 `release-<name>.yml`（fork 自己拥有的文件，随便改）。

**唯一规矩：底座路径（`kit/`、`kit-agent-koog/`、`rules/`、`.github/workflows/`、`backend/`、`settings.gradle.kts`）
不改不删** —— 改了，pull 上游必冲突，更新链就断。底座能力不够，回上游提 PR，别在下游打补丁。

### 只要一部分（拷贝脚本，备选）

不想 fork 全套、想把 kit 拷进已有项目？老路径还在：

```bash
bash scripts/install-kit.sh ~/code/MyApp com.myname.myapp MyApp
```

脚本会：复制 KMP 源集（`:kit` + `:kit-agent-koog`）→ 按新包名重排目录并替换包名 → 合并 `strings.xml` → 装规则/CI/skills。
**它不替你做决定**，装完照提示改这几处：

1. `settings.gradle.kts`：`include(":kit")`（要 agent 再加 `include(":kit-agent-koog")`），并把根插件块合进你的 `build.gradle.kts`
2. 你的 `:app` 模块：`implementation(project(":kit"))`（要 agent 再加 `implementation(project(":kit-agent-koog"))`）
3. `app/build.gradle.kts` 的 `namespace` / `applicationId`
4. release 签名换真 keystore（kit 里写好了正确写法，别抄 debug 签名那行）
5. `settings.gradle.kts` 的 `rootProject.name`
6. `strings.xml` 的 `app_name`
7. `Theme.App` → `Theme.<你的应用名>`

> 注意：拷走的代码不会跟着本仓库更新 —— 要持续跟进就选上面的 fork 模式。

### 只拿一部分

不想全套？按需拷：

| 你只要 | 拷这个 |
|---|---|
| agent 规则 | `rules/CLAUDE.template.md` → 目标 `CLAUDE.md` |
| 视觉规范 | `rules/GOOGLE_STYLE.md` + `rules/COMPONENT_SPEC.md` |
| 组件库 | `kit/src/commonMain/kotlin/.../ui/{theme,components,templates,preview}` + `composeResources/` |
| 存储层 | `kit/src/commonMain/.../data/storage/`（okio 引擎 + 格式 codec，三端通用） |
| 本地 agent 引擎 | `kit-agent-koog/src/commonMain/.../data/agent/`（Koog loop 跑在端上，模型直连 Anthropic API）+ `kit-agent-koog/build.gradle.kts` |
| CI | `.github/workflows/*.yml` 原样可用，fork 即生效 |

组件依赖 `composeResources/values/strings.xml` 里的 key，单独拷组件时把 strings.xml 一起带走（或用 `merge_strings.py` 补）。

### iOS 怎么接

iOS 侧要一个 Xcode 工程，导入 kit 产出的 framework：

1. 在 macOS 上跑 `./gradlew :kit:linkReleaseFrameworkIosArm64 :kit:linkReleaseFrameworkIosSimulatorArm64`
   → 得到 `kit/build/bin/ios*/releaseFramework/Kit.framework`
2. Xcode 里 **General → Frameworks, Libraries, and Embedded Content** 加 `Kit.framework`
3. Swift 侧接 UIKit 入口：
```swift
import SwiftUI
import UIKit
import Kit   // framework 名 = build.gradle.kts 里的 baseName

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ controller: UIViewController, context: Context) {}
}
```

**现成的例子**：`apps/demo/ios/` 就是照上面这套做的完整壳工程（Swift 薄壳 + Compose 内核 +
XcodeGen 生成的 Xcode 工程 + 一键构建脚本）。抄 app 时整个目录拷走就行：

```bash
bash apps/demo/ios/build.sh     # 编 framework → 打 xcframework → 生成工程 → 编译
open apps/demo/ios/DemoApp.xcodeproj
```

---

## 三条最重要的注意事项

1. **规则模板里有 5 条容易「写了没做到」**，`rules/KIT-NOTES.md` 列了对照表，`CLAUDE.template.md` 里已经改过来。
   别把旧的 `CLAUDE.md` 抄回去。

2. **release 签名**：用 debug 密钥签 release 是常见错写法，测试用可以，发商店必炸。
   kit 里给的是正确写法。

3. **iOS 只能靠 CI 验证**：本地 Windows/Linux 编不了 iOS target。
   `kit/build.gradle.kts` 里按 host 门控（`hostIsMac`），Windows 上不会声明 iOS target，所以本地构建不会被 native 工具链绊倒。

---

## 桌面对话 demo（验证 agent 链路）

`chat-demo/`：一个 Compose Desktop 聊天窗口，串起「前端 → skill → Koog → Anthropic API」全链路，
带一个 `now` 工具顺带验证工具调用。

```bash
gradlew :chat-demo:run                        # 开窗口对话
gradlew :chat-demo:run --args="--check"       # 无界面跑一轮，把事件打出来
```

配置走环境变量：`ANTHROPIC_API_KEY`（或 `ANTHROPIC_AUTH_TOKEN`）、`ANTHROPIC_BASE_URL`、`ANTHROPIC_MODEL`。

---

## 已验证

kit 代码在真实 Gradle 工程里跑通过（2026-11-03 复核）：

| 验证项 | 结果 |
|---|---|
| `:apps:demo:assembleDebug`（Android，含 manifest / 资源 / 主题 / 打包 / agent 模块依赖） | ✅ BUILD SUCCESSFUL |
| `:kit:compileAndroidMain`（Android 目标 + androidMain 专属代码） | ✅ BUILD SUCCESSFUL |
| `:kit:compileKotlinDesktop`（桌面目标） | ✅ BUILD SUCCESSFUL |
| `:kit:desktopTest`（KitDateFormat 4 + StoreCodec 4） | ✅ 8 个用例通过 |
| `:kit-agent-koog:desktopTest`（SkillTool 3 + KoogAgentSession 5） | ✅ 8 个用例通过 |
| `:kit:detekt` `:kit-agent-koog:detekt` `:apps:demo:detekt` | ✅ 0 问题（规则集 `config/detekt/`） |
| `:kit:apiCheck`（公开 API 二进制兼容） | ✅ 与 `api/*.api` 基线一致 |
| `scripts/verify-kit.py` | ✅ 通过（62 个 kt 文件，133 个 string key） |
| `scripts/verify-skills.py` | ✅ 通过（4 个 tool，分支都在 data 层） |
| `:chat-demo:run --args="--check"`（agent 全链路） | ✅ ToolUse(now) → Text → Result |
| `backend: npm run check`（tsc 7.0.2 `--noEmit`） | ✅ 无错 |
| `backend: npm run smoke`（skill 加载 + 契约校验 + SSE 断连检测 + 鉴权/session 归属/限流） | ✅ 通过 |
| `backend: node --import tsx src/server.ts` + `/health` `/v1/skills` | ✅ 路由通 |
| iOS | ⚠️ 本机是 Windows 编不了，靠 CI 的 macOS job 验 |
| 依赖校验（`gradle/verification-metadata.xml`） | ✅ 生成的 sha256 基线（JVM/Android 产物；iOS 产物要在 macOS 上补） |

> ⚠️ 依赖约束：compileSdk 要到 **37.1**（`compileSdkMinor = 1`）。
> AGP 9 的 KMP 库插件里 `compileSdkMinor` 不可用，要用新的 DSL：
> ```kotlin
> android {
>     compileSdk { version = release(37) { minorApiLevel = 1 } }
> }
> ```

---

## 改组件的流程

组件本体在 `kit/src/commonMain/` 里，改完直接在这里验：
`./gradlew :kit:compileKotlinDesktop` → `python scripts/verify-kit.py` 自检。

改 app 的 skill 工具（业务逻辑该下沉到 data 层，tool 只做薄包装）：
`python scripts/verify-skills.py` 自检 —— 规范见 `rules/CLAUDE.template.md`「逻辑 api 化」。

---

## 依赖前提

- JDK 17
- AGP 9.5.0 / Kotlin 2.4.21 / Compose Multiplatform 1.12.2 / Gradle 9.8.2
- compileSdk 37.1 / minSdk 26 / targetSdk 36（Play 自 2026-08-31 起要求 ≥ 36）
- 版本号统一在 `gradle/libs.versions.toml` 里改
