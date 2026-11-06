// Android 壳工程：只管 manifest / 主题 / 启动 Activity，代码全在 :kit
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    // skill 工具的入参用 @Serializable 数据类声明
    id("org.jetbrains.kotlin.plugin.serialization")
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

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // ⚠️ 别用 debug 密钥签 release（signingConfigs.getByName("debug")）。
            //    那是**测试用的假设置**，发到商店会炸 —— 正式发布必须换成真 keystore：
            //
            //    signingConfigs {
            //        create("release") {
            //            storeFile = file(System.getenv("KEYSTORE") ?: "keystore.jks")
            //            storePassword = System.getenv("KEYSTORE_PASS")
            //            keyAlias = System.getenv("KEY_ALIAS")
            //            keyPassword = System.getenv("KEY_PASS")
            //        }
            //    }
            //    signingConfig = signingConfigs.getByName("release")
            //
            // 不想上架就整段留着 debug 签名，但别忘了 R8 规则要自己维护。
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
