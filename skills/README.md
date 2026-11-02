# Skills

## 已随 kit 附带

| 目录 | 来源 | 干什么 |
|---|---|---|
| `android-skills/build-system/agp/agp-9-upgrade` | Google 官方 [android/skills](https://github.com/android/skills) | AGP 9 升级（含 KSP/kapt、paparazzi） |
| `android-skills/jetpack-compose/adaptive` | 同上 | Compose 自适应布局 |
| `android-skills/jetpack-compose/migration/migrate-xml-views-to-jetpack-compose` | 同上 | XML 视图迁移到 Compose |
| `android-skills/jetpack-compose/theming/styles` | 同上 | Compose 样式与主题 |
| `openclaw-skills-material-design` | 第三方 | Material Design 3 对照检查（**只是参考，冲突时以 `rules/GOOGLE_STYLE.md` 为准**） |

`install-kit.sh` 会把它们分别复制到目标项目的 `.claude/skills/` 和 `.agents/skills/`。

## 没附带（按需 clone）

Google 官方 skills 仓库完整版有 5MB+，多数是本项目用不上的：

```bash
git clone --depth 1 https://github.com/android/skills .claude/skills/android-skills
```

需要全量时再 clone，覆盖掉 kit 里那几个子目录即可。

## 使用约定

写 Android 相关代码前，先看 skill 列表里有没有现成的：

- AGP 升级 → `build-system/agp/agp-9-upgrade`
- Compose 自适应 → `jetpack-compose/adaptive`
- XML 迁移 → `jetpack-compose/migration/migrate-xml-views-to-jetpack-compose`
- 样式主题 → `jetpack-compose/theming/styles`
- M3 规范对照 → `openclaw-skills-material-design`

没有现成 skill 再自己实现 —— 别重复造轮子。
