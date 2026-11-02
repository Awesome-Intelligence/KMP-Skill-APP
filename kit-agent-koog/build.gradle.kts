import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Koog 引擎：agent loop 交给 Koog（JetBrains 的 KMP agent 框架）跑，工具/技能还是 kit 自己那套。
// 单独一个模块：不想背 Koog 这份依赖的 app 不用引它。
plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    // skill 的工具入参用 @Serializable 数据类声明
    id("org.jetbrains.kotlin.plugin.serialization")
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
            // Koog 1.3.0：agent loop + Anthropic 客户端 + 事件钩子
            api("ai.koog:agents-core:1.3.0")
            api("ai.koog:agents-tools:1.3.0")
            api("ai.koog:prompt-executor-anthropic-client:1.3.0")
            api("ai.koog:agents-features-event-handler:1.3.0")
            // ChatMemory：多轮对话历史的官方实现（按 sessionId 存取）
            api("ai.koog:agents-features-memory:1.3.0")
            // Koog 的 HTTP 客户端实现（Ktor），跟 :kit 用同一套引擎依赖
            implementation("ai.koog:http-client-ktor:1.3.0")

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:3.6.1")
        }

        val desktopMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:3.6.1")
            }
        }

        if (hostIsMac) {
            val iosMain by getting {
                dependencies {
                    implementation("io.ktor:ktor-client-darwin:3.6.1")
                }
            }
        }
    }
}
