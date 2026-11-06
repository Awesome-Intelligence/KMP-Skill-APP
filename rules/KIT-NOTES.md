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
- app 里接一个真 skill，让这套 loop 第一次在设备上跑起来

---

## 一、规则漂移审计（规则初版 vs 实际代码）

规则初版里有 5 条**写了但代码没做到**的约定。直接抄会把未兑现的承诺带到新项目：

| 规则原文 | 代码实况 | kit 里的处理 |
|---|---|---|
| 「日志用 Timber」 | `build.gradle.kts` 引了 Timber，但代码里 6 处 `android.util.Log` | 依赖保留；`CLAUDE.template.md` 保留规则并注明「release 不 plant」 |
| 「UI 层统一 `UiState<T>` (Loading/Success/Error)」 | **根本没有这个类**，11 个页面各写各的 `XxxUiState` data class | 模板里补了 `UiState` 的 8 行真定义，规则才立得住 |
| 「MVVM + Clean Architecture，Room」 | 没有 Room，没有 `domain/` 层，存储是文件（格式可换） | 模板改成「默认文件存储，格式不锁死，要关系型查询再上 Room」 |
| 「适配 Android 5.0+ (API 21+)」 | `minSdk = 26` | 模板写死 26 |
| 包结构 `domain/` / `data/local/dao/` | 实际是 `data/` + `ui/` | 模板改成实际布局 |

`docs/TECHNICAL_DESIGN.md` / `docs/UI_DESIGN.md` 描述的也是**没实现的架构**（Room + Clean 分层），
所以**没有进 kit**。只有 `GOOGLE_STYLE.md`（纯视觉数值）和 `COMPONENT_SPEC.md`（设计令牌 + 组件规格）是无条件可复用的。

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

---

## 五、还没搬的（以后可能想要）

- `StorageManager` / `ObsidianDetector` —— 领域目录常量 + 封面图资源，改造量不大但收益不高
- `data/ai/repository/AiRepositoryImpl` + `AiParsers` —— 含 `Goal` / `QuestionCard` 解析，改造量中等
- 计费（`data/billing`）—— Google Play Billing 接入，跟业务强相关
- `RichEditor` / `compose-markdown` 的封装层 —— 依赖已带，封装没带（KMP 版 markdown 渲染器待选型）
- iOS 端业务实现 —— kit 只覆盖了 Android 侧，iOS 业务层要从零写（kit 只给了 `MainViewController` 入口）
