package com.androidkit.app.data.update

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

/**
 * 从静态 JSON 清单读版本信息（[UpdateManifest] 的字段说明里有格式）。
 *
 * [client] 由 App 提供，为什么不在 kit 里 new 一个：
 * Ktor 的引擎是平台相关的（Android/Desktop 用 OkHttp、iOS 用 Darwin），
 * 而 kit 的硬规矩是**不写 expect/actual** —— 引擎和超时策略归 App 管。
 *
 * 注意：清单地址必须是 HTTPS。更新通道本身是攻击面 —— 清单被篡改就能把用户引到恶意包。
 */
class HttpUpdateSource(
    private val client: HttpClient,
    private val manifestUrl: String,
) : UpdateSource {

    override suspend fun fetch(): UpdateManifest {
        val response = client.get(manifestUrl) {
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
            header(HttpHeaders.UserAgent, USER_AGENT)
        }
        check(response.status.isSuccess()) { "版本清单请求失败：HTTP ${response.status.value}" }
        return json.decodeFromString(response.bodyAsText())
    }

    private companion object {
        const val USER_AGENT = "Android-Kit-UpdateChecker"

        // ignoreUnknownKeys：以后清单加字段，旧版 App 照样能读
        val json = Json { ignoreUnknownKeys = true }
    }
}
