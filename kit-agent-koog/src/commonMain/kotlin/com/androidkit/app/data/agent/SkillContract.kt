package com.androidkit.app.data.agent

/**
 * skill 的「体检」——把 rules/CLAUDE.template.md 里的规矩变成能跑的检查。
 *
 * 为什么需要它：skill 是**运行期**的东西（人设 + 工具集），编译器不管它。
 * 工具名打错、两个工具重名、description 写空 —— 这些错编译得过、运行起来才发现
 * （模型挑不到工具、或者挑错了）。
 *
 * 用法（写在 app 的单元测试里）：
 * ```kotlin
 * @Test
 * fun `skill 符合契约`() {
 *     assertEquals(emptyList(), todoSkill(store, "claude-opus-5-5").violations())
 * }
 * ```
 *
 * 返回**违反项**而不是直接断言，是为了让你能按自己的需要放宽某一条
 * （比如某个只读工具允许 description 为空）：
 * ```kotlin
 * val problems = skill.violations().filterNot { it.rule == SkillViolation.Rule.EMPTY_TOOL_DESCRIPTION }
 * assertEquals(emptyList(), problems)
 * ```
 */
data class SkillViolation(
    /** 违反了哪条规则 */
    val rule: Rule,
    /** 说清楚是哪儿错了 */
    val detail: String,
) {
    /** 规则编号。稳定不变，方便测试里引用。 */
    enum class Rule {
        /** skill 的 id 为空 */
        EMPTY_ID,

        /** skill 的 id 不是 snake_case */
        ID_NOT_SNAKE_CASE,

        /** skill 名字为空 */
        EMPTY_NAME,

        /** skill 描述为空 */
        EMPTY_DESCRIPTION,

        /** systemPrompt 为空 —— 模型没有角色设定 */
        EMPTY_SYSTEM_PROMPT,

        /** 两个工具重名 —— 模型只能挑一个，另一个永远调不到 */
        DUPLICATE_TOOL_NAME,

        /** 工具名不是英文 snake_case */
        TOOL_NAME_NOT_SNAKE_CASE,

        /** 工具 description 为空 —— 模型不知道这个工具干什么 */
        EMPTY_TOOL_DESCRIPTION,

        /** maxTurns / maxTokens 非正 —— agent loop 跑不起来 */
        INVALID_LIMITS,
    }
}

/**
 * 跑一遍 skill 的体检，返回所有违反项（空列表 = 合格）。
 *
 * 检查项刻意只覆盖**「错了就一定出问题」**的硬规则：
 * 措辞好不好、prompt 写没写清楚 —— 那是人读的，不归机器判。
 */
fun SkillDefinition.violations(): List<SkillViolation> {
    val out = mutableListOf<SkillViolation>()

    if (id.isBlank()) {
        out += SkillViolation(V.EMPTY_ID, "skill.id 为空")
    } else if (!SNAKE_CASE.matches(id)) {
        out += SkillViolation(V.ID_NOT_SNAKE_CASE, "skill.id \"$id\" 不是 snake_case")
    }

    if (name.isBlank()) out += SkillViolation(V.EMPTY_NAME, "skill.name 为空")
    if (description.isBlank()) out += SkillViolation(V.EMPTY_DESCRIPTION, "skill.description 为空")
    if (systemPrompt.isBlank()) out += SkillViolation(V.EMPTY_SYSTEM_PROMPT, "skill.systemPrompt 为空")

    if (maxTurns <= 0) out += SkillViolation(V.INVALID_LIMITS, "maxTurns=$maxTurns，应该 > 0")
    if (maxTokens <= 0) out += SkillViolation(V.INVALID_LIMITS, "maxTokens=$maxTokens，应该 > 0")

    tools.groupBy { it.name }.filterValues { it.size > 1 }.forEach { (name, group) ->
        out += SkillViolation(V.DUPLICATE_TOOL_NAME, "工具名 \"$name\" 重复了 ${group.size} 次")
    }

    tools.forEach { tool ->
        if (!SNAKE_CASE.matches(tool.name)) {
            out += SkillViolation(V.TOOL_NAME_NOT_SNAKE_CASE, "工具名 \"${tool.name}\" 不是 snake_case")
        }
        // description 在 ToolDescriptor 上（Koog 的 ToolBase 只暴露 name）
        if (tool.descriptor.description.isBlank()) {
            out += SkillViolation(V.EMPTY_TOOL_DESCRIPTION, "工具 \"${tool.name}\" 的 description 为空")
        }
    }

    return out
}

/** 模型靠 name 挑工具 —— 驼峰、带空格都会被模型挑错。 */
private val SNAKE_CASE = Regex("^[a-z][a-z0-9_]*$")

/** 缩短下面的断言行 —— 规则枚举嵌套在 data class 里，全名太长。 */
private typealias V = SkillViolation.Rule
