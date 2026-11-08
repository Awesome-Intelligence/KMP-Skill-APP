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
npm run smoke          # 不花钱的检查：skill 加载 + 契约校验 + SSE 断连检测
npm run dev            # http://localhost:8787
```

> CI：`.github/workflows/ci.yml` 里的 `Backend (gateway)` job 每次 push 跑 `npm ci` → `npm run check`
> → `npm run smoke` → 起一次真服务器打 `/health` 和 `/v1/skills`。
> 消费方项目没装 backend/ 时这条 job 自动跳过。

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
- API key 只在服务端。
- **日志只记事件类型**（一行一个 JSON，见 `src/log.ts`）：记 userId / skillId / sessionId / 耗时 / 结果，
  **不记对话正文、不记工具返回内容**（那些是用户隐私）。

## 多用户

```bash
# GATEWAY_TOKENS="alice:tok-a,bob:tok-b" —— token 直接认用户
GATEWAY_TOKENS="alice:tok-a,bob:tok-b" npm run dev
```

- `GATEWAY_TOKEN` 是单用户模式（userId 固定 `default`），行为和以前一样。
- `GATEWAY_TOKENS` 是多用户模式，格式 `userId:token` 逗号分隔。
- 两个都没配 = 不鉴权（**仅本机开发**）。
- token 用定长比较（`timingSafeEqual`），不逐字节比。

**session 按 userId 隔离**：`resume` 会让 agent 接着那个 session 继续跑 —— 客户端如果能随便传
别人的 sessionId，就能接着别人的会话问下去（读到他上次的上下文）。所以网关记着
`sessionId → userId`：

| 情况 | 处理 |
|---|---|
| session 是这个用户的 | 正常 resume |
| session 是**别人**的 | 403，直接拒绝 |
| 本进程没见过 | 当新会话开（服务重启过 / 别的实例产的），**绝不拿它去 resume** |

表在内存里，重启就丢 —— 要跨重启 resume 就落盘。

## 限流

每个用户每分钟最多 `RATE_LIMIT_PER_MIN`（默认 60）次请求，超了 429 + `Retry-After: 60`。
一次 invoke 就是一次真金白银的模型请求，这道闸门是账单保险。

## 环境变量

| 变量 | 默认 | 说明 |
|---|---|---|
| `PORT` | 8787 | 端口 |
| `SKILLS_DIR` | `backend/skills/` | skill 目录 |
| `GATEWAY_TOKEN` | 无 | 单用户 token；不配 = 不鉴权（仅本机开发） |
| `GATEWAY_TOKENS` | 无 | 多用户：`userId:token,userId:token` |
| `RATE_LIMIT_PER_MIN` | 60 | 每用户每分钟请求上限 |
| `AGENT_MODEL` | claude-opus-5-5 | 模型 |
| `ANTHROPIC_API_KEY` | 无 | **只在服务端**，客户端不带 |
