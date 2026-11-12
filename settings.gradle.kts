pluginManagement {
    repositories {
        // 国内镜像优先，拉不动再去官方源；换成别的镜像改这三行即可
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// 约定插件（androidkit.android.app / androidkit.kmp.library）。
// 独立 included build：不污染主构建的插件 classpath，也不跟着 app 的模块数膨胀。
includeBuild("build-logic")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Compose Multiplatform 的 -android 产物只有 Maven Central 有（国内镜像没同步），
        // 所以这几个组固定走官方源，别让镜像的半截 metadata 把解析带沟里。
        mavenCentral {
            content {
                includeGroupByRegex("org\\.jetbrains\\.compose(\\..*)?")
            }
        }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        google()
        maven("https://repo1.maven.org/maven2/")
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Android-Kit"
include(":kit")
include(":kit-agent-koog")
include(":kit-membership")
include(":chat-demo")

// apps/ 下每个带 build.gradle.kts 的目录自动成为模块（:apps:<目录名>）。
// 本仓库是底座：各 app fork 本仓库后在 apps/<name>/ 里开发自己的壳，
// 加新 app 只需新建目录，不用改这个文件 —— 这样 git pull 上游更新永不冲突。
file("apps").listFiles()
    ?.filter { it.isDirectory && it.resolve("build.gradle.kts").exists() }
    ?.sortedBy { it.name }
    ?.forEach { include(":apps:${it.name}") }
