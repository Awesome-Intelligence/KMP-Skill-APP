# agent-gateway

> **2026-11-02 起：agent loop 已移到手机端**（app = 前端 + skill + Koog 运行时，见根目录 `ARCHITECTURE.md`）。
> 本目录降级为**可选**：skill 需要服务端能力（shell、重计算、集中管控）时才部署；
> 或只当 key 转发代理用（客户端 `baseUrl` 直指本服务，本地 loop + 代理出网）。

通用 agent 网关：**skill 即逻辑**。每个 `skills/<id>/SKILL.md` 就是一个可调用的后端能力，
HTTP + SSE 暴露给任何前端。

## 快速开始

```bash
cp .env.example .env   # 填 ANTHROPIC_API_KEY
npm install
npm run smoke          # 不花钱的检查：skill 加载 + 契约校验
npm run dev            # http://localhost:8787
```

```bash
# 调用
curl -N -X POST http://localhost:8787/v1/skills/summarize/invoke \
  -H "Authorization: Bearer $GATEWAY_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"input": {"text": "一段长文本…"}}'
```

## API

| 路由 | 说明 |
|---|---|
| `GET /health` | 存活检查 |
| `GET /v1/skills` | skill 列表 `[{id, name, description}]` |
| `POST /v1/skills/:id/invoke` | body `{input, sessionId?}` → SSE 事件流 |

SSE 事件（`event:` 与 data JSON 里的 `type` 一致）：
`start{sessionId}` → `text{text}` / `tool{name}` … → `result{output}`（已按契约校验）| `error{message}`。

## 写一个新 skill

建目录 `skills/<id>/SKILL.md`，frontmatter 里 `input` / `output` 写 **JSON Schema**（服务端双向校验），
正文写 agent 的 prompt（这就是这个 skill 的全部"业务逻辑"）：

```markdown
---
name: <显示名>
description: <一句话>
maxTurns: 2          # 可选，agent 最大轮数
tools: []            # 可选，工具白名单；空 = 纯对话
input:  { type: object, ... }
output: { type: object, ... }
---
<给 agent 的 prompt，说明如何从 input 得到 output>
```

重启即生效。新增 skill **不写一行 TS**。

## 安全（改架构前先读）

- skill 是**服务端控制的可执行逻辑**，永远不接受客户端上传/指定 skill 文件。
- 工具走白名单（frontmatter `tools`），默认空；**不要**给 `Bash`、任意文件写这类工具。
- `settingSources: []`：网关不读宿主机 CLAUDE.md/settings，行为确定。
- 鉴权只有一个共享 Bearer token（`GATEWAY_TOKEN`）；做多用户时换成按用户的 token，
  并把 sessionId 按 userId 隔离（`resume` 会接着别人的会话——现在靠 token 挡）。
- API key 只在服务端。
