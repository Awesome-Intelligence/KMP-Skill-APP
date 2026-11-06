/**
 * agent 网关 HTTP 层。三条路由，完了：
 *   GET  /health
 *   GET  /v1/skills                     → skill 列表（id/name/description）
 *   POST /v1/skills/:id/invoke          → SSE 事件流（start/text/tool/result/error）
 *
 * 鉴权：配置了 GATEWAY_TOKEN 时，所有 /v1 请求必须带 Authorization: Bearer <token>。
 */
import "./env.js";
import express, { type Request, type Response } from "express";
import { fileURLToPath } from "node:url";
import { loadSkills, inputError } from "./skills.js";
import { invokeSkill, type GatewayEvent } from "./agent.js";
import { abortOnDisconnect } from "./sse.js";

const PORT = Number(process.env.PORT ?? 8787);
const SKILLS_DIR = process.env.SKILLS_DIR || fileURLToPath(new URL("../skills/", import.meta.url));

const skills = loadSkills(SKILLS_DIR);
console.log(`skills 加载完成: ${[...skills.keys()].join(", ") || "(空)"} ← ${SKILLS_DIR}`);

const app = express();
app.use(express.json({ limit: "1mb" }));

function authed(req: Request): boolean {
  const token = process.env.GATEWAY_TOKEN;
  if (!token) return true; // 未配置 = 不鉴权（仅本机开发）
  return req.headers.authorization === `Bearer ${token}`;
}

function requireAuth(req: Request, res: Response): boolean {
  if (authed(req)) return true;
  res.status(401).json({ error: "缺少或错误的 Bearer token" });
  return false;
}

app.get("/health", (_req, res) => res.json({ ok: true }));

app.get("/v1/skills", (req, res) => {
  if (!requireAuth(req, res)) return;
  res.json([...skills.values()].map((s) => ({ id: s.id, name: s.name, description: s.description })));
});

app.post("/v1/skills/:id/invoke", (req, res) => {
  if (!requireAuth(req, res)) return;
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

  // SSE 头
  res.writeHead(200, {
    "Content-Type": "text/event-stream",
    "Cache-Control": "no-cache",
    Connection: "keep-alive",
  });
  const send = (e: GatewayEvent) => {
    res.write(`event: ${e.type}\ndata: ${JSON.stringify(e)}\n\n`);
  };

  // 客户端断开 → 中止 agent loop（挂 res，不挂 req，原因见 sse.ts）
  const abort = abortOnDisconnect(res);

  invokeSkill(skill, input, sessionId, send, abort.signal)
    .catch((e: unknown) => {
      if (!abort.signal.aborted) send({ type: "error", message: (e as Error).message });
    })
    .finally(() => res.end());
});

app.listen(PORT, () => {
  console.log(`agent gateway → http://localhost:${PORT} (鉴权: ${process.env.GATEWAY_TOKEN ? "开" : "关"})`);
});
