/**
 * agent 网关 HTTP 层。三条路由，完了：
 *   GET  /health
 *   GET  /v1/skills                     → skill 列表（id/name/description）
 *   POST /v1/skills/:id/invoke          → SSE 事件流（start/text/tool/result/error）
 *
 * 鉴权：配置了 GATEWAY_TOKEN 时，所有 /v1 请求必须带 Authorization: Bearer <token>。
 * 多用户：GATEWAY_TOKENS="alice:tok1,bob:tok2" —— token 直接认用户（userId）。
 *
 * 三条安全线（改这块之前先读 SECURITY.md）：
 *   1. session 按 userId 隔离 —— resume 会接着别人的会话跑，不能让客户端随便传 sessionId
 *   2. 日志只记事件类型，不记对话正文
 *   3. 危险工具走白名单，闸门在工具执行前拦
 */
import "./env.js";
import express, { type Request, type Response } from "express";
import { fileURLToPath } from "node:url";
import { loadSkills, inputError } from "./skills.js";
import { invokeSkill, type GatewayEvent } from "./agent.js";
import { abortOnDisconnect } from "./sse.js";
import { mayResume, RateLimiter, userOf } from "./guard.js";
import { log } from "./log.js";

const PORT = Number(process.env.PORT ?? 8787);
const SKILLS_DIR = process.env.SKILLS_DIR || fileURLToPath(new URL("../skills/", import.meta.url));
// 每个用户每分钟最多几次 invoke —— agent 一次调用就是一次真金白银的模型请求
const RATE_LIMIT_PER_MIN = Number(process.env.RATE_LIMIT_PER_MIN ?? 60);

const skills = loadSkills(SKILLS_DIR);
log.info("skills.loaded", { dir: SKILLS_DIR, count: skills.size, ids: [...skills.keys()] });

const app = express();
app.use(express.json({ limit: "1mb" }));

const limiter = new RateLimiter(RATE_LIMIT_PER_MIN);

/**
 * sessionId → 拥有它的 userId。
 *
 * ponytail: 内存表，服务重启就丢（重启后客户端带来的 sessionId 一律当新会话）。
 *         要跨重启 resume 就落盘（sessionId → userId 一行一条）。
 */
const sessionOwner = new Map<string, string>();

// ── 路由 ─────────────────────────────────────────────────────

/** 认出用户 + 限流；认不出就自己回 401/429 并返回 null。 */
function requireUser(req: Request, res: Response): string | null {
  const userId = userOf(
    req.headers.authorization,
    process.env.GATEWAY_TOKENS,
    process.env.GATEWAY_TOKEN
  );
  if (!userId) {
    log.warn("auth.fail", { path: req.path });
    res.status(401).json({ error: "缺少或错误的 Bearer token" });
    return null;
  }
  if (limiter.hit(userId)) {
    log.warn("rate.limit", { userId, path: req.path });
    res.setHeader("Retry-After", "60");
    res.status(429).json({ error: `每分钟最多 ${RATE_LIMIT_PER_MIN} 次` });
    return null;
  }
  return userId;
}

app.get("/health", (_req, res) => res.json({ ok: true }));

app.get("/v1/skills", (req, res) => {
  if (!requireUser(req, res)) return;
  res.json([...skills.values()].map((s) => ({ id: s.id, name: s.name, description: s.description })));
});

app.post("/v1/skills/:id/invoke", (req, res) => {
  const userId = requireUser(req, res);
  if (!userId) return;

  const skill = skills.get(req.params.id);
  if (!skill) {
    res.status(404).json({ error: `skill 不存在: ${req.params.id}` });
    return;
  }

  const { input, sessionId } = req.body ?? {};
  const bad = inputError(skill, input);
  if (bad) {
    res.status(400).json({ error: bad });
    return;
  }

  // 客户端要接着上次的会话跑 —— 先验归属
  const requested = typeof sessionId === "string" && sessionId ? sessionId : undefined;
  let resumeId: string | undefined;
  if (requested) {
    const decision = mayResume(sessionOwner, requested, userId);
    if (decision === "forbidden") {
      log.warn("session.forbidden", { userId, skillId: skill.id });
      res.status(403).json({ error: "session 不属于当前用户" });
      return;
    }
    if (decision === "resume") {
      resumeId = requested;
    } else {
      // 本进程没见过这个 session（服务重启过 / 别的实例产的）—— 当新会话，绝不拿它去 resume
      log.warn("session.unknown", { userId, skillId: skill.id });
    }
  }

  // SSE 头
  res.writeHead(200, {
    "Content-Type": "text/event-stream",
    "Cache-Control": "no-cache",
    Connection: "keep-alive",
  });

  const startedAt = Date.now();
  let outcome = "ok";
  let activeSessionId = resumeId ?? "";
  const send = (e: GatewayEvent) => {
    if (e.type === "start") {
      activeSessionId = e.sessionId;
      sessionOwner.set(e.sessionId, userId);
    }
    if (e.type === "error") outcome = "error";
    res.write(`event: ${e.type}\ndata: ${JSON.stringify(e)}\n\n`);
  };

  // 审计日志：记事件类型和标识符，不记正文
  log.info("invoke.start", { userId, skillId: skill.id, resumed: resumeId !== undefined });

  // 客户端断开 → 中止 agent loop（挂 res，不挂 req，原因见 sse.ts）
  const abort = abortOnDisconnect(res);

  invokeSkill(skill, input, resumeId, send, abort.signal)
    .catch((e: unknown) => {
      outcome = "error";
      if (!abort.signal.aborted) send({ type: "error", message: (e as Error).message });
    })
    .finally(() => {
      log.info("invoke.end", {
        userId,
        skillId: skill.id,
        sessionId: activeSessionId,
        ms: Date.now() - startedAt,
        outcome,
      });
      res.end();
    });
});

// 兜底错误处理：别把栈信息回给客户端（里面可能有路径、配置），只记日志
app.use((err: unknown, _req: Request, res: Response, _next: express.NextFunction) => {
  log.error("http.error", {
    error: err instanceof Error ? err.message : String(err),
    stack: err instanceof Error ? err.stack : undefined,
  });
  if (!res.headersSent) res.status(500).json({ error: "内部错误" });
});

// 进程级兜底：崩了要留下痕迹（崩溃报告的最小实现 —— 不引第三方 SDK，
// 免得把用户的对话内容发给外网）
process.on("unhandledRejection", (reason) => {
  log.error("unhandledRejection", { error: String(reason) });
});
process.on("uncaughtException", (err) => {
  log.error("uncaughtException", { error: err.message, stack: err.stack });
  process.exit(1);
});

app.listen(PORT, () => {
  log.info("gateway.listening", {
    port: PORT,
    auth: process.env.GATEWAY_TOKENS ? "multi" : process.env.GATEWAY_TOKEN ? "on" : "off",
    rateLimitPerMin: RATE_LIMIT_PER_MIN,
  });
});
