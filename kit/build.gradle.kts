import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // AGP 9 的 KMP 库插件：同一个模块里同时出 android / desktop / ios 产物
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.compose")
    id("io.gitlab.arturbosch.detekt")
    // 公开 API 的二进制兼容性：改了签名就 apiCheck 失败（除非显式更新 api/*.api）
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
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

        // KMP 的 Android 库默认不带 android resources，compose 资源（.cvr）就进不了 assets，
        // 运行时 stringResource(Res.string.xxx) 直接 MissingResourceException 崩掉。
        // 开了这个开关，compose 资源才会被打进 assets。
        androidResources {
            enable = true
        }
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
            api(libs.compose.ui.tooling.preview)
            // Crossfade / AnimatedVisibility
            api(libs.compose.animation)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            // api：存储层的公开签名里就是 JsonElement（实体字段），下游必须能看到
            api(libs.kotlinx.serialization.json)

            // 图片加载（Coil3 是 KMP 版，包名 coil3.*）
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            // Ktor：Coil 的网络图 + 更新检查的 HTTP 客户端（平台引擎在 androidMain / desktopMain / iosMain 分别挂）。
            // api：HttpUpdateSource / GitHubReleaseSource 的构造参数就是 HttpClient，下游要看得见这个类型。
            api(libs.ktor.client.core)

            // 文件系统（KMP，替掉 java.io.File）
            implementation(libs.okio)

        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.documentfile)
            implementation(libs.ktor.client.okhttp)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.okhttp)
            }
        }

        // 非 mac host 上没声明 iOS target，也就没有 iosMain 源集
        if (hostIsMac) {
            val iosMain by getting {
                dependencies {
                    implementation(libs.ktor.client.darwin)
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
