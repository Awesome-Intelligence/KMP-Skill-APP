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
├── build-logic/              ← 约定插件（androidkit.android.app）：SDK / 签名 / release / detekt 统一配置
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
│       │   ├── ui/theme/       Mono / Swiss / Notion 等主题、ThemeTokens（品牌主题扩展点）、Type、Motion、TagColors
│       │   ├── ui/navigation/  NavTransitions（页面级 M3 转场：Fade Through / Shared Axis）
│       │   ├── ui/components/  通用组件（AppCard / Button / Dialog / SearchBar …）
│       │   ├── ui/templates/   ListScreen / FormScreen 两个页面骨架
│       │   ├── ui/shell/       AppShell（系统栏 / 键盘 / 底栏 / Snackbar 的单一入口）
│       │   ├── ui/state/       UiState（Idle / Loading / Success / Error）
│       │   ├── ui/preview/     精简版 DemoData（预览用）
│       │   ├── data/storage/   存储层（okio 引擎 + 可插拔格式 codec，三端通用）
│       │   ├── data/update/    版本检查（语义化版本比对 + 清单来源 + 节流；HTTP 由 App 提供）
│       │   └── (agent 契约与 Koog 运行时在 kit-agent-koog/)
│       ├── commonMain/composeResources/values{,-en}/strings.xml  ← 文案（compose-resources）
│       ├── commonTest/          纯逻辑单测（日期 / 存储 / 更新 / 主题 / UiState）
│       ├── androidMain/kotlin/  SpeechInput（语音）、SAF 存储（SafStorageEngine）
│       ├── desktopMain/kotlin/  main.kt（application/Window 入口）
│       └── iosMain/kotlin/      MainViewController.kt
├── kit-agent-koog/         ← agent 运行时（Koog 1.3.0）：AgentRuntime 契约 + SkillDefinition/SkillTool + Koog 实现 + 网关实现
├── kit-membership/         ← 付费/会员框架（可选模块）：档位表 + 权益/额度引擎 + 会员状态落盘 + 付费墙 UI
├── apps/demo/               ← 示例 app（壳工程 + 业务 skill + 页面）。fork 后你的 app 放 apps/<name>/
│   └── ios/                   iOS 壳工程：SwiftUI 薄壳 + Compose 内核（XcodeGen 生成 Xcode 工程）
├── chat-demo/               ← 桌面对话 demo（验证 agent 对话链路，见下）
├── backend/                 ← 可选的 agent 网关（Node/TS + Claude Agent SDK）；默认 agent 跑在手机端，见 ARCHITECTURE.md
├── .github/workflows/       ← GitHub Actions：Android / Desktop / iOS 三条构建线 + 静态检查 + backend 网关自检 + release
├── config/detekt/          ← detekt 规则集（只查能抓真 bug 的那批规则）
├── kit/api/desktop/kit.api ← 公开 API 的二进制基线（binary-compatibility-validator 生成）
├── skills/                  ← Google 官方 android/skills 子集 + M3 对照 skill
└── scripts/
    ├── ci.sh                    ★ 门禁执行层：CI 和本地跑的是同一份（detekt/api/verify/test/lint/build…）
    ├── checks/                  ★ fork 自定义门禁的落点：放个 .sh 进去就自动跑
    ├── new_app.py               ★ 从模板生成 apps/<name> 最小可编译壳（含壳自检单测）
    ├── templates/app/           生成器模板（manifest / MainActivity / 主题 / build.gradle.kts / 单测）
    ├── install-kit.sh           装进新项目（kit + agent 模块、包名替换、strings 合并）
    ├── merge_strings.py         strings.xml 合并（只补缺失 key）
    ├── verify-kit.py            ★ 静态自检：import / Res.string / DemoData 三类断裂
    ├── verify-skills.py         ★ 静态自检：skill 工具有没有把业务逻辑写进 tool（只警告）
    ├── verify-i18n.py           ★ 静态自检：values 和 values-en 的 key 对齐
    └── diff-ownership.sh        多 Claude 并行时的文件归属判定
```

### 门禁怎么跑

```bash
bash scripts/ci.sh all        # 提交前跑这个：detekt + api + verify + test
```

`ci.sh` 是**门禁的唯一出处** —— `.github/workflows/ci.yml` 只是薄壳（说在什么环境跑哪道门禁）。
fork 想加一道自己的门禁：往 `scripts/checks/` 放一个 `.sh`，CI 自动跑，
不用改 workflow（用别的文件名就不会跟上游的文件撞名）。详见 `scripts/checks/README.md`。

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

## 主题（ThemeTokens）

底座预置 Mono / Swiss / Notion 等几套，但 **App 的品牌主题不该改 kit**：

```kotlin
// apps/<name>/theme/BrandTheme.kt
object BrandTheme {
    fun light() = themeTokens(
        colorScheme = lightColorScheme(primary = Color(0xFF00696D), /* … */),
        isDark = false,
        cornerStyle = CornerStyle.Rounded,
        tagColors = mapOf("neutral" to Color(0xFF334455)),
    )
    fun dark() = themeTokens(colorScheme = darkColorScheme(/* … */), isDark = true)
}

// 入口
AppTheme(tokens = if (dark) BrandTheme.dark() else BrandTheme.light()) { App() }
```

- 四个轴：`colorScheme` / `extendedColors` / `typography` / `shapes`
- 三个开关：`isDark`（这套 tokens 的明暗，不是系统明暗）、`themeStyle`（风格标识）、`cornerStyle`（圆角）
- `tagColors` 留空 = 按主题风格取底座色板；品牌有自己的色板就传
- 只传颜色也能跑，其余轴走默认值
- 可编译样板：[apps/demo/src/main/kotlin/com/androidkit/app/theme/DemoBrand.kt](apps/demo/src/main/kotlin/com/androidkit/app/theme/DemoBrand.kt)

预设入口不变：`AppTheme(style = ThemeStyle.Mono / Swiss / Notion / EInk)`；
想拿预设当基底再微调，用 `monoThemeTokens(isDark)` / `swissThemeTokens(isDark)` 等工厂。

### Swiss（瑞士国际风格）

底座预置预设：纯白画布 + 纯黑文字 + 信号红强调，Helvetica 系无衬线（只有 400/700）、
全直角、零阴影零渐变、150ms ease-out 只变色。配套四个组件（规范里的角色）：
`SwissSectionLabel` / `SwissStructuralRow` / `SwissInlineButton` / `SwissColorField`，
外加一套布局骨架（规范里的 Layout 段）：`SwissPage` / `SwissGrid` / `SwissRule` / `SwissSection`。

```kotlin
AppTheme(style = ThemeStyle.Swiss) { App() }
```

规范（StyleKit swiss-style）本身只给了浅色，深色版是自建的语义反转（黑画布 + 白字）；
红色 `#ff0000` 对白底只有 4.0:1，小字位用 `#c40000`。

```kotlin
// 十二栏网格：栏间距 24px，页面封顶 1200px，分区间距 96px
SwissPage {
    SwissSection {
        SwissGrid(
            columns = listOf(
                8 to { SwissInlineButton(text = "Explore", onClick = {}) },
                4 to { SwissColorField(color = Swiss.redBlock, title = "Swiss") }
            )
        )
    }
}
```

切到 Swiss 主题时，kit 里的共享组件也跟着收口（否则就是「半套主题」）：
卡片容器透明化（规范「Do not wrap content in cards」）、装饰性圆形改直角、
文字一律左对齐、禁用态降对比但保持可读（不用 M3 默认的 38% 透明）。

### 电子纸（E-Ink）

底座预设：暖纸底 `#f5f3ea` + 墨色 `#20211e` + 三档灰阶（`#555750` / `#bab9b0` / `#e6e4da`），
零色相、全直角、零阴影、180ms ease-out 只变色；数值与单位用等宽字样（`EInkType.data`），
配套五个组件 + 一个装饰修饰符：
`EInkInkAction`（墨面主操作，44dp 高、零圆角，悬停转面板灰、聚焦翻成纸底墨字 + 2px 墨环）、
`EInkPaperField`（纸面输入，可见 label + 1px 边框 + 聚焦 2px 墨环）、
`EInkReadingRow`（1px 栏线分隔的行，选中 = 墨线 + 「已选」文字，窄屏 < 360dp 时数值换行到第二行）、
`EInkPanel`（信息面板，纸底 + 1px 栏线 + 0 elevation）、
`EInkStatusMarker`（实心/空心方块 + 状态词）、`Modifier.eInkGrain()`（装饰性网点底纹）。

```kotlin
AppTheme(style = ThemeStyle.EInk) { App() }
```

规范（StyleKit e-ink）只给了浅色，深色版是自建的语义反转（墨底 + 纸字）；
状态一律灰阶 —— 错误和警告不另发明色相，状态由**形状 + 文字**扛：
错误提示前带空心方块，危险操作用纸底 + 墨框而不是红色实心。

### macOS 毛玻璃（vibrancy）

底座第四套预设：三档灰 `#1c1c1e → #2c2c2e → #3a3a3c` 定层次（不用阴影制造深度）、
系统蓝 `#0a84ff` 是唯一强调色、衬线标题 + 无衬线正文 + 等宽代码、圆角 6/10/14、
200ms ease-out 只改颜色。配套四个组件（规范里的角色）：
`MacOSVibrancyPanel` / `MacOSListRow` / `MacOSToolbar` / `MacOSSegmentedControl`。

```kotlin
AppTheme(style = ThemeStyle.MacOS) { App() }
```

规范（StyleKit macos-vibrancy）本身是**暗色原生**，浅色版是自建的 macOS 浅色系统；
分隔一律 1px 半透明白（8% / 12%）。Compose Multiplatform 没有 backdrop-filter，
毛玻璃层用「半透明 + 1px 边」近似，真背景模糊要各端原生实现。

---

## 自动发现新版本（`data/update/`）

底座给“查有没有新版本”的纯逻辑，**不给**平台跳转/安装：版本号、HTTP 客户端、下载动作都由 App 传进来
（kit 不写 expect/actual）。

| 文件 | 内容 |
|---|---|
| `AppVersion` | 语义化版本解析/比较（`v1.2`、`1.2.3-beta.1`、`1.2.3+build`；数字段按数值比，不按字典序） |
| `UpdateManifest` | 版本清单契约（JSON，见下） |
| `UpdateSource` | 版本来源：`HttpUpdateSource`（静态 JSON）/ `GitHubReleaseSource`（GitHub Releases） |
| `UpdateChecker` | 节流（默认 6h）→ 拉清单 → 比版本；返回 `UpToDate` / `Available` / `Skipped` / `Throttled` / `Failed` |
| `FileUpdateStateStore` | 落盘“上次检查时间 + 跳过的版本”（okio，三端通用） |
| `UpdateDialog`（`ui/components/`） | 提示弹窗：强制更新不可关闭；可选“稍后 / 跳过此版本” |

清单长这样（放 CDN / GitHub Pages / 对象存储都行，**必须 HTTPS**）：

```json
{
  "latestVersion": "1.4.0",
  "minSupportedVersion": "1.2.0",
  "downloadUrl": "https://example.com/app-1.4.0.apk",
  "releaseNotes": "- 修复…\n- 新增…",
  "publishedAt": "2026-11-10T12:00:00Z"
}
```

`minSupportedVersion` 高于当前版本时 `Available.mandatory = true`（UI 必须挡住入口）；
不填 / 写错都按“不强制”处理。

接进 App（Android 示例，完整版见 `apps/demo/src/main/kotlin/com/androidkit/app/demo/UpdateGate.kt`）：

```kotlin
val checker = UpdateChecker(
    source = HttpUpdateSource(httpClient, "https://example.com/latest.json"),
    stateStore = FileUpdateStateStore(filesDir.toPath()),
)
when (val result = checker.check(BuildConfig.VERSION_NAME)) {
    is UpdateResult.Available -> UpdateDialog(
        latestVersion = result.manifest.latestVersion,
        currentVersion = BuildConfig.VERSION_NAME,
        releaseNotes = result.manifest.releaseNotes,
        mandatory = result.mandatory,
        onUpdate = { /* 跳商店 / 浏览器 */ },
        onLater = {},
        onSkipVersion = { /* checker.skip(...) */ },
    )
    else -> Unit   // 失败 / 节流 / 已最新：静默，不打扰用户
}
```

要点：

- **HTTP 客户端由 App 提供**：Ktor 引擎按平台挂（Android/Desktop OkHttp、iOS Darwin），
  kit 只定义 `HttpClient` 类型、不替你 new
- **冷启动或回前台调一次即可**：`UpdateChecker` 自己节流；手动检查传 `check(..., force = true)`，
  会绕过节流和“跳过此版本”
- 没配清单就直接读 GitHub Releases（`GitHubReleaseSource`，零托管）。它没有强制更新字段，
  且 `release.yml` 的 `draft: true` 草稿要手动发布后才会被查到
- “立即更新”是 App 的事：Android 跳商店 / 浏览器 / Play In-App Updates，iOS 跳 App Store，
  Desktop 跳下载页
- **更新通道是攻击面**：清单和下载地址必须 HTTPS；上架应用优先跳商店，别让用户装来路不明的包
- demo 的配置在 `apps/demo/build.gradle.kts` 文件头：`-PupdateManifestUrl=…`（优先）或
  `-PupdateGithubRepo=owner/repo`（默认本仓库）

---

## 付费 / 会员（`kit-membership/`）

可选模块 —— 不引它就当没有付费这回事。框架只做**平台无关**的部分；
Play Billing / StoreKit 这些平台 SDK 由各端 app 自己实现（`BillingProvider`）。

| 文件 | 是什么 |
|---|---|
| `Plan.kt` | 档位表：`Plan`（id / tier / 权益点 / 额度上限）+ `PlanCatalog`（业务侧一张表） |
| `Membership.kt` | 会员状态：当前档位 + 到期时间 + 是否验过签 |
| `MembershipStore.kt` | 状态落盘（okio JSON，三端通用） |
| `UsageTracker.kt` | 额度计数：按天重置（`DAILY`）/ 永不过期（`TOTAL`） |
| `MembershipManager.kt` | 门面：`has()` / `check()` / `consume()` / `purchase()` / `restore()` |
| `BillingProvider.kt` | 平台支付通道接口（各端实现） |
| `ui/UpgradeSheet.kt` | 升级页：档位对比 + 购买 + 恢复购买 |
| `ui/PaywallDialog.kt` | 额度用完时的付费墙弹窗 |
| `ui/PlanOption.kt` | 升级页的数据来源：`provider.planOptions(catalog)` —— 商店商品 × 档位表 |

```kotlin
// 业务侧一张档位表
val catalog = PlanCatalog(listOf(
    Plan("free", 0, limits = mapOf("ai" to QuotaLimit(5, QuotaPeriod.DAILY))),
    Plan("premium_monthly", 1, entitlements = setOf("export"),
         limits = mapOf("ai" to QuotaLimit(100, QuotaPeriod.DAILY))),
))
val manager = MembershipManager(catalog, MembershipStore(dir), UsageTracker(dir))

// 业务动作里查额度：超了就把用户送去付费墙
when (val access = manager.consume("ai")) {
    is Access.Allowed -> doIt()
    is Access.Blocked -> showPaywall(access)
}
```

规矩：

- 会员资格只认**已验过签**的购买（`Membership.verified`）；过期 / 没验签一律按免费档算
- 额度用 `consume()` 扣，别自己加 —— 扣了才知道超没超
- 档位表写在代码里，不从服务器下发（下发的话上限能被随便改）
- 额度上限按业务配（免费档 5 次/天、付费档 100 次/天这种），不是框架写死的

---

## 怎么用

### 起一个新 app（推荐：fork 本仓库）

本仓库是**底座**。新 app 不把代码拷走，而是 fork（或 clone）整个仓库，在 `apps/` 里长出来，
靠 `git pull upstream main` 持续跟进底座更新：

```bash
git clone <本仓库地址> myapp && cd myapp
git remote rename origin upstream   # 以后 git pull upstream main
```

1. 生成 app 壳：
   ```bash
   python scripts/new_app.py myapp --package com.example.myapp --app-name "我的应用"
   ```
   它从 `scripts/templates/app/` 生成最小可编译壳（manifest / MainActivity / 主题 / strings /
   build.gradle.kts）。`settings.gradle.kts` **自动 include `apps/` 下所有模块**（不用注册），CI 自动逐个构建。
   SDK 版本 / 签名 / release 收缩 / detekt 由 `build-logic/` 的 `androidkit.android.app` 统一管，
   每个 app 的 build 文件只留 namespace / applicationId 和自己的配置。
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

## agent 的两种运行模式

| 模式 | loop 在哪 | 出网 | 适合 |
|---|---|---|---|
| `AgentMode.Local` | 手机 | prompt + 工具结果 | 数据不出端、个人 / 内测 |
| `AgentMode.Gateway` | 你的服务器 | 一句输入 + 结果 | 上架、key 不上端、集中管控 |

```kotlin
val runtime = createAgentRuntime(
    if (useGateway) AgentMode.Gateway(baseUrl, token, httpClient)
    else AgentMode.Local(apiKey)
)
val session = runtime.session(skill, approver)
```

两种模式实现同一个 `AgentRuntime`，UI 不用改。网关协议见 [backend/README.md](backend/README.md)：
`POST /v1/skills/:id/invoke` → SSE。**注意**：网关模式下工具在服务端执行，本地 `ToolApprover`
不生效（危险工具白名单归服务端 `SKILL.md` 的 `tools` 字段）。

---

## 桌面对话 demo（验证 agent 链路）

`chat-demo/`：一个 Compose Desktop 聊天窗口，串起「前端 → skill → Koog → Anthropic API」全链路，
带一个 `now` 工具顺带验证工具调用。

```bash
gradlew :chat-demo:run                        # 开窗口对话
gradlew :chat-demo:run --args="--check"       # 无界面跑一轮，把事件打出来
gradlew :chat-demo:run --args="--fake"        # 本地假运行时：不联网、不需要 key
gradlew :chat-demo:run --args="--check --fake" # 无 key 的端到端自检（CI 可跑）
```

配置走环境变量：`ANTHROPIC_API_KEY`（或 `ANTHROPIC_AUTH_TOKEN`）、`ANTHROPIC_BASE_URL`、`ANTHROPIC_MODEL`。
`--fake` 走 `FakeAgentRuntime`：按脚本吐事件，用来验 UI 和事件流，不花 token。

---

## 聊天组件（`kit/ui/agent/`）

气泡 + **过程折叠**（工具调用折成一行「调用了 N 个工具」，展开看每一步）+ **审批卡**
（允许一次 / 本任务免确认 / 拒绝），都在 kit 里，app 直接用：

```kotlin
val log = remember { ChatLog() }
val messages by log.messages.collectAsState()

ChatMessageList(messages)
log.user(text)
session.send(text).collect { log.onEvent(it) }
```

交互参照了手机上的开源 agent 项目 [dsh-Go](https://github.com/ccch1mneyyy/dsh-Go)（DeepSeek Harness 的 Go 版），
但**代码全部自己写**：dsh-Go 是 **AGPL-3.0**，本仓库是 MIT，抄代码会传染许可证；
它的 README 也明确不许在改版里用它的名字和吉祥物。同类的 [mobile-mcp](https://github.com/QAQADHD/mobile-mcp)（Apache-2.0）、
[Mobile-Agent](https://github.com/Xiaomi-Partner-Organization/mobile-agent)（MIT）、
[Open-AutoGLM](https://github.com/zai-org/Open-AutoGLM)（Apache-2.0）许可证更宽松，值得直接看。

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
