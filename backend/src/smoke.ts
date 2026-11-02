/** 冒烟检查（不花 token，不连网）：skill 能加载、契约是合法 JSON Schema、parseOutput 行为正确。 */
import assert from "node:assert";
import { fileURLToPath } from "node:url";
import { loadSkills, inputError, parseOutput } from "./skills.js";

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
