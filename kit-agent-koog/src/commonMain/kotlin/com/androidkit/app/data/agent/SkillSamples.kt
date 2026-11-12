package com.androidkit.app.data.agent

import ai.koog.serialization.JSONSerializer
import ai.koog.serialization.kotlinx.KotlinxSerializer
import ai.koog.serialization.kotlinx.toKoogJSONObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

/**
 * 一条黄金样例 —— **skill 化没有编译期保障，样例就是它的「类型检查」**。
 *
 * 样例钉的是**工具这一层**：模型应该发出哪个工具调用、工具该返回什么。
 * 模型那句自然语言（"帮我记一下买牛奶"）没法离线判定 —— 那要真调模型、要花钱、
 * 结果还不一定一样。所以样例只钉工具：工具名、入参、返回值。
 *
 * ```kotlin
 * val failures = todoSkill(store, "claude-opus-5-5").runSamples(
 *     listOf(
 *         SkillSample("add_todo", """{"title":"买牛奶"}""", "买牛奶"),
 *         SkillSample("list_todos", "{}", "（还没有待办）"),
 *     ),
 * )
 * assertEquals(emptyList(), failures)
 * ```
 */
data class SkillSample(
    /** 工具名 —— 必须和 skill 里注册的名字一模一样。 */
    val tool: String,
    /** 工具入参的 JSON —— 和模型发的 input_schema 对得上。 */
    val input: String,
    /** 期望工具返回里包含的片段。 */
    val expect: String,
)

/** 一条样例没过。 */
data class SampleFailure(
    val sample: SkillSample,
    /** 说清楚哪儿不对。 */
    val reason: String,
)

/**
 * 跑一组黄金样例，返回失败项（空列表 = 全过）。
 *
 * 工具找不到、入参解不开、返回值对不上 —— 三种都算失败。
 *
 * @param samples 样例表
 * @param approver 审批闸门 —— 默认全放行；样例要验「危险工具会被拦下」就传自己的
 */
suspend fun SkillDefinition.runSamples(
    samples: List<SkillSample>,
    approver: ToolApprover = ToolApprover.AllowAll,
): List<SampleFailure> = samples.mapNotNull { it.run(this, approver) }

private suspend fun SkillSample.run(
    skill: SkillDefinition,
    approver: ToolApprover,
): SampleFailure? {
    val tool = skill.tools.firstOrNull { it.name == tool }
        ?: return SampleFailure(this, "skill 里没有工具 \"$tool\"")

    tool.bind(approver)

    val args = try {
        val element = Json.parseToJsonElement(input).jsonObject
        // decodeArgs 是 Koog 的内部 API（标了 @InternalAgentToolsApi）——
        // 走公开的 serializer.decodeFromJSONElement，一样的活，少一层 opt-in 依赖。
        SERIALIZER.decodeFromJSONElement<Any?>(element.toKoogJSONObject(), tool.argsType)
    } catch (e: Exception) {
        return SampleFailure(this, "入参解不开：${e.message}")
    }

    val result = try {
        tool.executeUntyped(args)
    } catch (e: Exception) {
        return SampleFailure(this, "工具执行失败：${e.message}")
    }

    if (!result.text.contains(expect)) {
        return SampleFailure(this, "返回里没有 \"$expect\"，实际是：${result.text}")
    }
    return null
}

private val SERIALIZER: JSONSerializer = KotlinxSerializer()
