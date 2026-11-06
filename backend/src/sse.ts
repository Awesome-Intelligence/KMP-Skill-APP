import type { Response } from "express";

/**
 * 客户端断开 → 中止 agent loop。
 *
 * 挂 res 的 'close'，**不要挂 req 的**：Node 16+ 里 req 的 'close' 是"请求体读完"，
 * express.json() 解析完 body 就触发 —— 用它会在 SSE 刚开始时把 agent 立刻掐掉。
 * res 的 'close' 只在连接真的断了（或响应结束时）才触发。
 */
export function abortOnDisconnect(res: Response): AbortController {
  const abort = new AbortController();
  res.on("close", () => {
    if (!res.writableEnded) abort.abort();
  });
  return abort;
}
