import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // AGP 9 的 KMP 库插件：同一个模块里同时出 android / desktop / ios 产物
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.compose")
}

// Apple target 只能在 macOS host 上编（Kotlin/Native 限制）。
// Windows/Linux 上不声明，省得每次构建都被 native 工具链绊倒；CI 的 macOS job 负责编 iOS。
val hostIsMac = System.getProperty("os.name").startsWith("Mac")

kotlin {
    jvmToolchain(17)

    android {
        namespace = "com.androidkit.app.kit"
        compileSdk {
            version = release(37) { minorApiLevel = 1 }
        }
        minSdk = 26
    }

    jvm("desktop") {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    if (hostIsMac) {
        // framework：Xcode 侧要 import 的产物（baseName 就是 Xcode 里 import 的名字）
        listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
            target.binaries.framework {
                baseName = "Kit"
                isStatic = true
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform（包名与 androidx.compose.* 一致，组件代码不用改 import）
            // api：组件的公开签名里就是 Compose 类型，下游必须能看到
            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)
            api(compose.material3)
            api(compose.materialIconsExtended)
            api(compose.components.resources)
            // @Preview 注解（CMP 版，包名换成 org.jetbrains.compose.ui.tooling.preview）
            api("org.jetbrains.compose.ui:ui-tooling-preview:1.13.0-beta01")
            // Crossfade / AnimatedVisibility
            api("org.jetbrains.compose.animation:animation:1.13.0-beta01")

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
            // api：存储层的公开签名里就是 JsonElement（实体字段），下游必须能看到
            api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")

            // 图片加载（Coil3 是 KMP 版，包名 coil3.*）
            implementation("io.coil-kt.coil3:coil-compose:3.6.3")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.6.3")

            // Ktor：只服务 Coil 的网络图（平台引擎在 androidMain / desktopMain / iosMain 分别挂）。
            // agent 的 HTTP 客户端在 :kit-agent-koog 里（Koog 自带），这边不需要了。
            implementation("io.ktor:ktor-client-core:3.6.1")

            // 文件系统（KMP，替掉 java.io.File）
            implementation("com.squareup.okio:okio:3.18.2")

        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation("androidx.core:core-ktx:1.19.1")
            implementation("androidx.activity:activity-compose:1.13.0")
            implementation("androidx.documentfile:documentfile:1.1.0")
            implementation("io.ktor:ktor-client-okhttp:3.6.1")
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.ktor:ktor-client-okhttp:3.6.1")
            }
        }

        // 非 mac host 上没声明 iOS target，也就没有 iosMain 源集
        if (hostIsMac) {
            val iosMain by getting {
                dependencies {
                    implementation("io.ktor:ktor-client-darwin:3.6.1")
                }
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.androidkit.app.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Android-Kit"
            packageVersion = "1.0.0"
        }
    }
}

// 生成的资源访问器：com.androidkit.app.res.Res.string.xxx
compose.resources {
    packageOfResClass = "com.androidkit.app.res"
    publicResClass = true
}
