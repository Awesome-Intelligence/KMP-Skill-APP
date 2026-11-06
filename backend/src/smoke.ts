/** 冒烟检查（不花 token，不连网）：skill 能加载、契约是合法 JSON Schema、parseOutput 行为正确。 */
import assert from "node:assert";
import type { AddressInfo } from "node:net";
import { fileURLToPath } from "node:url";
import express from "express";
import { loadSkills, inputError, parseOutput } from "./skills.js";
import { abortOnDisconnect } from "./sse.js";

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
