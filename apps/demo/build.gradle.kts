// Android 壳工程：只管 manifest / 主题 / 启动 Activity，代码全在 :kit
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    // skill 工具的入参用 @Serializable 数据类声明
    id("org.jetbrains.kotlin.plugin.serialization")
    id("io.gitlab.arturbosch.detekt")
}

// API key / 模型：命令行 -PanthropicApiKey=xxx，或环境变量 ANTHROPIC_API_KEY。
// 别把 key 写进仓库 —— key 会被打进 APK，提取出来就能用。
val anthropicApiKey = (providers.gradleProperty("anthropicApiKey").orNull
    ?: providers.environmentVariable("ANTHROPIC_API_KEY").orNull
    ?: "").replace("\\", "\\\\").replace("\"", "\\\"")
val anthropicBaseUrl = providers.gradleProperty("anthropicBaseUrl").orNull
    ?: providers.environmentVariable("ANTHROPIC_BASE_URL").orNull
    ?: "https://api.anthropic.com"
val anthropicModel = providers.gradleProperty("anthropicModel").orNull
    ?: providers.environmentVariable("ANTHROPIC_MODEL").orNull
    ?: "claude-opus-5-5"

// release 包里带 key = 把 key 公开了（谁都能从 APK 里提取出来）。
// 只警告，不拦构建 —— 本地自测打 release 完全没问题；
// 真要上架，把 key 挪到服务端代理，APK 里只留 baseUrl。
val isReleaseBuild = gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }
if (isReleaseBuild && anthropicApiKey.isNotBlank()) {
    logger.warn(
        "\n" +
            "  ⚠️  release 包里带着 ANTHROPIC_API_KEY —— 它能被从 APK 里提取出来，等于公开了。\n" +
            "      本地自测无所谓；上架前把 key 挪到服务端代理（APK 里只留 baseUrl）。\n"
    )
}

// release 签名：CI 里 secret KEYSTORE_BASE64 解码成 release.keystore，三个密码走 secret
// （KEYSTORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD，见 .github/workflows/release.yml）。
// 本地没配这些环境变量时整个跳过 —— assembleRelease 照样能跑，产物是 unsigned。
// ⚠️ 别用 debug 密钥签 release（signingConfigs.getByName("debug")），那是测试用的假设置。
val releaseKeystore = file(System.getenv("KEYSTORE") ?: "release.keystore")
val releaseStorePass = System.getenv("KEYSTORE_PASSWORD")
val releaseKeyAlias = System.getenv("KEY_ALIAS")
val releaseKeyPass = System.getenv("KEY_PASSWORD")
val signingReady = releaseKeystore.exists() && releaseStorePass != null && releaseKeyAlias != null

android {
    namespace = "com.androidkit.app"
    compileSdk = 37
    compileSdkMinor = 1

    defaultConfig {
        applicationId = "com.androidkit.app"
        minSdk = 26
        // Google Play 自 2026-08-31 起要求新应用/更新 targetSdk ≥ 36
        targetSdk = 36
        versionCode = 1
        // CI 用 -PVERSION_NAME=1.2.3 覆盖（release.yml 就是这么传的）
        versionName = (project.findProperty("VERSION_NAME") as String?) ?: "1.0.0"

        // agent 的连接配置（值来自 gradle 属性 / 环境变量，见文件头）
        buildConfigField("String", "ANTHROPIC_API_KEY", "\"$anthropicApiKey\"")
        buildConfigField("String", "ANTHROPIC_BASE_URL", "\"$anthropicBaseUrl\"")
        buildConfigField("String", "ANTHROPIC_MODEL", "\"$anthropicModel\"")

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (signingReady) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = releaseStorePass
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPass ?: releaseStorePass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // keystore + 密码没配就留在 unsigned —— 发布走 CI，那边四个 secret 必须齐
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { it.maxHeapSize = "4g" }
        }
    }
}

dependencies {
    implementation(project(":kit"))
    // app 的 agent 运行时：Koog（模型走云端 API，loop 和工具都跑在手机上）
    implementation(project(":kit-agent-koog"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    // 业务 skill 的工具入参是 @Serializable 数据类
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
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
