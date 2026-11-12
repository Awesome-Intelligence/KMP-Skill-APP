import com.android.build.api.dsl.ApplicationExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.JavaVersion

/**
 * 所有 `apps/<name>` 的 Android 应用壳共用配置。
 *
 * 每个 app 的 build.gradle.kts 只留三样东西：`namespace` / `applicationId`、
 * 自己的 BuildConfig 字段、依赖。SDK 版本、签名、release 收缩、测试参数、detekt
 * 全在这里 —— 加第 10 个 app 时不会因为「忘了改 targetSdk」跟别的 app 漂移。
 *
 * 用法：
 * ```kotlin
 * plugins {
 *     id("com.android.application")
 *     id("org.jetbrains.kotlin.plugin.compose")
 *     id("androidkit.android.app")
 * }
 * android {
 *     namespace = "com.example.app"
 *     defaultConfig { applicationId = "com.example.app" }
 * }
 * ```
 *
 * 版本号：`-PVERSION_NAME=1.2.3` / `-PVERSION_CODE=12`（CI 的 release 就是这么传的）。
 * 签名：环境变量 `KEYSTORE` / `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD`；
 * 没配就保持 unsigned（本地 assembleRelease 照常能跑）。
 */

pluginManager.withPlugin("com.android.application") {
    extensions.configure<ApplicationExtension> {
        compileSdk = 37
        // AGP 9 的 minor 版本；kit 的 KMP 库要求 37.1
        compileSdkMinor = 1

        defaultConfig {
            minSdk = 26
            // Google Play 自 2026-08-31 起要求新应用/更新 targetSdk ≥ 36
            targetSdk = 36
            versionCode = (project.findProperty("VERSION_CODE") as String?)?.toInt() ?: 1
            versionName = (project.findProperty("VERSION_NAME") as String?) ?: "1.0.0"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        buildFeatures {
            compose = true
            buildConfig = true
        }

        testOptions {
            unitTests {
                isIncludeAndroidResources = true
                all { it.maxHeapSize = "4g" }
            }
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }

        // release 签名：四个 env 齐了才建 signingConfig；否则 release 保持 unsigned。
        // ⚠️ 别用 debug 密钥签 release —— 那只适合本地自测，发商店必炸。
        val keystore = project.file(System.getenv("KEYSTORE") ?: "release.keystore")
        val storePass = System.getenv("KEYSTORE_PASSWORD")
        val keyAliasEnv = System.getenv("KEY_ALIAS")
        val signingReady = keystore.exists() && storePass != null && keyAliasEnv != null

        signingConfigs {
            if (signingReady) {
                create("release") {
                    storeFile = keystore
                    storePassword = storePass
                    keyAlias = keyAliasEnv
                    keyPassword = System.getenv("KEY_PASSWORD") ?: storePass
                }
            }
        }

        val defaultProguard = getDefaultProguardFile("proguard-android-optimize.txt")
        buildTypes {
            getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                signingConfig = signingConfigs.findByName("release")
                val appRules = project.file("proguard-rules.pro")
                if (appRules.exists()) {
                    proguardFiles(defaultProguard, appRules)
                } else {
                    proguardFiles(defaultProguard)
                }
            }
        }
    }
}

// detekt 规则集在根目录 config/detekt/detekt.yml，所有模块共享
pluginManager.withPlugin("io.gitlab.arturbosch.detekt") {
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(project.rootDir.resolve("config/detekt/detekt.yml"))
        // KMP 的源码不在 src/main/java 下，detekt 默认扫不到 —— 手动列出来
        source.setFrom(project.fileTree("src") { include("**/kotlin/**/*.kt") })
    }
}
