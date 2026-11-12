# Skills

## 已随 kit 附带（全量）

`android-skills/` 是 Google 官方 [android/skills](https://github.com/android/skills) 的**完整镜像**（352 个文件 / 3.6 MB，含 LICENSE.txt）。
每次新增 skill 不必再 clone，直接查本地这份。

| 分类 | 目录 | 主要内容 |
|---|---|---|
| 构建 | `build-system/agp/agp-9-upgrade` | AGP 9 升级（KSP/kapt、paparazzi、built-in Kotlin） |
| 构建 | `devtools/android-cli` | `android` CLI 的交互与 journey |
| Compose | `jetpack-compose/adaptive` | 自适应布局、`NavigationSuiteScaffold`、list-detail |
| Compose | `jetpack-compose/migration/migrate-xml-views-to-jetpack-compose` | XML 视图迁移到 Compose |
| Compose | `jetpack-compose/theming/styles` | Styles API + 状态/动画（`animate { }`，实验性） |
| 导航 | `navigation/navigation-3` | Nav3：类型安全路由、**`recipes/animations.md`（转场钩子）**、scenes |
| 导航 | `navigation/navigation-event` | 导航事件 / 返回处理 |
| 系统 | `system/edge-to-edge` | 边到边、M3 组件安全区 |
| 相机 | `camera/camerax` | CameraX 迁移与进阶（foldable / XR / wear） |
| 设备 AI | `device-ai/appfunctions` `device-ai/ml-kit-genai-prompt-api` | 系统级 function calling、ML Kit GenAI |
| 身份 | `identity/restore-credentials` `identity/verified-email` | Passkey / Digital Credentials |
| 媒体 | `media/media3-cast-integration` | Media3 Cast |
| 性能 | `performance/r8-analyzer` `profilers/android-profiler` | R8 规则分析、Perfetto 抓取与解读 |
| 发布 | `play/*` | 政策洞察、Engage SDK、Billing 升级 |
| 测试 | `testing/testing-setup` | 测试脚手架、截图测试 |
| 安全 | `security/android-intent-security` | Intent 安全 |
| 大屏 | `tv/leanback-to-compose-tv-migration` | TV Compose（`androidx.tv.material3`） |
| 手表 | `wear/wear-compose-m3` | **Wear OS M3**：组件样例、`TransformingLazyColumn` |
| XR | `xr/display-glasses-with-jetpack-compose-glimmer` | Glimmer（眼镜） |

另有第三方 `openclaw-skills-material-design`：Material Design 3 对照检查
（**只是参考，冲突时以 `rules/GOOGLE_STYLE.md` 为准**）。

`install-kit.sh` 会把它们分别复制到目标项目的 `.claude/skills/` 和 `.agents/skills/`。

## 关于「M3 规范要求」

Google 官方这套 skill **没有**一份「M3 动效/令牌要求清单」：

- `jetpack-compose/theming/styles` 的 `state-animations.md` 讲的是 Styles API 的 `animate { }`
  块（`compileSdk 37` + `ExperimentalFoundationStyleApi` opt-in），**CMP 1.12 上还用不了**；
- `navigation/navigation-3/recipes/animations.md` 只讲 `NavDisplay` 的
  `transitionSpec` / `popTransitionSpec` / `predictivePopTransitionSpec` 三个钩子，示例全是
  `tween(1000)` 占位值，没有 M3 时长/缓动。

M3 动效要求的实际出处是本仓库自己的两处：

- `skills/openclaw-skills-material-design/SKILL.md` §7 —— Duration 表、Easing、四种转场模式
- `rules/GOOGLE_STYLE.md` §动效 —— 落到代码的选型表（**冲突时以它为准**）

## 使用约定

写 Android 相关代码前，先看 skill 里有没有现成的：

- AGP 升级 → `android-skills/build-system/agp/agp-9-upgrade`
- Compose 自适应 → `android-skills/jetpack-compose/adaptive`
- XML 迁移 → `android-skills/jetpack-compose/migration/migrate-xml-views-to-jetpack-compose`
- 样式主题 → `android-skills/jetpack-compose/theming/styles`
- 导航 / 转场 → `android-skills/navigation/navigation-3`
- 边到边 → `android-skills/system/edge-to-edge`
- M3 规范对照 → `openclaw-skills-material-design`

没有现成 skill 再自己实现 —— 别重复造轮子。

## 更新

全量镜像的来源是 [android/skills](https://github.com/android/skills)（`main`）。
需要同步新版时用 `git clone --depth 1` 到临时目录，替换 `skills/android-skills/`（**别把 `.git` 带进来**）。
