# 参与开发

## 两种用法

| 用法 | 怎么做 | 适合 |
|---|---|---|
| **fork 模式**（推荐） | fork 本仓库 → `git remote rename origin upstream` → 在 `apps/<name>/` 里长出自己的 app | 要持续跟进底座更新 |
| **拷贝模式** | `bash scripts/install-kit.sh <目标目录> <新包名> <应用名>` | 已有项目，只想拿组件层 |

## fork 模式的唯一规矩

**只新增、不改底座文件。**

底座路径（**唯一真相是 `scripts/check-base-unchanged.sh` 里的 `PROTECTED_PATHS`**）：

| 类别 | 路径 |
|---|---|
| 库 | `kit/`、`kit-agent-koog/`、`kit-membership/` |
| 构建 | `build-logic/`、`gradle/`、`config/`、`settings.gradle.kts`、`build.gradle.kts`、`gradle.properties` |
| 规范 | `rules/`、`skills/`、`backend/`、`apps/demo/` |
| 门禁 | `.github/actions/`、`.github/workflows/{ci,base-guard,release}.yml`、`.github/{CODEOWNERS,dependabot.yml,pull_request_template.md}`、`scripts/ci.sh`、`scripts/check-base-unchanged.sh`、`scripts/new_app.py`、`scripts/install-kit.sh`、`scripts/templates/`、`scripts/verify-*.py`、`scripts/merge_strings.py` |
| 法务 | `LICENSE`、`SECURITY.md` |

改了，pull 上游就冲突，更新链就断。底座能力不够，**回上游提 PR**，别在下游打补丁。

**这条规矩是机器强制的**，不是嘴上说说：

- 上游有 `.github/workflows/base-guard.yml` —— 用 `pull_request_target` 触发，workflow 永远取 **base 分支**的版本，fork 改不动它（改了就等于把检查删了）。PR 碰了上面任一路径 → 这道门禁直接红，PR 合并不进来。
- fork 本地自查：`bash scripts/ci.sh base-guard`（默认比对 `upstream` 远端的默认分支）。`bash scripts/ci.sh all` 已经包含它 —— 提交前跑一遍，改了底座当场就知道。

例外只有两个：

1. **自己的发布 workflow** —— fork 把 `.github/workflows/release.yml` 拷成自己的 `release-<name>.yml`（新文件，随便改；`.github/` 里只有上表那几个是底座）。
2. **自己的门禁脚本** —— 放 `scripts/checks/<你的名字>.sh`，那块不锁。

## 加一个新 app

最快的一条路：`python scripts/new_app.py <name>` —— 生成可编译的壳（含一条壳自检单测），
`settings.gradle.kts` **自动 include `apps/` 下所有模块**，CI 自动逐个构建 —— 不用注册。

1. 引用 `kit/`（要 agent 再加 `kit-agent-koog/`），为每个功能写一个 `SkillDefinition`。
2. 给 skill 配黄金样例 —— **skill 化没有编译期保障，样例就是它的「类型检查」**：
   - `skill.violations()`：静态体检（工具名、描述、重名、limit）
   - `skill.runSamples(...)`：工具样例（工具名 + 入参 JSON + 期望返回里的一段话）

   照抄 `apps/demo/src/test/kotlin/com/androidkit/app/demo/TodoSkillTest.kt`。
3. UI collect 事件流渲染。

详见 `ARCHITECTURE.md`「写一个新 app 的步骤」。

## 质量门禁

门禁逻辑**不在 workflow 里** —— 全在 `scripts/ci.sh`，CI 和本地跑的是同一份。
workflow 只负责「在什么环境跑哪道门禁」。

```bash
bash scripts/ci.sh all        # 提交前跑这个：detekt + api + verify + test + base-guard
bash scripts/ci.sh detekt     # 静态规则（命名 / 复杂度 / 吞异常…）
bash scripts/ci.sh api        # 公开 API 二进制兼容
bash scripts/ci.sh verify     # 自带脚本 + scripts/checks/ 下的自定义门禁
bash scripts/ci.sh test       # 单元测试（kit / kit-agent-koog / apps/*）
bash scripts/ci.sh lint       # Android lint
bash scripts/ci.sh backend    # agent 网关自检（不花钱、不连网）
bash scripts/ci.sh base-guard # 底座保护：改了底座路径就失败（fork 必跑）
```

**加一道自己的门禁**：往 `scripts/checks/` 放一个 `.sh`，退出码非 0 = 失败。
`ci.sh verify` 会自动跑它 —— 不用改 `ci.sh`，也不用改 `.github/workflows/`。
上游只往那个目录加**自己命名的文件** —— 你用别的文件名，`git pull upstream` 就不会冲掉你那份。
详见 `scripts/checks/README.md`。

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
