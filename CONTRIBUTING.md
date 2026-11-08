# 参与开发

## 两种用法

| 用法 | 怎么做 | 适合 |
|---|---|---|
| **fork 模式**（推荐） | fork 本仓库 → `git remote rename origin upstream` → 在 `apps/<name>/` 里长出自己的 app | 要持续跟进底座更新 |
| **拷贝模式** | `bash scripts/install-kit.sh <目标目录> <新包名> <应用名>` | 已有项目，只想拿组件层 |

## fork 模式的唯一规矩

**只新增、不改底座文件。**

底座路径：`kit/`、`kit-agent-koog/`、`rules/`、`.github/workflows/`、`backend/`、`settings.gradle.kts`。

改了，pull 上游就冲突，更新链就断。底座能力不够，**回上游提 PR**，别在下游打补丁。

例外只有一个：发布用的 workflow。fork 把 `.github/workflows/release.yml` 拷成自己的 `release-<name>.yml`（新文件，随便改）。

## 加一个新 app

1. 照抄 `apps/demo/` → `apps/<name>/`，改四处：`build.gradle.kts` 的 namespace / applicationId、`strings.xml` 的 app_name、主题名。
   `settings.gradle.kts` **自动 include `apps/` 下所有模块**，CI 自动逐个构建 —— 不用注册。
2. 引用 `kit/`（要 agent 再加 `kit-agent-koog/`），为每个功能写一个 `SkillDefinition`。
3. 给 skill 配 5-10 个黄金样例对话（输入 → 期望行为）—— skill 化没有编译期保障，样例就是它的「类型检查」。
4. UI collect 事件流渲染。

详见 `ARCHITECTURE.md`「写一个新 app 的步骤」。

## 提交前

```bash
./gradlew :kit:desktopTest :kit-agent-koog:desktopTest   # 真测试在这两个任务里
./gradlew :kit:detekt :kit-agent-koog:detekt             # 静态检查（规则集在 config/detekt/）
./gradlew :kit:apiCheck :kit-agent-koog:apiCheck         # 公开 API 的二进制兼容性
./gradlew lintDebug                                     # Android lint
python scripts/verify-kit.py                            # import / Res.string / DemoData 断裂
python scripts/verify-skills.py                         # skill 工具有没有把业务逻辑写进 tool
```

CI 跑的就是这些（`apps/*` 的 `testDebugUnitTest` 目前是空的，但 fork 的 app 加了单测就自动进来）。

**改了 `kit/` 的公开 API**（公开类/函数的签名）—— `apiCheck` 会失败，这是故意的：
确认是有意为之之后，跑 `./gradlew :kit:apiDump` 更新 `api/*.api` 基线，一起提交。
只有想清楚了才更新 —— 这份基线是给下游用的兼容性承诺。

**detekt 报错了**：规则集在 `config/detekt/detekt.yml`。
新增规则前先想清楚 —— 规则是给所有人（包括 fork 的下游）跑的，别加只适合本仓库的规则。

## 代码风格

- 文案走 **compose-resources**（`Res.string.x`），不是 `R.string.x`
- `commonMain` 里**禁止** `import android.*` / `import java.*`，平台差异下沉到 `androidMain`
- 业务动作放 data 层，**UI 的按钮和 agent 的工具调同一个函数**（见 `rules/CLAUDE.template.md`「逻辑 api 化」）
- 注释写「为什么」，不写「做了什么」

规则全文见 `rules/CLAUDE.template.md`。
