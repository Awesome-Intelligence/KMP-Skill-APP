package com.androidkit.app.data.agent.gateway

import com.androidkit.app.data.agent.AgentEvent
import com.androidkit.app.data.agent.SkillDefinition
import com.androidkit.app.data.agent.ToolApprover
import com.sun.net.httpserver.HttpServer
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 端到端：真起一个 HTTP 服务发 SSE，验证 Ktor 客户端 + 手拆 SSE + 事件映射整条链路。
 *
 * 用 JDK 自带的 `com.sun.net.httpserver`，不引 Ktor server / 测试框架依赖。
 * 之所以要这一层：`GatewaySseTest` 只测解析，`GatewayAgentRuntimeTest` 只测映射，
 * 真正的「HTTP 请求 → 状态码 → 逐行读流」只有这里能兜住。
 */
class GatewayHttpTest {

    private lateinit var server: HttpServer
    private val baseUrl: String get() = "http://127.0.0.1:${server.address.port}"
    private val authHeader = AtomicReference<String?>(null)

    private val skill = SkillDefinition(id = "chat", name = "对话", description = "", systemPrompt = "")

    @BeforeTest
    fun startServer() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/v1/skills/chat/invoke") { exchange ->
                authHeader.set(exchange.requestHeaders.getFirst("Authorization"))
                exchange.responseHeaders.add("Content-Type", "text/event-stream")
                exchange.sendResponseHeaders(200, 0)
                exchange.responseBody.bufferedWriter().use { writer ->
                    writer.write("event: start\ndata: {\"type\":\"start\",\"sessionId\":\"s-1\"}\n\n")
                    writer.write("event: text\ndata: {\"type\":\"text\",\"text\":\"你好\"}\n\n")
                    writer.write("event: result\ndata: {\"type\":\"result\",\"output\":{\"text\":\"完成\"}}\n\n")
                    writer.flush()
                }
            }
            createContext("/v1/skills/broken/invoke") { exchange ->
                val body = """{"error":"skill 不存在"}""".toByteArray()
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(404, body.size.toLong())
                exchange.responseBody.use { it.write(body) }
            }
            start()
        }
    }

    @AfterTest
    fun stopServer() {
        server.stop(0)
    }

    @Test
    fun 真实HTTP_SSE端到端() = runBlocking {
        val client = HttpClient(OkHttp)
        try {
            val session = GatewayAgentRuntime(baseUrl, "tok", client).session(skill, ToolApprover.AllowAll)
            val events = session.send("嗨").toList()

            assertEquals("Bearer tok", authHeader.get())
            assertEquals(listOf("你好"), events.filterIsInstance<AgentEvent.Text>().map { it.text })
            assertEquals("完成", events.filterIsInstance<AgentEvent.Result>().single().text)
        } finally {
            client.close()
        }
    }

    @Test
    fun 非2xx_保留状态码和服务端错误文本() = runBlocking {
        val client = HttpClient(OkHttp)
        try {
            val events = GatewayAgentRuntime(baseUrl, "tok", client)
                .session(skill.copy(id = "broken"), ToolApprover.AllowAll)
                .send("嗨")
                .toList()

            val message = events.filterIsInstance<AgentEvent.Error>().single().message
            assertTrue(message.contains("404"), "错误里应带状态码，实际=$message")
            assertTrue(message.contains("skill 不存在"), "错误里应带服务端文本，实际=$message")
        } finally {
            client.close()
        }
    }
}
