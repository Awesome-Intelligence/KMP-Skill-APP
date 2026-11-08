/**
 * 鉴权 / 限流 / session 归属 —— 抽成纯逻辑，好单测（smoke.ts 在测）。
 *
 * 这三样都是安全边界，出错的后果是「别人的会话被接着跑」或者「刷爆模型账单」，
 * 所以规则要能脱离 HTTP 层单独验证。
 */
import { timingSafeEqual } from "node:crypto";

/** "Bearer xxx" → "xxx" */
export function tokenOf(authorization: string | undefined): string {
  return authorization?.startsWith("Bearer ") ? authorization.slice(7) : "";
}

/** 定长比较，别让 token 逐字节比出时间差 */
export function safeEqual(a: string, b: string): boolean {
  const ba = Buffer.from(a);
  const bb = Buffer.from(b);
  return ba.length === bb.length && timingSafeEqual(ba, bb);
}

/**
 * token → userId，认不出返回 null。
 *
 * - multi="alice:tok1,bob:tok2" → 多用户，token 认用户
 * - single="tok"                → 单用户，userId 固定 "default"
 * - 都没有                       → 不鉴权（仅本机开发），userId "default"
 */
export function userOf(
  authorization: string | undefined,
  multi?: string,
  single?: string
): string | null {
  if (multi) {
    const token = tokenOf(authorization);
    for (const pair of multi.split(",")) {
      const [userId, tok] = pair.split(":").map((s) => s.trim());
      if (userId && tok && safeEqual(tok, token)) return userId;
    }
    return null;
  }
  if (!single) return "default";
  return safeEqual(single, tokenOf(authorization)) ? "default" : null;
}

/**
 * 客户端能不能接着某个 session 跑。
 *
 * - "resume"    → 是这个用户的，本进程见过，可以接着跑
 * - "forbidden" → 是**别人**的 session —— 拒绝
 * - "new"       → 本进程没见过（服务重启过 / 别的实例产的）—— 当新会话，绝不拿它去 resume
 *
 * `resume` 会让 agent 接着那个 session 继续跑，所以归属必须验。
 */
export type ResumeDecision = "resume" | "forbidden" | "new";

export function mayResume(
  registry: Map<string, string>,
  sessionId: string,
  userId: string
): ResumeDecision {
  const owner = registry.get(sessionId);
  if (owner === undefined) return "new";
  return owner === userId ? "resume" : "forbidden";
}

/** 固定窗口限流：每个 key 每分钟最多 N 次。 */
export class RateLimiter {
  private readonly buckets = new Map<string, { count: number; resetAt: number }>();

  constructor(
    private readonly limitPerMin: number,
    private readonly now: () => number = Date.now
  ) {}

  /** 记一次，返回 true 表示超限。 */
  hit(key: string): boolean {
    const now = this.now();
    const b = this.buckets.get(key);
    if (!b || now >= b.resetAt) {
      this.buckets.set(key, { count: 1, resetAt: now + 60_000 });
    } else {
      b.count += 1;
    }
    if (this.buckets.size > 1000) {
      for (const [k, v] of this.buckets) if (now >= v.resetAt) this.buckets.delete(k);
    }
    return (this.buckets.get(key)?.count ?? 0) > this.limitPerMin;
  }
}
