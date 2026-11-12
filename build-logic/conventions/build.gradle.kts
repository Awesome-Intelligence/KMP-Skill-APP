plugins {
    // Gradle 自带的 Kotlin DSL 插件：把 src/main/kotlin 下的 .gradle.kts 预编译成插件
    `kotlin-dsl`
}

dependencies {
    // 约定插件要配置 AGP / detekt 的扩展，编译期需要它们的类型。
    // 版本来自主构建的 libs.versions.toml（见 settings.gradle.kts 的 versionCatalogs）
    implementation(libs.android.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
}
