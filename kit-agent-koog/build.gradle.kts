import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Koog 引擎：agent loop 交给 Koog（JetBrains 的 KMP agent 框架）跑，工具/技能还是 kit 自己那套。
// 单独一个模块：不想背 Koog 这份依赖的 app 不用引它。
plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    // skill 的工具入参用 @Serializable 数据类声明
    id("org.jetbrains.kotlin.plugin.serialization")
    id("io.gitlab.arturbosch.detekt")
    // 公开 API 的二进制兼容性：改了签名就 apiCheck 失败（除非显式更新 api/*.api）
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

// Apple target 只能在 macOS host 上编（Kotlin/Native 限制），和 :kit 保持一致。
val hostIsMac = System.getProperty("os.name").startsWith("Mac")

kotlin {
    jvmToolchain(17)

    android {
        namespace = "com.androidkit.app.agent"
        compileSdk {
            version = release(37) { minorApiLevel = 1 }
        }
        minSdk = 26
    }

    jvm("desktop") {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    if (hostIsMac) {
        listOf(iosX64(), iosArm64(), iosSimulatorArm64())
    }

    sourceSets {
        commonMain.dependencies {
            // 聊天消息模型 + 聊天 UI 在 :kit（通用组件，不含 agent 逻辑）：
            // ChatLog 要把 agent 事件映射成可渲染的消息列表，所以这层依赖 :kit。
            api(project(":kit"))

            // 网关运行时的 HTTP/SSE 客户端。api：GatewayAgentRuntime 的公开构造参数就是 HttpClient
            api(libs.ktor.client.core)
            // Koog 1.3.0：agent loop + Anthropic 客户端 + 事件钩子
            api(libs.koog.agents.core)
            api(libs.koog.agents.tools)
            api(libs.koog.anthropic.client)
            api(libs.koog.event.handler)
            // ChatMemory：多轮对话历史的官方实现（按 sessionId 存取）
            api(libs.koog.memory)
            // Koog 的 HTTP 客户端实现（Ktor），跟 :kit 用同一套引擎依赖
            implementation(libs.koog.http.ktor)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        // 端到端测试要真起一个 HTTP SSE 端点：桌面端用 OkHttp 引擎
        val desktopTest by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        if (hostIsMac) {
            val iosMain by getting {
                dependencies {
                    implementation(libs.ktor.client.darwin)
                }
            }
        }
    }
}

detekt {
    // 规则集在 config/detekt/detekt.yml（根目录，共享给所有模块）
    // buildUponDefaultConfig 只能写在 Gradle 侧：yml 里没这个 key，写进去会被校验判为非法属性
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    // KMP 的源码不在 src/main/java 下，detekt 默认扫不到 —— 手动列出来
    source.setFrom(
        fileTree("src") { include("**/kotlin/**/*.kt") },
    )
}

apiValidation {
    // 只校验 JVM（desktop）那一份 API。
    // klib（Kotlin/Native）那份只在 macOS 上才编得出来 —— 在 Windows 上生成基线，
    // 到 macOS 上 apiCheck 就会因为「基线里没有 ios 的 klib」而失败。
    // ponytail: 只跑 JVM 一份，跨平台一致；等 CI 稳定了再补 klib。
    klib {
        enabled = false
    }
}
