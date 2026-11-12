pluginManagement {
    repositories {
        // 和主构建一致的镜像顺序；build-logic 只在配置期拉插件，命中缓存后基本不联网
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        google()
        mavenCentral()
    }
    versionCatalogs {
        // 和主构建共用一份版本目录：约定插件引用的 AGP / detekt 永远跟主构建同版本
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":conventions")
