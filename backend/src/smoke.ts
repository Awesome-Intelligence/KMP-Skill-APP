/** 冒烟检查（不花 token，不连网）：skill 能加载、契约是合法 JSON Schema、parseOutput 行为正确。 */
import assert from "node:assert";
import type { AddressInfo } from "node:net";
import { fileURLToPath } from "node:url";
import express from "express";
import { loadSkills, inputError, parseOutput } from "./skills.js";
import { abortOnDisconnect } from "./sse.js";
import { mayResume, RateLimiter, userOf } from "./guard.js";

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

const skills = loadSkills(fileURLToPath(new URL("../skills/", import.meta.url)));
assert.ok(skills.size > 0, "skills 目录是空的，至少要有一个 skill");

const summarize = skills.get("summarize");
assert.ok(summarize, "缺 demo skill: summarize");

// input 契约
assert.equal(inputError(summarize!, { text: "你好" }), null);
assert.ok(inputError(summarize!, {}), "空 input 应该被契约拦下");

// output 契约 + 围栏容错
const good = parseOutput(summarize!, '好的，结果如下：\n```json\n{"summary":"短摘要","keywords":["a"]}\n```');
assert.ok(good.ok, `合法输出被误判: ${!good.ok ? good.error : ""}`);
const bad = parseOutput(summarize!, '{"summary":123}');
assert.ok(!bad.ok, "类型不对的 output 应该被拦下");

console.log(`smoke 通过: ${skills.size} 个 skill（${[...skills.keys()].join(", ")}）`);

/**
 * SSE 断连检测：body 读完**不能** abort，客户端真的断开才 abort。
 * （req 的 'close' 在 body 读完就触发，用它会把 agent 立刻掐掉。）
 */
async function checkAbortOnDisconnect() {
  const app = express();
  app.use(express.json());
  let controller: AbortController | undefined;
  app.post("/probe", (_req, res) => {
    controller = abortOnDisconnect(res);
    res.writeHead(200, { "Content-Type": "text/event-stream" });
    res.write("data: open\n\n");
  });
  const server = app.listen(0);
  const port = (server.address() as AddressInfo).port;

  const clientAbort = new AbortController();
  const resp = await fetch(`http://127.0.0.1:${port}/probe`, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: "{}",
    signal: clientAbort.signal,
  });
  // 读到服务端开流为止 —— 此时 body 已解析完
  await resp.body?.getReader().read();

  assert.ok(controller, "probe 没拿到 AbortController");
  assert.equal(controller.signal.aborted, false, "body 一读完就 abort 了 —— 又踩了 req 'close' 的坑");

  clientAbort.abort(); // 客户端断开
  for (let i = 0; i < 50 && !controller.signal.aborted; i++) await sleep(20);
  assert.ok(controller.signal.aborted, "客户端断了但没 abort");

  server.closeAllConnections();
  server.close();
}

await checkAbortOnDisconnect();
console.log("smoke 通过: SSE 断连检测");

/**
 * 安全边界自检：鉴权 → userId、session 归属、限流。
 * 这三条出错的后果是「接着别人的会话跑」或者「刷爆模型账单」，必须挡住。
 */
function checkGuards() {
  // 鉴权
  const multi = "alice:tok-a,bob:tok-b";
  assert.equal(userOf("Bearer tok-a", multi), "alice", "alice 的 token 应该认出 alice");
  assert.equal(userOf("Bearer tok-b", multi), "bob");
  assert.equal(userOf("Bearer tok-c", multi), null, "别人的 token 不能被认出来");
  assert.equal(userOf("", multi), null, "没带 token 不能通过");
  assert.equal(userOf("Bearer x", undefined, "single"), null, "单用户模式也要验 token");
  assert.equal(userOf("Bearer single", undefined, "single"), "default");
  assert.equal(userOf("", undefined, undefined), "default", "没配 token = 本机开发，放行");

  // session 归属
  const registry = new Map<string, string>([["s1", "alice"]]);
  assert.equal(mayResume(registry, "s1", "alice"), "resume", "自己的 session 要能接着跑");
  assert.equal(mayResume(registry, "s1", "bob"), "forbidden", "别人的 session 必须拒绝");
  assert.equal(mayResume(registry, "s-unknown", "bob"), "new", "没见过的 session 当新的，别 resume");

  // 限流
  const limiter = new RateLimiter(3, () => 1000);
  assert.equal(limiter.hit("u"), false);
  assert.equal(limiter.hit("u"), false);
  assert.equal(limiter.hit("u"), false);
  assert.equal(limiter.hit("u"), true, "第 4 次要超限");
  assert.equal(limiter.hit("other"), false, "限流要按 key 分开");
}

checkGuards();
console.log("smoke 通过: 鉴权 / session 归属 / 限流");
