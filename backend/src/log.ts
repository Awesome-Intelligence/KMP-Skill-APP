/**
 * 结构化日志：一行一个 JSON。
 *
 * 为什么不用 console.log("...")：
 *   - 字符串日志没法机器聚合（按 event 类型统计、报警）
 *   - 拼字符串容易漏字段，JSON 强制字段名固定
 *
 * 红线（见 SECURITY.md）：**只记事件类型和标识符，不记对话正文、不记工具返回内容**。
 * 工具返回里可能有用户的待办正文、联系人、日历内容 —— 那些不进日志。
 */
export type LogFields = Record<string, unknown>;

function emit(level: "info" | "warn" | "error", event: string, fields?: LogFields): void {
  const line = JSON.stringify({ ts: new Date().toISOString(), level, event, ...fields });
  if (level === "error") console.error(line);
  else console.log(line);
}

export const log = {
  info: (event: string, fields?: LogFields) => emit("info", event, fields),
  warn: (event: string, fields?: LogFields) => emit("warn", event, fields),
  error: (event: string, fields?: LogFields) => emit("error", event, fields),
};
