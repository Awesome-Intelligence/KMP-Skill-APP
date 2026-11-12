# Kit 说明 —— 边界在哪、为什么这么取舍

这份文档给**接手 kit 的人**看：知道边界在哪，比知道里面有什么更重要。

---

## 〇、KMP 化（2026-10-31）

kit 现在是 **Compose Multiplatform 工程**：`:kit`（KMP 库）+ `apps/`（各 app 的壳工程，`apps/demo` 是示例）。
目标端 = Android + iOS + Desktop（**不做 Web/wasm**）。

### 搬进来的公共层

公共层只有**纯 UI + 纯逻辑**，代码在 `kit/src/commonMain/kotlin/com/androidkit/app/`：

| 位置 | 内容 |
|---|---|
| `ui/theme/` | Mono 暖中性色板、Type、Motion、TagColors、ExtendedColors |
| `ui/components/` | 通用组件（原 41 个，剔掉领域耦合的） |
| `ui/templates/` | `ListScreen` `FormScreen` |
| `ui/preview/DemoData.kt` | 精简版预览数据 |
| `data/storage/` | okio 版存储引擎 + 可插拔格式 codec（替掉 `java.io.File`） |
| `data/agent/` | agent 网关客户端（Ktor + kotlinx.serialization，SSE）——2026-11-02 起**取代 `data/ai/`**，见「〇.b」；**已在 〇.c / 〇.f 全部删掉**（agent 契约搬进 `:kit-agent-koog`） |

### 依赖替换表

| 原来（Android 专属） | 换成（KMP） | 备注 |
|---|---|---|
| `androidx.compose.*` | `org.jetbrains.compose` 插件 | **包名不变**，import 基本不动 |
| `androidx.compose.ui.res.stringResource` + `R.string.x` | `org.jetbrains.compose.resources.stringResource` + `Res.string.x` | 文案搬到 `composeResources/` |
| `androidx.compose.ui.tooling.preview.Preview` | `androidx.compose.ui.tooling.preview.Preview`（CMP 版 artifact） | 注解类位置没变，只是 artifact 换源 |
| Coil 2 | Coil 3 | `coil3.compose.AsyncImage`，`ImageRequest.Builder` 要 `PlatformContext` |
| Retrofit + OkHttp + Gson | Ktor 3 + kotlinx.serialization | 引擎按平台挂 |
| `java.time.*` | kotlinx-datetime + `KitDateFormat` | 见下 |
| `java.io.File` | okio `FileSystem.SYSTEM` | |
| `android.net.Uri` | `String?` | |
| Timber 5.0.1 | `println` | **Timber 没有 KMP 产物** |
| `dev.jeziellago:compose-markdown` | 纯文本渲染 | KMP 版依赖重、样式不可控；要富文本再上 `com.mikepenz:multiplatform-markdown-renderer` |
| `material3.adaptive` + `androidx.window` | `BoxWithConstraints` 自算宽度 | |
| `palette-ktx` | 调用方传入主色 | |
| `billing`（Play Billing） | 删除 | 公共层不留计费 |
| `richeditor` | 删除 | MarkdownEditor 本来就是 BasicTextField |
| `dev.jeziellago` 的 `Md`（markdown 渲染） | 纯 `Text` | 同上 |

### 平台下沉（`androidMain`）

- `SpeechInput.kt`（RecognizerIntent + 运行时权限）
- `SafStorageEngine.kt`（SAF / DocumentFile）

### 明确不做的

- **不引入 expect/actual 平台接口**（用户拍板）—— 平台能力由各端项目自己实现
- 不做 Web/wasm
- 不搬业务架构（Repository / ViewModel / 导航）

### 三个容易踩的坑

1. **iOS 目标只能在 macOS host 编**。`kit/build.gradle.kts` 里按 `hostIsMac` 门控声明 iOS target，
   Windows 本地不会声明，也就不会被 Kotlin/Native 工具链绊倒。CI 的 macOS job 负责验 iOS。
   framework 的 `baseName = "Kit"` —— Xcode 里 import 的名字就是这个。

2. **compileSdk 必须 37.1**。CMP 的 android 产物要求 37.1+，
   AGP 9 的 KMP 库插件里 `compileSdkMinor` 属性不存在，要用新 DSL：
   ```kotlin
   android {
       compileSdk { version = release(37) { minorApiLevel = 1 } }
   }
   ```
   `apps/*/build.gradle.kts`（`com.android.application`）那边 `compileSdkMinor = 1` 直接写就行。

3. **Aliyun 镜像没同步 CMP 的 `-android` 产物**。`settings.gradle.kts` 里给
   `org.jetbrains.compose*` 组单独挂了一条 `mavenCentral()`（带 content filter），
   否则解析会掉进镜像的半截 metadata。

---

## 〇.b Agent 化（2026-11-02）

**`data/ai/` 整个删了**（Provider 抽象、场景路由、PromptManager 模板 —— 11 个文件）。

为什么：kit 的定位变了 —— AI 逻辑整体挪到 **agent 后端**（根目录 `backend/`，见
`ARCHITECTURE.md`）。prompt 就是业务逻辑，放服务端的 SKILL.md 里；kit 只留一个
**网关客户端** `data/agent/`：

| 文件 | 内容 |
|---|---|
| `AgentClient.kt` | 接口：`skills()` / `invoke(skillId, input, sessionId?)` |
| `AgentEvent.kt` | SSE 事件 sealed class（start/text/tool/result/error） |
| `KtorAgentClient.kt` | Ktor 实现 + 手拆 SSE（零新依赖） |

旧 `AiException` 的「`*Default` 消息」惯例保留在了 `AgentException` 上。

**对业务项目的含义**：原来在各 app 里填 `PromptManager` 模板、配 `AiProviderRegistry`
的工作，现在变成在 backend 写 `skills/<id>/SKILL.md`（契约 + prompt）。
`data/ai/repository/*`（依赖 Goal/QuestionCard 的那部分）随之作废，不用再惦记第五节那条。

---

## 〇.c Agent 本地化（2026-11-02，推翻 〇.b 的客户端部分）

> ⚠️ 本节里的 `LocalAgentClient` / `AnthropicApi` **已在 〇.f 删除**，本节保留作决策记录。

方向反转：**agent loop 跑在手机上**（`data/agent/LocalAgentClient`），模型调用直连
Anthropic API，工具在本机执行 —— 数据不出端。原因：用户要 app 客户端自带 agent 能力，
而不是依赖云端网关。

〇.b 建的网关客户端（`AgentClient` 接口 / `KtorAgentClient` / SSE 解析）已删。
`backend/` 网关还在（Claude Agent SDK 那套），降级为**可选**：服务端重技能 / 集中管控时才用。

| 文件 | 内容 |
|---|---|
| `LocalAgentClient.kt` | agent loop + `AgentSession`（对话历史在端上） |
| `AgentRuntime.kt` | 引擎契约：`session(skill, approver): AgentSession`（两个引擎都实现它） |
| `AnthropicApi.kt` | `/v1/messages` Ktor 客户端 + DTO + JSON Schema 小工具 |
| `SkillDefinition.kt` | skill = systemPrompt + `AgentTool` 列表（Kotlin 代码，不再是 SKILL.md） |
| `AgentEvent.kt` | Text / ToolUse / Result / Error（进程内，不序列化） |

注意：官方 Anthropic Java SDK 是 JVM 专属，进不了 commonMain（iOS 编不了），
所以这里手写 Ktor 客户端。遇到新内容块类型（开 thinking 后会有）要给 `ContentBlock` 补子类。

---

## 〇.d Koog 引擎（2026-11-02）

> ⚠️ 本节描述的是"两个引擎并存"阶段。**薄引擎已在 〇.f 删除**，`KoogTool.kt` 也删了，
> 本节保留作决策记录。

`:kit-agent-koog` —— 用 Koog（JetBrains 的 KMP agent 框架，1.3.0）跑 agent loop 的第二个
`AgentRuntime` 实现。自己的薄引擎只够"模型→工具→模型"，Koog 有一整套现成能力（事件钩子、
子图、缓存、多 agent）。用户明确接受为它多背依赖。**独立模块，app 默认不引**：
`implementation(project(":kit-agent-koog"))` 才生效。

| 文件 | 内容 |
|---|---|
| `KoogAgentRuntime.kt` | Koog `AIAgent` + Anthropic 客户端；每轮 send 起一个 agent，prompt 带历史，跑完把新消息收回历史 |
| `KoogTool.kt` | `AgentTool` → Koog 工具：JSON Schema 转 descriptor、入参出参原样透传、审批闸门 |

踩过的坑（改这块前先看）：

- **HITL 只能写在工具的 `execute` 里**。Koog 的事件钩子（`onLLMCallCompleted` 等）只能通知，
  不能阻塞，所以拦不住工具执行。拒绝走 `ToolException.ValidationFailure`，模型收到的是
  带 `isError` 的工具结果，会自己改道。
- **Koog 给 Anthropic 的 `input_schema` 是从 `ToolDescriptor` 生成的**，不是原始 JSON Schema。
  kit 的 `AgentTool.inputSchema` 必须转成 `ToolParameterDescriptor` + `ToolParameterType`
  （`enum`/`array`/`object` 都有对应类型；认不出来的类型一律当 string）。
- **Koog 的模型表里没有 `claude-opus-5-5`**（只有 `claude-opus-5`）。`resolveModel()` 找不到就
  就地 `AnthropicModels.addCustomModel()` 登记一个，并把 `modelVersionsMap[model] = model.id`。
  登记时的上下文/输出上限**抄同族模型**（逐级剥版本后缀找最近的已知 id：
  `claude-opus-5-5` → `claude-opus-5` → 1M 上下文 / 128K 输出），
  别写死 200K/64K 那套老数字 —— 声明小了 Koog 会提前截历史，声明大了被 API 拒。
  另外 Opus 5.x 已经从 API 里删掉 `temperature`/`top_p`（发过去 400），
  所以登记时把 `LLMCapability.Temperature` 去掉；但 `Thinking` 必须留着 ——
  Opus 5.5 的 adaptive thinking 响应里一定带 thinking 块，Koog 要靠这个能力位解析并原样回传。
- **多轮靠 prompt 里带历史**：每轮新起 agent，prompt = system + 历史，跑完从
  `session.context().llm.prompt.messages` 把新消息收回历史。
- `baseUrl` 可配 → 上架场景的转发代理方案（key 留在代理上）照旧成立。

---

## 〇.e 存储解耦（2026-11-02）

原来存储层写死 markdown：`MarkdownStore` 的签名就是
`write(subdir, entityId, frontmatter: Map<String, String>, content: String)` ——
格式、字段类型、存储位置全焊在一张接口上。现在拆成**引擎**和**格式**两层：

| 文件 | 内容 |
|---|---|
| `EntityStore.kt` | 存储抽象（不绑格式）：`write/read/delete/listIds/loadAll` + 二进制附件 + `imageUri` |
| `StoreCodec.kt` | 格式抽象：`extension` + `encode`/`decode` |
| `MarkdownCodec.kt` | frontmatter + 正文（默认，兼容老数据） |
| `JsonCodec.kt` | 一个实体一个 `.json` |
| `Fields.kt` | 取值 helper（`str/intOr/doubleOr/boolOr/listOrEmpty/archivedFlagOr`…） |
| `FileStorageEngine.kt` | okio 引擎，构造参数带 codec（默认 `MarkdownCodec()`） |
| `SafStorageEngine.kt` | Android SAF 引擎（原 `MarkdownStorageEngine`，改名） |

要点：

- **字段类型化**：`Map<String, JsonElement>`，数字/布尔/数组/嵌套对象是一等公民，
  不用再塞进 `Map<String, String>` 里靠 `intOr` 猜类型。
- **老数据不用迁移**：老 md 里值全是字符串，新的取值 helper 宽松转换（`"0.65"` → `0.65`）。
- **换存储**（比如上 SQLDelight）= 实现 `EntityStore`，调用方一行不改；
  **换格式** = 实现 `StoreCodec`，引擎一行不改。
- `kotlinx-serialization-json` 从 `implementation` 升成 `api`（公开签名里有 `JsonElement`）。
- **破坏性变更**：`MarkdownStore` / `MarkdownParser` / `MarkdownStorageEngine` 已删。

---

## 〇.f 只有 Koog 一套（2026-11-02，取代 〇.c / 〇.d 的引擎部分）

用户拍板：「我都有 koog 了，我为啥还需要薄引擎？」→ **薄引擎整条删掉，agent 运行时只有 Koog**。
架构变成 **前端（Compose）+ skill（SkillDefinition）+ Koog（agent runtime）**，三层都在手机 app 里，
只有模型调用出网。同时选 **B 方案（干干净净）**：skill 层直接绑 Koog，不留任何转换/适配层。

### 删了什么

| 删掉 | 原因 |
|---|---|
| `kit/.../data/agent/LocalAgentClient.kt` | 有 Koog 就不需要第二套 loop |
| `kit/.../data/agent/AnthropicApi.kt` | Koog 自带 Anthropic 客户端（Ktor 引擎） |
| `kit-agent-koog/.../koog/KoogTool.kt` | JSON Schema → Koog descriptor 的转换层，没必要 |
| `kit/` 里的 `data/agent/` 整包 | agent 契约整体搬进 `:kit-agent-koog` |

`:kit` 现在**不含任何 agent 代码**（只剩 UI 组件 + `data/storage/`），Ktor 只为 Coil 留着。

### 现在的形状

| 文件 | 内容 |
|---|---|
| `AgentRuntime.kt` | 契约：`session(skill, approver): AgentSession`、`ToolApprover` 闸门 |
| `SkillDefinition.kt` | skill = systemPrompt + 工具集 + 模型/maxTurns |
| `SkillTool.kt` | 工具基类：继承 Koog `ToolBase`，入参 `@Serializable` 数据类，**schema 自动生成** |
| `KoogAgentRuntime.kt` | `AIAgent` + Anthropic 客户端 + `ChatMemory` + 事件 → `AgentEvent` |
| `StreamingExecutor.kt` | 包 executor：`execute()` 走 `executeStreaming()`，文本增量逐块回调 |
| `AgentEvent.kt` | Text（**增量块**，UI 追加）/ ToolUse / Result / Error |

写工具不用再写 JSON Schema：

```kotlin
@Serializable
data class SearchArgs(val query: String, val limit: Int = 10)

class SearchTool(private val store: Store) : SkillTool<SearchArgs>(
    typeToken<SearchArgs>(), "search", "搜索本地数据",
) {
    override suspend fun doExecute(args: SearchArgs): String = store.search(args.query, args.limit)
}
```

### Koog 的坑（改这块前先看）

- **多轮历史不在 session 上**。Koog 默认策略每次 `session.run()` 都是干净的一次性对话，
  历史只活在**一次 run 内部**（工具循环）。跨轮历史的官方答案是 **ChatMemory feature**
  （`ai.koog:agents-features-memory`，`install(ChatMemory.Feature) { windowSize(n) }`），
  它按 `AIAgentContext.runId` 存取 —— runId 就是 run session 的 id，所以同一个
  `agent.createSession()` 天然共享历史。默认 provider 是内存的，app 退出即失忆。
- **`chatAgentStrategy()` 不适合做"一轮一问"**：它设计成在一次 run 里自己循环到底
  （实测每轮把 LLM 打到 maxIterations），要一次一答就用默认策略 + ChatMemory。
- **`PromptExecutor` 的方法全是 final**，不能拿现成 executor 改写；子类必须自己实现
  `execute(prompt, model, tools)` / `executeStreaming(...)` / `moderate(...)` / `close()`。
  agent 走的是 `execute`（内部经 `executeProcessed`）。
- **Koog 的 `TypeToken` 不是泛型**：`typeToken<A>()` 的类型是 `TypeToken`（无 `<A>`）。
- **`ToolException.ValidationFailure` 会被 Koog 转成 `ToolResultKind.ValidationError`**，
  消息回到模型 → 这就是 HITL 拒绝后模型能改道的原因。HITL 闸门必须写在工具 `execute` 里
  （事件钩子是 suspend 的，拦不住执行）。
- **`Message.Assistant(...)` 的 `metaInfo` 不能传 null**（用 `ResponseMetaInfo.Empty`）；
  `Message.User` 的内容在 `.parts`，不是 `.content`。
- **Koog 的模型表里没有 `claude-opus-5-5`**（只有 `claude-opus-5`）。`resolveModel()` 找不到就
  就地 `AnthropicModels.addCustomModel()` 登记一个，并把 `modelVersionsMap[model] = model.id`。
  登记时的上下文/输出上限**抄同族模型**（逐级剥版本后缀找最近的已知 id：
  `claude-opus-5-5` → `claude-opus-5` → 1M 上下文 / 128K 输出），
  别写死 200K/64K 那套老数字 —— 声明小了 Koog 会提前截历史，声明大了被 API 拒。
  另外 Opus 5.x 已经从 API 里删掉 `temperature`/`top_p`（发过去 400），
  所以登记时把 `LLMCapability.Temperature` 去掉；但 `Thinking` 必须留着 ——
  Opus 5.5 的 adaptive thinking 响应里一定带 thinking 块，Koog 要靠这个能力位解析并原样回传。
- `baseUrl` 可配 → 上架场景的转发代理方案（key 留在代理上）照旧成立。

### 还没做的

- 逐 token 流式（Koog 有 `requestLLMStreaming`，接上就能打字机）
- 对话历史的持久化（`ChatHistoryProvider` 自己实现）
- ~~app 里接一个真 skill，让这套 loop 第一次在设备上跑起来~~ —— `apps/demo/` 已经做了（待办助手）

---

## 〇.g 位图（Bitmap）主题 —— 已移除（2026-11-12）

曾经预置过一套「位图 / 1-bit」风格（冷灰白 + 橙强调、fusion-pixel 像素字体、全直角、按帧跳动效），
连同六个像素组件和 7.3MB 的像素字体。**2026-11-12 整块移除**：`ThemeStyle.Bitmap` 枚举值、
`bitmapThemeTokens()`、`ColorBitmap.kt` / `TypeBitmap.kt` / `PixelMotion.kt`、六个像素组件
（`DotMatrixNumber` / `DotMatrixBarChart` / `PixelDissolve` / `PixelProgressBar` /
`Modifier.pixelDitherHover()` / `TypewriterText`）和像素字体一并删掉 —— 选过位图风格的 fork 要改回 Mono。

**留下的**：形状 token 化（43 处硬编码 `RoundedCornerShape` → `MaterialTheme.shapes.*`）——
Mono 模式下像素级保留原样，Swiss / E-Ink 这类直角主题直接受益。这条没删。

## 〇.h 逻辑 api 化（2026-11-07）

底座只规定「能力边界」，业务逻辑归 app。但业务逻辑在 app 里有两个消费者：
**UI 的按钮** 和 **agent 的工具**。两边各写一份，行为就会分叉 ——
最常见的表现是「agent 删的待办和用户删的不一样」。

规矩只有一条：**UI 和 tool 调同一个函数**。

| 层 | 放什么 | 规矩 |
|---|---|---|
| data 层 | 业务动作 + 数据 | 普通类 / 普通函数（要挂起才 `suspend`）；不写 Compose 代码 |
| ui 层 | Composable | 只调 data 层，不直接改数据 |
| skill 层 | `SkillTool` 子类 | 薄包装：`doExecute` 单行透传，不写业务分支 |

配套三条：

- 动作函数返回**一句结果文案**（纯文本）—— tool 透传，UI 拿同一句去显示
- 同一份数据的文本格式只有一处（别让用户和模型看到两个格式）
- 危险动作（删 / 外发 / 覆盖）过 `ToolApprover` —— 工具一多最容易漏挂闸门

**刻意不做**：UI 状态类操作（开弹窗、切 tab、滚动、展开）不做成工具 ——
agent 是「本机能力」路线，不是 UI 自动化。工具粒度按「用户的一句自然话」切，不按按钮切
（每个按钮一个工具 = 模型选错工具 + prompt 膨胀；入参字段名还是对模型的公开契约）。

落点：

- 规范写在 `rules/CLAUDE.template.md` 的「逻辑 api 化」一节 ——
  `install-kit.sh` 会把它原样拷成 app 的 `CLAUDE.md`，所以**下游 app 天生带着这条要求**
- `apps/demo/` 已按它改造：`TodoStore` 是 data 层（校验、分支、结果文案都在那儿），
  三个 tool 单行透传，UI 和 agent 共用 `add` / `remove` / `format` —— 连结果文案都是同一句
- `scripts/verify-skills.py` 静态自检（**只警告，不挡构建**）：

  | # | 检查 |
  |---|---|
  | 1 | tool 类里有 Compose 代码（tool 跑在 agent loop 里，没有 UI 上下文） |
  | 2 | `doExecute` 里有 `if` / `when` / `for` / `while` / `try`（分支该下沉到 data 层） |
  | 3 | 工具名不是英文 snake_case（模型靠 name 挑工具） |

---

## 〇.i 自动发现新版本（2026-11-10）

需求：app 启动时自动知道有没有新版本，而不是等用户自己去商店看。

### 边界

“发现”是底座能力，“安装”不是。kit 只做 **拉清单 + 比版本 + 节流**，
不碰平台跳转（商店 / 浏览器 / PackageInstaller）—— 和“不写 expect/actual”是同一条线。

| 类型 | 职责 |
|---|---|
| `AppVersion` | 语义化版本解析/比较；脏数据返回 null 不抛 |
| `UpdateManifest` | 远端清单契约（latest / minSupported / downloadUrl / notes / publishedAt） |
| `UpdateSource` | 来源接缝：`HttpUpdateSource`（静态 JSON）/ `GitHubReleaseSource`（GitHub Releases） |
| `UpdateChecker` | 节流 → fetch → 比版本；返回 sealed `UpdateResult`，`check` 不抛异常 |
| `FileUpdateStateStore` | okio 落盘 `lastCheckedAtMillis` + `skippedVersion`（`UpdateStateStore` 接口方便测试） |
| `UpdateDialog` | 纯 UI：强制更新不可关；可选“稍后 / 跳过此版本” |

### 为什么这么切

- **currentVersion 是参数**：kit 没有 BuildConfig，三端版本号来源不同（Android `versionName`、
  iOS `CFBundleShortVersionString`、Desktop 打包版本）。让 App 传最省事，也避免 kit 碰平台 API。
- **HttpClient 由 App 传**：Ktor 引擎按平台挂（OkHttp / Darwin），kit 没法统一 new；
  `HttpUpdateSource` / `GitHubReleaseSource` 只接收 `HttpClient`。为此 kit 把 `ktor-client-core`
  从 implementation 升成 **api**（公开构造参数里有这个类型）。
- **两种来源**：静态清单能表达“最低支持版本”（强制更新），适合正式产品；
  GitHub Releases 零托管，适合开源项目，但没有强制更新字段。
- **失败也记检查时间**：否则离线用户每次冷启动都白等一次超时；demo 的 HttpClient 超时 5s/10s。
- **跳过按版本号记**：`skippedVersion` 只在等于远端最新版时生效；出现更新版本自动重新提示。
  手动检查（`force = true`）会忽略跳过记录。

### 刻意没做

- 后台定时轮询 / WorkManager：冷启动或回前台查一次足够；要后台就由 App 用自己的调度器调 `check`
- 应用内下载安装、增量更新、AB 分包：平台差异大，交给 App / 商店
- 多平台清单字段：一个清单只描述一个平台/渠道，三端各配各的 URL 更简单
- iOS / Desktop 壳的接线：数据层三端通用，但 demo 只接了 Android（`UpdateGate`）

### 安全

- 清单和下载地址必须 HTTPS —— 更新通道被篡改就能把用户引到恶意包
- `releaseNotes` 是不可信文本，`UpdateDialog` 只当纯文本渲染，不解析 HTML/Markdown
- 上架应用优先跳商店（Google Play / App Store）做校验，别让用户手动装来路不明的 APK

### 踩坑

- **GitHub 的 `releases/latest` 不返回 draft / prerelease**。`release.yml` 建的是 `draft: true`，
  发布前查不到，这是预期行为。
- **版本号不要用字符串比**：`"1.10.0" < "1.9.0"` 按字典序成立，按语义正好相反。
- **清单缓存**：放 CDN 时给 `latest.json` 配短缓存（如 `Cache-Control: max-age=300`），
  否则发版后可能几小时才生效。
- 公开仓库的 GitHub API 未登录限 60 次/小时/IP；6h 节流足够，高频检查要配 token。

---

## 〇.j 页面外壳与状态约定（2026-11-10）

模板要派生很多 app。如果每个 app 都重写一遍「系统栏 + 键盘 + 底栏 + Snackbar」，
它们必然各自漂移 —— 这正是底座要消灭的东西。所以补三样最小公共能力：

| 文件 | 内容 | 为什么放 kit |
|---|---|---|
| `ui/shell/AppShell.kt` | 页面外壳：safeDrawing / IME / 键盘弹起收底栏 / Snackbar 宿主 | 每个页面都要用；放 app 层等于每个 app 抄一遍 |
| `ui/state/UiState.kt` | Idle / Loading / Success / Error + `map` / `dataOrNull` / `uiStateOf` | 以前只有规则模板里写了 UiState，代码里没有，每个 app 会各造一个 |
| `kit-agent-koog/.../FakeAgentRuntime.kt` | 脚本化假运行时 | 预览、无 key 演示、UI 单测都靠它，且不用 mock Koog |

### 边界

- **AppShell 不做业务，也不做导航** —— 只负责「这一页怎么避开系统 UI」。
- **topBar / bottomBar 槽位不做系统 inset**：`Header` 自带状态栏内边距、`NavigationBar` 自带手势条内边距，
  壳再加一层就是双重避让，标题会掉下去一截。内容区由壳统一避让，页面里不要再写 `windowInsetsPadding`。
- **UiState 是 UI 状态**，不携带 repository/网络类型；`uiStateOf` 只包挂起调用，
  `CancellationException` 原样抛（吞掉会把「离开页面」变成「加载失败」）。
- **FakeAgentRuntime 不执行工具**：审批闸门挂在 `SkillTool.execute` 上，假运行时只发 `ToolUse` 事件给 UI 看。

### 踩坑

- `WindowInsets.isImeVisible` 是 **Android 专属 API**，kit 的 commonMain 编不过（desktop 直接 unresolved）。
  键盘判断用 `WindowInsets.ime.getBottom(density) > 0`，三端通用。
- 键盘弹起时**底栏要连槽位一起撤掉**；只隐藏内容、保留高度，空隙还在。
- `private companion object { const val X }` 里的常量在 JVM 上会变成 public static 字段漏进 API 基线。
  不想公开的常量放**顶层 private**。
- apps/demo 的聊天页已改成 `AppShell`：删掉了手写的 `windowInsetsPadding` + `isImeVisible` 判断，
  底栏收放交给 `hideBottomBarWhenImeVisible`。

---

## 〇.k 主题扩展点 ThemeTokens（2026-11-10）

之前的 `ThemeStyle` 是**预设 id**（Mono / Swiss），不是扩展点：App 想要自己的品牌色，
只能改 `kit/.../Theme.kt` —— 而「不改底座」是 fork 模式的第一条规矩。这两件事直接矛盾，
所以把主题拆成「tokens + 两个装配入口」：

| 入口 | 用途 |
|---|---|
| `AppTheme(style = ThemeStyle.Mono/Swiss)` | 底座预设，历史行为不变 |
| `AppTheme(tokens = ...)` | **App 自己的品牌主题**，一行都不用改 kit |

`ThemeTokens` 是四个轴 + 三个开关：

| 字段 | 作用 |
|---|---|
| `colorScheme` | M3 的 35 个颜色槽位 |
| `extendedColors` | success / warning / aiAccent / star 这些 M3 之外的语义色 |
| `typography` / `shapes` | 排版、圆角桶 |
| `isDark` | 这套 tokens 是深色还是浅色（**不是**「系统当前是不是深色」） |
| `themeStyle` | 风格标识，标签色板等按它分发；品牌主题保持 Mono |
| `cornerStyle` | `themedCorner()` 用哪个风格（Rounded / Square） |
| `tagColors` | 标签色板；留空 = 按 `themeStyle + isDark` 取底座色板 |

造 tokens 用 `themeTokens(colorScheme, isDark, ...)`，只传颜色也能跑；其余轴有默认值。
底座预设也变成了工厂：`monoThemeTokens(isDark)` / `swissThemeTokens(isDark)` / `notionThemeTokens(isDark)`。

### 顺带修掉的两个「只换色不换风格」问题

- `themedCorner()` 原来按 `ThemeStyle` 硬判直角，品牌主题（Mono）永远拿不到直角。
  现在读 `LocalCornerStyle`，由 tokens 的 `cornerStyle` 注入。
- `tagColor()` 原来也按 `ThemeStyle` 分发，品牌色板接不进去。现在读 `LocalTagColors`，
  由 tokens 的 `tagColors` 注入（留空自动回落）。

### 规矩

- **品牌 tokens 放 app 模块**（示例：`apps/demo/.../theme/DemoBrand.kt`），不要加进 kit。
- tokens 按明暗各造一套；`AppTheme(tokens)` 不再收 `darkTheme`，避免「浅色 tokens + 深色标记」错配。

### 踩坑

- kit 里有个顶层 `val Typography`（Mono 排版），和 M3 的 `Typography()` 类同名。
  写默认参数时 `typography: Typography = Typography` 右边解析到的是 **val**，别手滑写成 `Typography()`，
  否则 Mono 的排版会被换回 M3 默认值。
- `ThemeTokens` 没有标 `@Immutable`：里面的 `Map` 不是 Compose 能证明不可变的类型，
  标了等于对编译器撒谎，反而会让重组判断失真。

---

## 〇.l 后端模式 GatewayAgentRuntime（2026-11-10）

`backend/` 一直是「可选网关」，但 app 侧没有客户端 —— 〇.b / 〇.c 把 `AgentClient` / `KtorAgentClient`
删掉之后，「后端模式」只存在于服务端。这一节把它接回来：同一个 `AgentRuntime` 契约的第二种实现。

| 文件 | 内容 |
|---|---|
| `data/agent/gateway/GatewayProtocol.kt` | SSE 帧定义 + 纯函数解析（不碰网络，好测） |
| `data/agent/gateway/GatewayAgentRuntime.kt` | `GatewayAgentRuntime` + Ktor 传输层 + 会话续接 |
| `data/agent/AgentMode.kt` | `Local` / `Gateway` 两种模式 + `createAgentRuntime()` |

### 协议映射

| 网关 SSE | `AgentEvent` |
|---|---|
| `start{sessionId}` | 内部记住，不发给 UI（下一轮带回去 resume） |
| `text{text}` | `Text`（**完整消息**，不是增量块） |
| `tool{name}` | `ToolUse(name, 空入参)` —— 网关不给入参 |
| `result{output}` | `Result(output 转文本)` |
| `error{message}` | `Error` |

`result.output` 是 skill 自己的 JSON：`{"text": …}` 取 text，纯字符串原样，其他 `toString()`。

### 三个必须知道的差异

1. **skill 有两份定义**：本地 Kotlin `SkillDefinition`，服务端 `skills/<id>/SKILL.md`。
   客户端按 `skill.id` 找服务端同名 skill，两边行为要自己对齐。
2. **工具在服务端执行** → 本地 `ToolApprover` **拦不住**。危险工具白名单归服务端
   `SKILL.md` 的 `tools` 字段（backend README 要求默认空）。
3. **文本不是流式增量**：`text` 是一条完整消息，UI 追加逻辑不用改，但没有打字机效果。

### 入参

`send(String)` 只有一个字符串，而网关要 JSON 对象：默认包成 `{"input": "<文本>"}`。
skill 的 schema 字段名不同（如 `summarize` 的 `text`）就传 `inputField = "text"`。

### 安全

- token 由 App 从平台安全存储（Keystore / Keychain）读出后传入，**不进 BuildConfig**；
  kit 不做 expect/actual 加密存储。
- baseUrl 必须 HTTPS；网关只认服务端下发的 skill，不接受客户端指定 skill 文件。

### 测试

不引 `ktor-client-mock`：传输层抽成 `GatewayInvoker` 接缝，单测注入假实现，
覆盖协议映射、sessionId 续接、错误帧、连接中断、异常兜底（`GatewayAgentRuntimeTest`），
SSE 解析单独测（`GatewaySseTest`）。真正的 HTTP 只在编译期保证。

---

## 〇.m build-logic 与 app 生成器（2026-11-10）

模板的承诺是「加第 N 个 app 只写业务」。但之前每个 app 的 `build.gradle.kts` 是 demo 的 160 行复制品，
targetSdk / 签名 / detekt / 测试参数全靠人肉对齐 —— app 一多必然漂移。

### 结构

| 位置 | 作用 |
|---|---|
| `build-logic/` | included build，放约定插件；不污染主构建、不随 app 数膨胀 |
| `build-logic/conventions/.../androidkit.android.app.gradle.kts` | 所有 `apps/<name>` 的 Android 壳共用配置 |
| `scripts/new_app.py` + `scripts/templates/app/` | 生成最小可编译 app 壳 |

### app 的 build.gradle.kts 只剩三样

1. `namespace` / `applicationId`
2. 自己的 BuildConfig 字段（key、更新清单 URL…）
3. 依赖

SDK（compileSdk 37.1 / minSdk 26 / targetSdk 36）、versionName/Code、签名、R8、测试参数、detekt
全在约定插件里。版本号走 `-PVERSION_NAME` / `-PVERSION_CODE`。

### 约定插件为什么不 apply AGP

app 的 build 文件自己 `id("com.android.application")`（版本在根 `build.gradle.kts` 统一声明），
约定插件只 `pluginManager.withPlugin(...)` 配置。这样：

- 版本只有一个出处（根 plugins 块），插件 classpath 不会出现两份 AGP；
- app 脚本里的 `android {}` accessor 仍然可用（accessor 由脚本里声明的插件生成）。

### 生成器

`python scripts/new_app.py <name> --package <pkg> --app-name "<显示名>"`：

- 从 `scripts/templates/app/` 渲染，替换包名/路径/显示名；
- 生成 manifest（默认图标用系统占位）、MainActivity（已套 `AppShell` + `AppTheme`）、
  主题、strings、build 文件；
- 不改 `settings.gradle.kts`（`apps/` 自动 include），不碰底座文件。

它**不做**：iOS 壳、业务 skill、图标/启动图、签名配置 —— 按项目自己补（iOS 壳照 `apps/demo/ios/` 抄）。

### 踩坑

- **Kotlin 块注释可以嵌套**：KDoc 里写 `apps/*` 会开一个嵌套注释，把后面的代码全吃掉
  （报 `Unresolved reference 'times'`）。注释里写 `apps/<name>`。
- build-logic 用 `versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }`
  复用主构建版本目录 —— AGP / detekt 不会出现两套版本。
- included build 会多拉一批 kotlin-dsl 编译期依赖；`org.gradle.dependency.verification=lenient`
  下只是 warning。要严格校验就在 macOS 上重跑 `--write-verification-metadata sha256`。
- 生成器是 Python（跨平台），不依赖 bash：Windows 上也能直接跑。

---

## 〇.n 第三套主题 Swiss 瑞士国际风格（2026-11-12）

规范来自 StyleKit `swiss-style`（黑白 + 信号红、Helvetica 系无衬线、零圆角零阴影零渐变、
150ms ease-out 只变色）。底座照老路走：`ThemeStyle` 加一个枚举值 + 一个
`swissThemeTokens(isDark)` 工厂 + 一份色板 + 一份排版，组件零改动。

### 落地清单

| 文件 | 内容 |
|---|---|
| `ui/theme/ColorSwiss.kt` | 35 槽位浅色 + 深色反转、扩展语义色、标签色板、`swissRed()` |
| `ui/theme/TypeSwiss.kt` | `SwissTypography`（11/13/15/20/32/56/88，400/700） |
| `ui/theme/MotionSwiss.kt` | `SwissMotion`（150ms，cubic-bezier(0.2,0,0,1)） |
| `ui/theme/Theme.kt` | `ThemeStyle.Swiss`、`swissThemeTokens()`、`themedElevation()` |
| `ui/theme/Weights.kt` | `themedFontWeight()`（Medium / SemiBold 一律折成 Bold） |
| `ui/theme/Alignment.kt` | `themedTextAlign()` / `themedHorizontalAlignment()`（一律左对齐） |
| `ui/theme/Disabled.kt` | `themedDisabledContentColor()`（降对比但保持可读） |
| `ui/theme/Shapes.kt` | `themedCircleShape()`（装饰性圆形改直角） |
| `ui/components/SwissComponents.kt` | `SwissSectionLabel` / `SwissStructuralRow` / `SwissInlineButton` / `SwissColorField` |
| `ui/components/SwissLayout.kt` | `SwissPage` / `SwissGrid` / `SwissRule` / `SwissSection` |

### 三个自己做的决定

1. **深色是自建的**。规范 `readiness.darkMode = missing`，只给了浅色契约；
   底座 `AppearanceMode` 又必须支持深色，所以做了语义反转（黑画布 + 白字 + 亮红），
   不是规范原文。守住「一屏只用红或蓝之一」「红不超过一个字符宽」两条纪律。
2. **红色分了两个**。规范红 `#ff0000` 对白底只有 4.0:1，小字达不到 AA：
   文字位用 `#c40000`（6.3:1）/ 深底用 `#ff3b30`（5.9:1），色块和栏线仍用原色。
3. **字体用系统无衬线**。规范自己写的替身是 "Inter, Arial, 'Helvetica Neue', sans-serif"，
   随包带字体要体积 + 授权，先用 `FontFamily.SansSerif`。

### 规矩

- **Elevation: None** 是规范硬要求：组件里凡是写死 elevation 的地方，
  默认值换成 `themedElevation(1.dp)`，别在每个组件里 `if (style == Swiss)`。
- 交互只改颜色/边框色：不位移、不缩放、不加阴影。唯一允许的位移是按钮箭头（规范点名）。
- 悬停态只在桌面/网页出现（`collectIsHoveredAsState`），触摸端不触发，等于普通内容。

### 复核收口（第二轮，对着 brief 逐条查）

第一轮只做了「Swiss 自己的四个组件」，规范里另外几条没人管 —— 切到 Swiss 主题
仍然是半套。第二轮补的是**共享层**和**布局层**：

| 规范条款 | 收口点 |
|---|---|
| Layout：十二栏 / 1200px / 96px 分区间距 / 1px 黑栏线 | 新增 `ui/components/SwissLayout.kt`：`SwissPage` / `SwissGrid` / `SwissRule` / `SwissSection` |
| Do not wrap content in cards | `AppCard` / `SmallCard` 在 Swiss 下容器色 = 画布色（卡片「消失」，只剩栏线） |
| No border radius | `themedCircleShape()`：装饰性圆形容器改直角（头像/照片那种图形本身仍是圆） |
| Do not exceed two weights | `themedFontWeight()`：组件里写死的 Medium / SemiBold 在 Swiss 下折成 Bold |
| Flush left everywhere, never centred | `themedTextAlign()` + `themedHorizontalAlignment()`：文字和容器一起左对齐 |
| Disabled must remain readable | `themedDisabledContentColor()`：M3 默认的 onSurface 38% 透明只有 2.4:1，改用 `onSurfaceVariant`（8.9:1） |
| No shadow and no gradient | `AvatarHero` 在 Swiss 下平涂（原来只有 macOS 分支）；`aiGlassSurface` 早有 Swiss 平涂分支 |
| Rational Restraint：零 scale | `StarFavoriteButton` 不回弹、`AvatarStack` 展开不缩放 |
| Guide Line Extension：栏线用原色红 | 悬停栏线 / 下划线改 `Swiss.redBlock`（原色 `#ff0000`）—— 栏线是图形不是文字，不吃 AA |
| 焦点环 2px + 偏移 | `Modifier.swissFocusRing()`：2px 红环，离组件边缘 2dp |

**踩坑**：M3 的 `ButtonDefaults.buttonColors()` 没有 `hoverContainerColor` 参数 ——
按钮 hover 换色得靠 `Button` 自己的 state layer，或者像 `SwissInlineButton` 那样自己画。

### 踩坑

- `swissRed()` 是 `@Composable`，**不能**在 `drawBehind` / `drawWithContent` 里调 ——
  得先在外面取值再捕获进 lambda，否则报 "Composable invocations can only happen from..."。
- `RoundedCornerShape` 的 `size` 在当前 Compose 版本不是公开属性；
  断言零圆角要靠 `assertEquals(RoundedCornerShape(0.dp), shapes.small)`。
- `apiDump` 会把工作区里**别的**未提交改动一起写进基线 —— 这次基线顺带收了
  `AppSettings` 等在途改动（那份基线本来就过期了）。

---

## 〇.o 第四套主题 macOS 毛玻璃 vibrancy（2026-11-12）

规范来自 StyleKit `macos-vibrancy`（三档灰 + 系统蓝、衬线标题、1px 分隔、200ms 只变色）。
老路：`ThemeStyle` 加枚举值 + 一个 `macOSThemeTokens(isDark)` 工厂 + 一份色板 / 排版 / 动效，
组件零改动。

### 落地清单

| 文件 | 内容 |
|---|---|
| `ui/theme/ColorMacOS.kt` | 35 槽位浅色 + 深色、扩展语义色、标签色板（macOS 系统色） |
| `ui/theme/TypeMacOS.kt` | `MacOSTypography`（衬线标题 / 无衬线正文）+ `MacOSCodeStyle`（等宽） |
| `ui/theme/MotionMacOS.kt` | `MacOSMotion`（200ms，cubic-bezier(0.2,0,0,1)） |
| `ui/theme/Theme.kt` | `ThemeStyle.MacOS`、`macOSThemeTokens()`、`themedElevation()` |
| `ui/theme/Shapes.kt` | `themedControlShape()`（按钮 / 输入 6px）、`themedStrokeWidth()`（描边 1px） |
| `ui/components/MacOSComponents.kt` | `MacOSVibrancyPanel` / `MacOSVibrancySidebar` / `MacOSListRow` / `MacOSToolbar` / `MacOSSegmentedControl` / `MacOSSystemButton` |

### 四个自己做的决定

1. **深色是规范原生，浅色是自建的**。规范 `theme: dark`，readiness 也标了 darkMode missing；
   但底座 `AppearanceMode` 必须支持浅色，所以按 macOS 浅色系统的实际做法自建一套
   （窗底 `#f2f2f7` + 白内容面 + `#007aff` 系的蓝）。
2. **浅色的蓝绿都压深了一档**。`#007aff` 在白底只有 4.0:1，做 13px 按钮文字不够 AA，
   文字位改 `#0060d6`（5.8:1）—— 和 Swiss 把 `#ff0000` 换成 `#c40000` 一个套路。
3. **没有 backdrop-filter**。规范写的是 `rgba(28,28,30,0.8)` + `blur(40px)`，
   但 CMP **没有背景模糊**（`Modifier.blur` 模糊的是节点自身内容），
   所以毛玻璃层只做「半透明 + 1px 白边」；真背景模糊得走各端原生。
4. **elevation 归零**。规范哲学是「层次靠背景深浅，不靠阴影」，所以 `themedElevation()` 里
   macOS 和 Swiss / Notion / E-Ink 一样归零。
5. **共享组件按风格收口**。规范是「这套风格长什么样」，不是「这几颗组件长什么样」——
   共享组件（按钮 / 输入 / 卡片 / AI 玻璃底 / 头像 / 动效）也得跟着风格走，
   否则切到 macOS 就是「半套主题」。收口点在**共享函数里**，不在每个调用点：
   `themedControlShape()`、`themedStrokeWidth()`、`M3Motion.fadeThrough()`、
   `rememberBreathingAlpha()`、`aiGlassSurface()`、`AvatarHero`、`AppCard` 各自认一次风格。

### macOS 下共享组件的收口明细

| 规范条款 | 落地 |
|---|---|
| 禁止渐变 / 发光 | `aiGlassSurface()`、`AvatarHero` 在 macOS 下改成平涂（+ 1px 边） |
| 描边永远 1px | `themedStrokeWidth()`：`TagText` 选中框 1.5→1、`FilterChip` 选中框 2→1、头像圈 1.5→1 |
| 只改颜色，不位移不缩放 | `fadeThrough()` 去掉 scaleIn、`floatEnter/Exit` 只淡入淡出、`rememberBreathingAlpha()` 静态化、`StarFavoriteButton` 不回弹 |
| 按钮 / 输入 6px 圆角 | `themedControlShape()`：`PrimaryButton` / `SecondaryButton` / `SmallSecondaryButton` / `DeleteButton` / `SimpleTextField` |
| 卡片 = 深灰底 + 1px 白 8% 边 | `AppCard` / `SmallCard` 在 macOS 下补 `BorderStroke(1.dp, outlineVariant)` |
| 输入聚焦 = 白 25% | `textFieldBorderColors()` 在 macOS 下用 `onSurface 25%`，不是主色蓝 |
| 焦点环 = accent 色 | `MacOSListRow` / `MacOSSystemButton` 用系统蓝 2px 环，往里缩 1px 留缝 |

### 规矩

- **1px 哲学**：分隔 / 边框一律 1px 半透明白（8% / 12%）—— `outline` / `outlineVariant`
  给的是带 alpha 的白，画在任何底色上都自然合成。
- **动效只改颜色**：不位移、不缩放；唯一允许的是控件自己的状态色过渡。
- **桌面密度**：行 44dp、正文 15px、标签 11px —— 不要按网页尺度放大。

### 踩坑

- `drawBehind` 里**不能**读 `MaterialTheme.colorScheme`（Composable 调用）——
  先在外面取值再捕获进 lambda。
- `MaterialTheme.colorScheme.outline` 给了带 alpha 的白时，注意它同时被当作「聚焦环」用，
  浅色主题下要的是黑 12% 而不是白 12%（macOS 的焦点环改用 accent 蓝，不复用 outline）。
- 规范写「13px 衬线标题居中」时，**别去改 `headlineSmall`** —— 那一档被弹窗 / 底部表单共用，
  改了会连带动别的风格；用 `bodyMedium.copy(fontFamily = Serif)` 单独拼一个 13px 衬线。

---

## 二、代码的取舍

### kit 里的（41 个组件 + 主题 + 模板 + 存储 + AI 内核）

| 位置 | 内容 | 说明 |
|---|---|---|
| `kit/src/commonMain/.../ui/theme/` | Mono 暖中性色板、Type、Motion、TagColors、ExtendedColors | 零外部依赖，原样可用 |
| `kit/src/commonMain/.../ui/components/` | 41 个通用组件 | 剔掉了领域耦合的部分，见下 |
| `kit/src/commonMain/.../ui/templates/` | `ListScreen` `FormScreen` | 无状态骨架 + `@Preview` |
| `kit/src/commonMain/.../data/storage/` | `EntityStore` 接口 + 两套引擎 + `StoreCodec`（md / json） | **不依赖业务层**，原样可用 |
| `kit/src/commonMain/.../data/ai/` | `core/` `config/` `providers/` `templates/` | AI 抽象层：Provider 能力枚举、场景路由、模型回退、Prompt 模板（**已删，见 〇.b**） |
| `kit/src/commonMain/.../ui/preview/DemoData.kt` | 精简版预览数据 | 裁到只剩通用组件用得到的成员 |

### 剔掉的（领域耦合）

| 文件 | 为什么剔 |
|---|---|
| `AddRelationBottomSheet` `RelationPickerBottomSheet` `RelationTypeOptions` | 依赖 `Contact` / `ContactRelation` 模型 |
| `PhoneContactPickerSheet` `ContactTypePickerBottomSheet` | 依赖 `Contact` / `ContactTypeInfo` |
| `QuestionCardView` | 依赖 `QuestionCard` / `OptionAction` |
| `UsageCard` `NavigationDrawer` | 依赖计费 `PlanTier` / 设置仓库 |
| `DialogTemplates` | 依赖 `Goal` + `StorageManager` |
| `Cards.kt` 的 `RecordCard` / `RecordListItem` / `RecordModel` / `recordTypeColor` | 依赖 `Record` / `RecordType`；**只留了 `AppCard` `CardTitle` `SmallCard`** |
| `DateFormatting.kt` 的 `recordDateLabel` | 依赖 `Record` 模型；**其余 5 个格式化函数全部保留** |
| `data/model/*` `data/repository/*` `ui/screens/*` | 整个业务域，各项目自己写 |
| `data/ai/repository/*` | 依赖 `Goal` / `QuestionCard` / 计费；AI **内核**保留，仓库层自己写 |

### 漏网的领域耦合（不是 import 形式，grep 抓不到）

`UserAvatar.kt` 里曾经写的是 `MyProfileRepository.getInstance()` 的全限定名 ——
没有 import 行，grep 抓不到，**只有真编译才能发现**。
所以 kit 必须过一遍真实构建，不能只靠静态查 import。

另一个坑：`ListScreen.kt` 用 `import ...R.string as RString` 别名引用字符串，
按 `R.string.x` 抓会漏掉 5 个 key —— 抽 strings 时要一起处理别名。
（KMP 化后改成 `import com.androidkit.app.res.Res.string as RString`，
`verify-kit.py` 现在抓 `Res.string.x` + 别名两种形式。）

### 改写过的（少量手工改动）

| 文件 | 改动 |
|---|---|
| `ui/theme/Theme.kt` | 去掉 `SettingsRepository` 依赖 → `AppTheme(darkTheme)`，注释里标了「想跟随设置就把这行换回去」 |
| `ui/theme/Color.kt` | `Importance` 从 `data.model` 换成本 kit 自带的 `ui/theme/Importance.kt`（3 个枚举值） |
| `ui/preview/DemoData.kt` | 删掉 `Goals` `Contacts` `Tasks` `Ai` `Activity` `Ideas` `Profile` `Billing` 八个对象；`Home` `Tags` `States` 换成只有通用组件用到的字段的 stub |
| `ui/components/UserAvatar.kt` | 头像 URL 解析原本走 `MyProfileRepository`（领域层），改成直接用原始路径 + `ponytail:` 注释 |
| 全部 `.kt` | 主题入口统一叫 `AppTheme` |
| `kit/src/commonMain/composeResources/values/strings.xml` | 从 1119 条砍到 **133 条**（kit 代码实际引用的），`values-en` 同步 |
| `apps/demo/src/main/res/values/themes.xml` | `Theme.AndroidKit` 保留（示例壳工程专用）；公共层不依赖它 |

---

## 三、两个坑

1. **`.gitignore` 陷阱**。上游仓库的 `.gitignore` 忽略了 `.claude/*`（只放行 `.claude/scripts/`），
   所以 `GOOGLE_STYLE.md` 和所有 skill 一直是**未跟踪**状态 —— 换台机器就没了。
   kit 的 `.gitignore` 特意**不忽略** `.claude/`、`.agents/`、`rules/`、`skills/`。

2. **release 签名的假设置**。上游仓库的 `apps/demo/build.gradle.kts` 里 release 用
   `signingConfigs.getByName("debug")` —— 只为本地出包，发商店必炸。
   kit 的 `apps/demo/build.gradle.kts` 把真 keystore 的写法注释在原位，别直接照抄 debug 签名那行。

---

## 四、kit 自己不动手也能查的问题

`scripts/verify-kit.py` 做静态自检（不用编译）：

1. `import com.androidkit.app.*` 指向的符号在 kit 里不存在
2. `Res.string.x` 在 kit 的 `strings.xml` 里没有对应 key
3. `DemoData.A.B` 在 kit 的 `DemoData` 里没有对应成员

改完 kit 跑一遍：`python scripts/verify-kit.py`。

`scripts/verify-skills.py` 查 app 层的 skill 工具（不用编译）：

1. tool 类里有 Compose 代码
2. `doExecute` 里有业务分支（`if` / `when` / `for` / `while` / `try`）
3. 工具名不是英文 snake_case

写完 skill 跑一遍：`python scripts/verify-skills.py`（**只警告，不挡构建**）。

---

## 〇.g 付费 / 会员（`:kit-membership`）

计费拆成两半：

| 在哪 | 是什么 |
|---|---|
| `:kit-membership`（**框架，平台无关**） | 档位表（`PlanCatalog`）、权益判定、额度引擎（按天/总量）、会员状态落盘、付费墙 + 升级页 UI |
| 各端 app（**平台接入**） | `BillingProvider` 实现：Android Play Billing / iOS StoreKit / 桌面假实现 |

为什么单独一个模块：跟 `:kit-agent-koog` 一个套路 —— **不要付费的 app 不用背这份代码**，
`:kit` 本身仍然一行计费都不留（`billing` 依赖没进 `:kit`）。

几条硬规矩（改这块前先看）：

- **档位表写在代码里，不从服务器下发**。下发的话额度上限能被随便改。
- **会员资格只认「已验过签」的购买**（`Membership.verified`）。没验签的购买等于送会员。
- 过期 / 没验签 / 找不到档位 → 一律按免费档算（一个分支，不到处判）。
- 额度用 `consume()` 扣，**别自己加计数** —— 扣了才知道超没超，超了不动计数。
- 额度计数落盘在 app 内部存储（okio JSON），按天重置按**本地时区自然日**算。

## 五、还没搬的（以后可能想要）

- `StorageManager` / `ObsidianDetector` —— 领域目录常量 + 封面图资源，改造量不大但收益不高
- `data/ai/repository/AiRepositoryImpl` + `AiParsers` —— 含 `Goal` / `QuestionCard` 解析，改造量中等
- 计费的**平台接入**（Play Billing / StoreKit）—— 跟业务强相关，各端 app 自己实现 `BillingProvider`；
  平台无关的那部分（档位 / 权益 / 额度 / 会员状态）已经落在 `:kit-membership` 模块（见「〇.g」）
- `RichEditor` / `compose-markdown` 的封装层 —— 依赖已带，封装没带（KMP 版 markdown 渲染器待选型）
- iOS 端业务实现 —— kit 只覆盖了 Android 侧，iOS 业务层要从零写（kit 只给了 `MainViewController` 入口）
