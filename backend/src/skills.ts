/**
 * skill 加载：skills/<id>/SKILL.md → { id, meta, body, validateInput, validateOutput }
 *
 * 约定（通用架构，不含业务）：
 *   - frontmatter 必须有 name / description / input / output
 *   - input / output 是 JSON Schema（object），服务端双向校验
 *   - body 是给 agent 的 prompt（skill 的"逻辑"就在这段话里）
 */
import fs from "node:fs";
import path from "node:path";
import matter from "gray-matter";
import { Ajv, type ValidateFunction } from "ajv";

export interface Skill {
  id: string; // 目录名
  name: string;
  description: string;
  model?: string; // 覆盖默认模型
  maxTurns?: number; // agent 最大轮数，防失控
  tools: string[]; // 工具白名单；空 = 纯对话，不给任何工具
  inputSchema: object; // JSON Schema，校验请求里的 input
  outputSchema: object; // JSON Schema，校验 agent 最终输出
  prompt: string; // SKILL.md 正文
  validateInput: ValidateFunction;
  validateOutput: ValidateFunction;
}

const ajv = new Ajv({ allErrors: true });

function loadSkill(dir: string, id: string): Skill {
  const file = path.join(dir, id, "SKILL.md");
  const raw = matter(fs.readFileSync(file, "utf8"));
  const m = raw.data as Record<string, unknown>;

  for (const key of ["name", "description", "input", "output"]) {
    if (m[key] === undefined) throw new Error(`${file}: frontmatter 缺少 ${key}`);
  }
  const inputSchema = m.input as object;
  const outputSchema = m.output as object;

  return {
    id,
    name: String(m.name),
    description: String(m.description),
    model: m.model as string | undefined,
    maxTurns: m.maxTurns as number | undefined,
    tools: Array.isArray(m.tools) ? (m.tools as string[]) : [],
    inputSchema,
    outputSchema,
    prompt: raw.content.trim(),
    validateInput: ajv.compile(inputSchema),
    validateOutput: ajv.compile(outputSchema),
  };
}

export function loadSkills(dir: string): Map<string, Skill> {
  const skills = new Map<string, Skill>();
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (!entry.isDirectory()) continue;
    if (!fs.existsSync(path.join(dir, entry.name, "SKILL.md"))) continue;
    const skill = loadSkill(dir, entry.name);
    skills.set(skill.id, skill);
  }
  return skills;
}

/** 校验请求 input；不通过返回错误文案（直接进 400） */
export function inputError(skill: Skill, input: unknown): string | null {
  if (!skill.validateInput(input)) {
    return `input 不符合 ${skill.id} 的契约: ${ajv.errorsText(skill.validateInput.errors)}`;
  }
  return null;
}

/**
 * 从模型回复里抠出 JSON 并按 output schema 校验。
 * 允许 ```json 围栏和前后闲话 —— prompt 已经要求只输出 JSON，这里兜底。
 */
export function parseOutput(
  skill: Skill,
  text: string
): { ok: true; output: unknown } | { ok: false; error: string } {
  const fenced = text.match(/```(?:json)?\s*([\s\S]*?)```/);
  const candidate = (fenced ? fenced[1] : text).trim();
  // 找第一个 { 到最后一个 }，中间才是 JSON
  const start = candidate.indexOf("{");
  const end = candidate.lastIndexOf("}");
  if (start < 0 || end <= start) return { ok: false, error: "输出里没有 JSON 对象" };

  let parsed: unknown;
  try {
    parsed = JSON.parse(candidate.slice(start, end + 1));
  } catch (e) {
    return { ok: false, error: `JSON 解析失败: ${(e as Error).message}` };
  }
  if (!skill.validateOutput(parsed)) {
    return { ok: false, error: `output 不符合 ${skill.id} 的契约: ${ajv.errorsText(skill.validateOutput.errors)}` };
  }
  return { ok: true, output: parsed };
}
