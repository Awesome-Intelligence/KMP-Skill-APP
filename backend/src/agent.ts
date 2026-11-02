/**
 * agent 执行：skill + input → SDK agent loop → 网关事件流。
 *
 * 事件（SSE data 的 JSON，与 kit 的 AgentEvent 一一对应）：
 *   { type: "start",  sessionId }
 *   { type: "text",   text }     // 一条完整的 assistant 文本消息
 *   { type: "tool",   name }     // 模型调了某个工具
 *   { type: "result", output }   // 已按 output schema 校验的 JSON
 *   { type: "error",  message }
 */
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { query } from "@anthropic-ai/claude-agent-sdk";
import type { Skill } from "./skills.js";
import { parseOutput } from "./skills.js";

export type GatewayEvent =
  | { type: "start"; sessionId: string }
  | { type: "text"; text: string }
  | { type: "tool"; name: string }
  | { type: "result"; output: unknown }
  | { type: "error"; message: string };

export interface InvokeResult {
  sessionId: string;
}

export async function invokeSkill(
  skill: Skill,
  input: unknown,
  sessionId: string | undefined,
  onEvent: (e: GatewayEvent) => void,
  signal?: AbortSignal
): Promise<InvokeResult> {
  // 每个 skill 有自己的输出契约，拼进 system prompt；输出 JSON 由服务端 parseOutput 兜底校验
  const systemPrompt =
    `${skill.prompt}\n\n` +
    `## 输出要求（必须遵守）\n` +
    `你的最终回复必须是且仅是一个 JSON 对象（可以放在 \`\`\`json 围栏里），` +
    `不要输出 JSON 以外的内容。它必须符合这个 JSON Schema：\n` +
    JSON.stringify(skill.outputSchema);

  const abortController = new AbortController();
  signal?.addEventListener("abort", () => abortController.abort(), { once: true });

  // 隔离的工作目录：agent 不碰仓库和用户文件
  const workdir = fs.mkdtempSync(path.join(os.tmpdir(), "agent-gw-"));
  let resultSessionId = sessionId ?? "";

  try {
    const stream = query({
      prompt: `输入参数：\n${JSON.stringify(input, null, 2)}`,
      options: {
        systemPrompt,
        allowedTools: skill.tools,
        maxTurns: skill.maxTurns ?? 8,
        model: skill.model ?? process.env.AGENT_MODEL ?? "claude-opus-5-5",
        settingSources: [], // ponytail: 不读宿主机 CLAUDE.md/settings，网关行为要确定
        cwd: workdir,
        resume: sessionId,
        abortController,
      },
    });

    for await (const message of stream) {
      if (message.type === "system" && message.subtype === "init") {
        resultSessionId = message.session_id;
        onEvent({ type: "start", sessionId: message.session_id });
      } else if (message.type === "assistant") {
        for (const block of message.message.content) {
          if (block.type === "text") {
            onEvent({ type: "text", text: block.text });
          } else if (block.type === "tool_use") {
            onEvent({ type: "tool", name: block.name });
          }
        }
      } else if (message.type === "result") {
        if (message.subtype === "success") {
          const parsed = parseOutput(skill, message.result ?? "");
          if (parsed.ok) {
            onEvent({ type: "result", output: parsed.output });
          } else {
            onEvent({ type: "error", message: parsed.error });
          }
        } else {
          onEvent({ type: "error", message: `agent 执行失败 (${message.subtype})` });
        }
      }
      // user（tool 结果）/ stream_event 等消息：v1 不下发
    }
  } finally {
    fs.rmSync(workdir, { recursive: true, force: true });
  }

  return { sessionId: resultSessionId };
}
