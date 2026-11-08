## 改了什么

<!-- 一两句说清楚。别写「优化代码」这种没法 review 的话 -->

## 为什么

<!-- 关联的 issue / 需求。没链接就说清楚动机 -->

## 怎么验的

<!-- 跑过什么命令 / 看过哪个页面。没验证就说没验证，别含糊 -->

```bash
./gradlew :kit:desktopTest :kit-agent-koog:desktopTest
./gradlew :kit:detekt :kit-agent-koog:detekt
python scripts/verify-kit.py
```

## 检查项

- [ ] 没改底座文件（`kit/`、`kit-agent-koog/`、`rules/`、`backend/`、`.github/workflows/`）—— fork 模式下这会让上游更新冲突
- [ ] 没把 API key、token 写进代码或日志
- [ ] 新增的用户可见文案走了 compose-resources（`Res.string.x`），并且 `values/` 和 `values-en/` 都补了
- [ ] 新增的工具遵守「逻辑 api 化」：tool 里没有业务分支，UI 按钮和 agent 调同一个函数
- [ ] 危险操作走 `ToolApprover` 闸门，闸门在工具**执行前**拦
