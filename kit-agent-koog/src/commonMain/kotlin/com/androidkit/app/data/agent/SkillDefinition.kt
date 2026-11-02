package com.androidkit.app.data.agent

/**
 * 一个 skill = 一个本地 agent 的人设 + 工具集（原来在服务端 SKILL.md 里的东西，现在是代码）。
 *
 * 工具是 Koog 原生的 [SkillTool]：入参用 `@Serializable` 数据类声明，schema 自动生成。
 */
data class SkillDefinition(
    val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String,
    val tools: List<SkillTool<*>> = emptyList(),
    val model: String = "claude-opus-5-5",
    val maxTurns: Int = 8, // 一次 send 里最多几轮模型调用，防失控
    val maxTokens: Int = 4096,
)
