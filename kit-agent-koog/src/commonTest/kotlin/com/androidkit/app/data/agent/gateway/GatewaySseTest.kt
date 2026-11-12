package com.androidkit.app.data.agent.gateway

import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GatewaySseTest {

    @Test
    fun text帧_取text字段() {
        assertEquals(
            GatewayFrame.Text("你好"),
            GatewaySse.parse("text", """{"type":"text","text":"你好"}"""),
        )
    }

    @Test
    fun start帧_带上sessionId() {
        assertEquals(
            GatewayFrame.Start("s-1"),
            GatewaySse.parse("start", """{"type":"start","sessionId":"s-1"}"""),
        )
    }

    @Test
    fun tool帧_只有工具名() {
        assertEquals(
            GatewayFrame.Tool("now"),
            GatewaySse.parse("tool", """{"type":"tool","name":"now"}"""),
        )
    }

    @Test
    fun result帧_保留任意output() {
        assertEquals(
            GatewayFrame.Result(JsonPrimitive("完成")),
            GatewaySse.parse("result", """{"type":"result","output":"完成"}"""),
        )
    }

    @Test
    fun error帧_取message() {
        assertEquals(
            GatewayFrame.Failure("限流了"),
            GatewaySse.parse("error", """{"type":"error","message":"限流了"}"""),
        )
    }

    /** event 行和 JSON 里的 type 一致；JSON 缺 type 时退回 event 行。 */
    @Test
    fun JSON没有type时_退回event行() {
        assertEquals(
            GatewayFrame.Text("回退"),
            GatewaySse.parse("text", """{"text":"回退"}"""),
        )
    }

    @Test
    fun 坏JSON_直接跳过不炸流() {
        assertNull(GatewaySse.parse("text", "{oops"))
        assertNull(GatewaySse.parse("text", "[]"))
        assertNull(GatewaySse.parse("text", ""))
    }

    @Test
    fun 未知类型_跳过() {
        assertNull(GatewaySse.parse("weird", """{"type":"weird"}"""))
    }
}
