import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// 付费 / 会员框架：档位表 + 权益判定 + 额度引擎 + 会员状态持久化 + 付费墙 UI。
//
// 单独一个模块（跟 :kit-agent-koog 一个套路）：不做付费的 app 不用引它。
// 平台支付通道（Play Billing / StoreKit）不在这里 —— 这里只定 BillingProvider 接口，
// 各端 app 自己实现（公共层不留计费，见 rules/KIT-NOTES.md）。
plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    // 付费墙 / 升级页是 Compose 代码，要 compose 编译器插件
    id("org.jetbrains.kotlin.plugin.compose")
    // skill 工具的入参用 @Serializable 数据类声明
    id("org.jetbrains.kotlin.plugin.serialization")
    // compose-resources：本模块自己的文案（不跟 :kit 的混在一起）
    id("org.jetbrains.compose")
    id("io.gitlab.arturbosch.detekt")
    // 公开 API 的二进制兼容性：改了签名就 apiCheck 失败（除非显式更新 api/*.api）
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

// Apple target 只能在 macOS host 上编（Kotlin/Native 限制），和 :kit 保持一致。
val hostIsMac = System.getProperty("os.name").startsWith("Mac")

kotlin {
    jvmToolchain(17)

    android {
        namespace = "com.androidkit.app.membership"
        compileSdk {
            version = release(37) { minorApiLevel = 1 }
        }
        minSdk = 26

        // KMP 的 Android 库默认不带 android resources，compose 资源（.cvr）就进不了 assets，
        // 运行时 stringResource(Res.string.xxx) 直接 MissingResourceException 崩掉。
        androidResources {
            enable = true
        }
    }

    jvm("desktop") {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    if (hostIsMac) {
        listOf(iosX64(), iosArm64(), iosSimulatorArm64())
    }

    sourceSets {
        commonMain.dependencies {
            // compose 组件、文案资源都在 :kit（api 传递下去，本模块直接用）
            api(project(":kit"))
            // 本模块自己有文案资源（compose-resources），必须直接依赖它
            api(compose.components.resources)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            // 会员状态 / 额度计数落在 app 内部存储（okio，三端通用）
            implementation(libs.okio)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        // 落盘要临时目录 —— commonTest 里没有 JVM 的 Files，只有 desktopTest 能测
        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

compose.resources {
    packageOfResClass = "com.androidkit.app.membership.res"
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
