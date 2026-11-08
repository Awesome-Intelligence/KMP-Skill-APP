# 更新日志

本项目遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号用 [语义化版本](https://semver.org/lang/zh-CN/)。

## [未发布]

### 新增
- 无

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
