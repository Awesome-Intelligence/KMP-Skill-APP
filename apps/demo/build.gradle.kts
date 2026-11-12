// Android 壳工程：只管 manifest / 主题 / 启动 Activity，代码全在 :kit。
// SDK 版本 / 签名 / release 收缩 / detekt 由 build-logic 的 androidkit.android.app 统一管。
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    // skill 工具的入参用 @Serializable 数据类声明
    id("org.jetbrains.kotlin.plugin.serialization")
    id("io.gitlab.arturbosch.detekt")
    id("androidkit.android.app")
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

// 版本更新检查（kit 的 data/update）：
//   -PupdateManifestUrl=… 或 UPDATE_MANIFEST_URL → 读静态 JSON 清单（可控制强制更新）
//   -PupdateGithubRepo=owner/repo 或 UPDATE_GITHUB_REPO → 直接读 GitHub Releases
// 清单 URL 优先；默认走本仓库的 GitHub Releases（零托管）。两者都配空 = 不检查更新。
val updateManifestUrl = providers.gradleProperty("updateManifestUrl").orNull
    ?: providers.environmentVariable("UPDATE_MANIFEST_URL").orNull
    ?: ""
val updateGithubRepo = providers.gradleProperty("updateGithubRepo").orNull
    ?: providers.environmentVariable("UPDATE_GITHUB_REPO").orNull
    ?: "Awesome-Intelligence/KMP-Skill-APP"

fun String.asBuildConfigString(): String = replace("\\", "\\\\").replace("\"", "\\\"")

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

android {
    namespace = "com.androidkit.app"

    defaultConfig {
        applicationId = "com.androidkit.app"

        // agent 的连接配置（值来自 gradle 属性 / 环境变量，见文件头）
        buildConfigField("String", "ANTHROPIC_API_KEY", "\"$anthropicApiKey\"")
        buildConfigField("String", "ANTHROPIC_BASE_URL", "\"$anthropicBaseUrl\"")
        buildConfigField("String", "ANTHROPIC_MODEL", "\"$anthropicModel\"")
        // 版本更新检查的配置（见文件头）
        buildConfigField("String", "UPDATE_MANIFEST_URL", "\"${updateManifestUrl.asBuildConfigString()}\"")
        buildConfigField("String", "UPDATE_GITHUB_REPO", "\"${updateGithubRepo.asBuildConfigString()}\"")

        vectorDrawables {
            useSupportLibrary = true
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
    // 检查更新要一个 Ktor 引擎（kit 只给 HttpClient 类型，引擎由 app 挂）
    implementation(libs.ktor.client.okhttp)
    // 设置文件落在 app 内部存储（kit 的 SettingsStore 用 okio 写 JSON）
    implementation(libs.okio)

    // 单元测试：shortcuts.xml 和 AppShortcut 枚举的漂移检查（见 shortcuts/AppShortcutSyncTest.kt）
    testImplementation(libs.junit)
}
