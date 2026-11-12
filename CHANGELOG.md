# 更新日志

本项目遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号用 [语义化版本](https://semver.org/lang/zh-CN/)。

## [未发布]

### 新增
- **kit 新主题 Notion（文档风格）**：`ThemeStyle.Notion` + `notionThemeTokens(isDark)` —— 米色纸面 `#f7f6f3` + 墨色 `#37352f` + 1px 发丝线 `#e9e7e4`，圆角一律 4px、零渐变零重阴影、150ms ease-out 只变色。规范来自 StyleKit `notion-style`
- **kit `ui/theme/ColorNotion.kt` / `TypeNotion.kt` / `MotionNotion.kt`**：规范只给了浅色，深色版是自建的语义反转（墨底 + 纸字）；悬停灰 `#efedea` / 按下灰 `#e3e1db` 分别落在 `surfaceVariant` / `surfaceContainerHighest` 两个槽位上
- **kit `ui/components/NotionComponents.kt`**：规范里的四个角色 —— `NotionBlockRow`（悬停/聚焦淡入拖拽手柄 ⋮⋮，Drag Handle Illusion）、`NotionGhostButton`（透明底、悬停 `#efedea` / 按下 `#e3e1db`）、`NotionInlineInput`（聚焦只有一条 1px 底线，无外圈）、`NotionCallout`（淡底 + 4px 圆角 + 左侧图标槽）
- **kit `SettingsScreen`**：主题风格选项改为按 `ThemeStyle.entries` 生成 —— 手动维护 `listOf(...)` 早晚跟枚举脱节，一脱节 `selectedIndex` 就指错人
- **kit 新主题 电子纸（E-Ink）**：`ThemeStyle.EInk` + `eInkThemeTokens(isDark)` —— 暖纸底 `#f5f3ea` + 墨色 `#20211e` + 三档灰阶（`#555750` / `#bab9b0` / `#e6e4da`），零色相、全直角、零阴影、180ms ease-out 只变色，数值与单位走等宽字样。规范来自 StyleKit `e-ink`
- **kit `ui/theme/ColorEInk.kt` / `TypeEInk.kt` / `MotionEInk.kt`**：规范只给了浅色，深色版是自建的语义反转（墨底 + 纸字）；状态一律灰阶 —— 错误和警告不另发明色相，状态由形状 + 文字扛
- **kit `ui/components/EInkComponents.kt`**：规范里的角色补齐 —— `EInkInkAction`（墨面主操作，44dp 高、悬停转面板灰、聚焦翻成纸底墨字 + 2px 墨环）、`EInkPaperField`（纸面输入，可见 label + 聚焦 2px 墨环）、`EInkReadingRow`（1px 栏线分隔，等宽序号与数值带单位，选中 = 左侧墨线 + 「已选」文字，窄屏 < 360dp 换行到第二行）、`EInkPanel`（信息面板，纸底 + 1px 栏线 + 0 elevation）、`EInkStatusMarker`（实心/空心方块 + 状态词）、`Modifier.eInkGrain()`（5dp 一格的装饰性网点底纹，一次 drawPoints 画完）
- **kit `ui/components/StatusMark.kt`**：通用实心/空心方块标记 —— 状态由形状扛，颜色只是灰阶底色
- **kit 电子纸的错误/危险态不再靠色相**：`SimpleTextField` 错误提示前加空心方块标记，`DeleteButton` 在电子纸下改成纸底 + 墨框（不用红色实心）
- **kit 主题 macOS 毛玻璃（vibrancy）**：`ThemeStyle.MacOS` + `macOSThemeTokens(isDark)` —— 三档灰 `#1c1c1e → #2c2c2e → #3a3a3c` 定层次、系统蓝 `#0a84ff` 是唯一强调色、衬线标题 + 无衬线正文 + 等宽代码、圆角 6/10/14、200ms ease-out **只改颜色**（不位移不缩放）。规范来自 StyleKit `macos-vibrancy`
- **kit `ui/theme/ColorMacOS.kt` / `TypeMacOS.kt` / `MotionMacOS.kt`**：深色是规范原生契约；浅色是自建的 macOS 浅色系统（规范 readiness 标注 darkMode missing）
- **kit `ui/components/MacOSComponents.kt`**：规范里的角色 —— `MacOSVibrancyPanel`（半透明毛玻璃层 + 1px 白边 + 14px 圆角）、`MacOSVibrancySidebar`（240px 宽 + 12px 内边距 + 只有右边一条 1px 白 10% 边）、`MacOSListRow`（44dp 行高、悬停白 6% / 选中系统蓝 18% / 2px accent 焦点环）、`MacOSToolbar`（52dp、红黄绿交通灯、居中 13px 衬线标题）、`MacOSSegmentedControl`（轨道白 6%、选中段浅灰）、`MacOSSystemButton`（系统蓝实心 + 6px 圆角 + 悬停亮 8% / 按下暗 10% / 禁用 12% 白底）
- **kit 共享组件跟着 macOS 规范收口**：禁渐变发光（`aiGlassSurface` / `AvatarHero` 改平涂）、描边一律 1px（`themedStrokeWidth()` 收 `TagText` / `FilterChip` / 头像圈）、只改颜色不位移不缩放（`M3Motion.fadeThrough()` 去 scale、浮层只淡入淡出、`rememberBreathingAlpha()` 静态化、收藏星标不回弹）、按钮与输入框 6px 圆角（`themedControlShape()`）、卡片补 1px 白 8% 描边、输入聚焦边框改白 25%
- **kit 新主题 Swiss（瑞士国际风格）**：`ThemeStyle.Swiss` + `swissThemeTokens(isDark)` —— 纯白画布 / 纯黑文字 / 信号红强调，Helvetica 系无衬线（只 400、700 两档），字号锁 11/13/15/20/32/56/88，全直角、零阴影零渐变、150ms ease-out 只变色。规范来自 StyleKit `swiss-style`
- **kit `ui/components/SwissComponents.kt`**：规范里的四个角色 —— `SwissSectionLabel`（序号 + 24dp 黑线 + 大写标题）、`SwissStructuralRow`（64dp 行高、左侧 2px 栏线、悬停栏线变红 + 底色变 `#f0f0f0`）、`SwissInlineButton`（无填充无边框、13/700 大写、2px 下划线、带箭头）、`SwissColorField`（红/蓝/黄整块分区，32/700 左对齐）
- **kit `ui/components/SwissLayout.kt`**：规范「Layout」段的骨架 —— `SwissPage`（封顶 1200px、左对齐）、`SwissGrid`（十二栏 + 24px 栏间距，窄屏自动竖排）、`SwissRule`（整幅 1px 栏线）、`SwissSection`（96px 分区间距 + 1px 栏线）
- **kit 共享组件跟着 Swiss 规范收口**（切换主题后不该是「半套主题」）：卡片容器透明化（规范「Do not wrap content in cards」）、装饰性圆形改直角（`themedCircleShape()`）、字重只留 400/700（`themedFontWeight()`）、文字与容器一律左对齐（`themedTextAlign()` / `themedHorizontalAlignment()`）、禁用态降对比但保持可读（`themedDisabledContentColor()`，不用 M3 默认的 38% 透明）、头像区禁渐变、收藏星标不回弹、头像堆叠展开不缩放
- **kit `themedElevation()`**：瑞士风格规范写死「Elevation: None」，该主题下卡片 / FAB / 浮层 / 头像的 elevation 统一归零
- **`kit-membership` 模块（付费 / 会员框架，可选）**：档位表（`PlanCatalog` / `Plan`）+ 权益判定 + 额度引擎（按天 / 总量，`UsageTracker`）+ 会员状态落盘（`MembershipStore`）+ 付费墙与升级页 UI（`MembershipPaywall` / `UpgradeSheet`）。平台无关 —— 支付通道走 `BillingProvider` 接口，各端 app 自己接 Play Billing / StoreKit
- **`MembershipManager` 门面**：业务只调 `has()` / `check()` / `consume()` / `purchase()` / `restore()`；会员资格只认**已验过签**的购买，过期 / 没验签一律按免费档算
- **kit `ui/agent/`：聊天组件**（参照 dsh-Go 的交互，代码自己写）—— `ChatMessage`（消息 + 工具步骤模型）、`ChatMessageList`（气泡 + **过程折叠**：工具调用折成一行「调用了 N 个工具」，展开看每一步的参数与结果）、`ToolApprovalCard`（审批卡：工具名 + 入参 + 风险 + 三个按钮）
- **kit-agent-koog `ChatLog.kt`**：agent 事件 → 消息列表的映射 —— app 只 `log.user(...)` / `log.onEvent(...)`，不用各写一套状态机
- **`ToolApproval` 分级审批**：`ToolApprover.approve` 由布尔改成三种结果 —— `Allow`（允许一次）/ `AllowAlways`（本任务免确认，工具名记进当前 session 的名单）/ `Deny`（拒绝，工具不执行、理由回给模型）
- **`ToolOutput`**：工具返回值类型 —— `ToolOutput.Text` 只带文本，`ToolOutput.TextWithImage` 还能把图带回模型（Anthropic 的 tool_result 允许 image 块，Koog 这条路是通的，工具回截图不用绕 vision 消息）
- **`AgentEvent.ToolDone` / `AgentEvent.ToolFailed`**：工具跑完 / 没跑成各有事件，UI 的过程折叠能标出"完成 / 失败"
- **`scripts/ci.sh`（门禁执行层）**：所有门禁逻辑的唯一出处 —— detekt / apiCheck / verify / 单测 / lint / 各端构建 / backend 自检。CI 和本地跑的是同一份脚本
- **`scripts/checks/`（fork 自定义门禁落点）**：放个 `.sh` 进去，`ci.sh verify` 自动跑 —— 不用改 `ci.sh`，也不用改 workflow，跟上游不冲突。附示例门禁 `no-hardcoded-secrets.sh`
- **`.github/actions/setup-gradle`（复合 action）**：JDK / Android SDK / gradlew 权限的 setup 只写一份，8 个 job 共用
- **`scripts/verify-i18n.py`**：values 和 values-en 的 key 必须一一对应（漏一个英文，界面那一格直接显示 key 名）
- **kit-agent-koog `SkillContract.kt`**：skill 的静态体检 —— `skill.violations()` 查空字段、工具重名、工具名不是 snake_case、limit 非正
- **kit-agent-koog `SkillSamples.kt`**：黄金样例 —— `skill.runSamples(...)` 用「工具名 + 入参 JSON + 期望返回」钉住工具行为，不调模型、不花钱、不连网
- **`scripts/templates/app/` 补单测骨架**：新 app 生成出来就带一条壳自检（strings / manifest 对账）和 `values-en/strings.xml`
- **`build-logic/` 约定插件 `androidkit.android.app`**：所有 `apps/*` 共用 SDK 版本 / versionName / 签名 / R8 / 测试参数 / detekt 配置，app 的 build 文件缩到「namespace + applicationId + BuildConfig + 依赖」
- **`scripts/new_app.py` + `scripts/templates/app/`**：从模板生成最小可编译 app 壳（manifest / MainActivity / 主题 / strings / build.gradle.kts），自动套 `AppShell` + `AppTheme`
- **apps/demo 迁到约定插件**：build.gradle.kts 从 160 行降到 ~100 行，签名/release/detekt 上移
- **kit-agent-koog `GatewayAgentRuntime` + `AgentMode`**：后端模式落地 —— Ktor + 手拆 SSE，按 `skill.id` 调 `/v1/skills/<id>/invoke`，自动带 `sessionId` 续接；`createAgentRuntime(Local / Gateway)` 切模式，UI 不用改
- **网关协议单独测试**：SSE 解析（`GatewaySseTest`）+ 假传输层（`GatewayAgentRuntimeTest`），不引 `ktor-client-mock`
- **kit `ui/theme/ThemeTokens.kt`**：主题扩展点 —— `AppTheme(tokens)` 让 App 用自己模块里的品牌 tokens 装配主题（颜色/扩展语义色/排版/形状/圆角/标签色板），**不用改 kit**；`themedCorner` 与 `tagColor` 从「按 ThemeStyle 分发」改成 token 驱动
- **kit `monoThemeTokens()` / `swissThemeTokens()` 等**：底座预设变成工厂函数，可拿去做品牌 tokens 的基底
- **apps/demo `theme/DemoBrand.kt`**：品牌主题的可编译样板（拷到自己 app 改色即可）
- **kit `ui/shell/AppShell.kt`**：页面外壳 —— 系统栏 / 键盘 / 键盘弹起收底栏 / Snackbar 宿主一次写对，业务页不再各写一套 `windowInsetsPadding`
- **kit `ui/state/UiState.kt`**：底座统一的异步状态（Idle / Loading / Success / Error）+ `map` / `dataOrNull` / `uiStateOf`（取消异常原样抛）
- **kit-agent-koog `FakeAgentRuntime`**：脚本化假运行时（文本分块、工具调用事件、失败模拟），用于预览 / 无 key 演示 / UI 单测
- **chat-demo `--fake`**：不联网、不需要 key 跑通「UI → 事件流」，`--check --fake` 可进 CI
- **apps/demo 聊天页改用 `AppShell`**：删掉手写的 inset 与键盘判断
- **`skills/android-skills` 改为全量镜像**：Google 官方 [android/skills](https://github.com/android/skills) 完整 18 个分类（352 文件 / 3.6 MB，含 LICENSE），不再只带 AGP / Compose 四份。新增 camera、device-ai、navigation、performance、play、profilers、security、system、testing、tv、wear、xr 等
- **kit `ui/navigation/NavTransitions.kt`**：页面级 M3 转场（Fade Through / Shared Axis X）。原来的转场只到组件级，整页切换是硬切
- **`M3Motion` 补全 M3 令牌**：`FADE_THROUGH_IN`（210ms）、`FADE_THROUGH_INITIAL_SCALE`（0.92）、`SHARED_AXIS_OFFSET`（30dp）、`pagerScroll()`、`expressiveSpatial()`（庆祝回弹，damping 0.6 / stiffness 800）
- **apps/demo**：设置页 ↔ 聊天页改用 `AnimatedContent` + Fade Through（原为 `if/else` 硬切）
- **kit `data/update/`**：自动发现新版本 —— 语义化版本比较、静态 JSON 清单与 GitHub Releases 两种来源、检查节流与“跳过此版本”落盘、`UpdateDialog` 提示弹窗（强制更新不可关闭）
- **apps/demo**：启动时自动检查更新（`UpdateGate`）；清单 URL / GitHub 仓库走构建配置，默认读本仓库 Releases
- **apps/demo 桌面长按菜单**（Android App Shortcuts）：静态声明「新建待办 / 问助手 / 设置」，装完即可用；点开直接落到对应页面（新建待办预填草稿并弹键盘）；`singleTop` 复用同一实例，热启动走 `onNewIntent`；上报使用情况参与桌面排序。`shortcuts.xml` 与 `AppShortcut` 枚举的漂移由单元测试拦住

### 变更
- **位图（Bitmap）主题整块移除**（**破坏性**，fork 要改）：`ThemeStyle.Bitmap` 枚举值和 `bitmapThemeTokens()` 删除；`ColorBitmap.kt` / `TypeBitmap.kt` / `PixelMotion.kt`、六个像素组件（`DotMatrixNumber` / `DotMatrixBarChart` / `PixelDissolve` / `PixelProgressBar` / `Modifier.pixelDitherHover()` / `TypewriterText`）和 7.3MB 的 fusion-pixel 像素字体一并删掉。形状 token 化保留，Swiss / Notion / E-Ink / macOS 主题不受影响 —— 选过位图风格的 App 改回 Mono 即可
- **工具返回值从 `String` 改成 `ToolOutput`**（**破坏性**，fork 要改）：`doExecute` 现在返回 `ToolOutput.Text(...)`；想要带图就用 `ToolOutput.TextWithImage(text, ToolImage(...))`
- **`ToolApprover.approve` 从 `Boolean` 改成 `ToolApproval`**（**破坏性**，fork 要改）：`ToolApproval.Allow` / `AllowAlways` / `Deny`；原来写 `{ _, _ -> true }` 的改成 `{ _, _ -> ToolApproval.Allow }`
- **`kit-agent-koog` 依赖 `:kit`**（`api(project(":kit"))`）：聊天消息模型和 UI 在 kit，事件映射在 agent 层 —— 这层依赖 kit 才成立
- **`kit/api` / `kit-agent-koog/api` 基线重新生成**（聊天组件 + 新事件 + 新类型）
- **`.github/workflows/ci.yml` 改成薄壳**：每个 job 只说「在什么环境跑哪道门禁」，逻辑全在 `scripts/ci.sh` —— fork 要加/调门禁改脚本，不动底座路径的 workflow
- **`scripts/install-kit.sh` 跟着拷门禁资产**：`ci.sh` / `verify-i18n.py` / `checks/` / `.github/actions/setup-gradle` —— 少了它们，消费方项目里那份 ci.yml 的 `run:` 全部落空
- **`kit/api` / `kit-agent-koog/api` 基线重新生成**：补上工作树里已存在但基线没跟上的公开 API（设置、更新检查、导航转场、设置页、AppShell/UiState 等），否则 `apiCheck` 会红
- **`M3Motion` spatial spring 阻尼系数 0.8 → 0.9**：与 M3 / AndroidX `MotionScheme.standard()` 的 standard spatial 规范一致。0.8 会让所有位移带可见过冲
- **`M3Motion.fadeThrough()` 补上进场缩放**：原来只有淡入，缺了 M3 规定的 92% 缩放，看不出“换了一层”
- **`StarFavoriteButton` 去掉手写 spring**：改用 `M3Motion.expressiveSpatial()`，组件层不再有裸动画参数
- **`rules/COMPONENT_SPEC.md` §11 重写**：删掉代码里并不存在的 `AppAnimations`（iOS 味 spring + `FastOutSlowInEasing`），改为 M3 令牌表，并把选型表统一指向 `GOOGLE_STYLE.md`（两处维护必然漂移）
- **`rules/GOOGLE_STYLE.md` 动效选型表**：补「整页替换」「层级推进」「接 navigation 库」「Pager 翻页」「庆祝回弹」五行

## [0.1.0] - 2026-11-08

底座模板的第一个可用版本。

### 新增
- **kit**：Compose Multiplatform 组件库（Android / 桌面 / iOS 三端），含 AI 聊天、Markdown 编辑器、待办等组件
- **kit-agent-koog**：基于 [Koog](https://github.com/JetBrains/koog) 的 agent 运行时，skill 即逻辑，工具走白名单
- **apps/demo**：示例 app（待办助手），含 Android 壳工程和 iOS 壳工程（SwiftUI）
- **backend**：agent 网关，SSE 流式返回，skill 热加载
- **CI**：`.github/workflows/ci.yml`（构建 / 测试 / lint / 静态检查 / 网关冒烟）、`release.yml`（APK / deb / dmg / iOS framework）
- **质量门禁**：detekt 规则集（`config/detekt/`）、`scripts/verify-kit.py`、`scripts/verify-skills.py`
- **工程基线**：MIT 许可证、`SECURITY.md`、`CONTRIBUTING.md`、`CODEOWNERS`、PR 模板、Dependabot、依赖校验（verification-metadata）

### 已知限制
- demo 把 API key 打进 APK，**只能用于内部测试**；正式上架要换成服务端代理
- 网关的 session 归属表在内存里，重启就丢；要跨重启 resume 得落盘
- 依赖校验基线在 Windows 上生成，只覆盖 JVM / Android 产物（`org.gradle.dependency.verification=lenient`）
- `apiCheck` 只校验 JVM（desktop）那一份 API；klib 那份要 macOS 才编得出来
