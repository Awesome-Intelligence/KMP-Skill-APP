// Top-level build file where you can add configuration options common to all sub-projects/modules.
// 版本号全在 gradle/libs.versions.toml 里改，这里只声明"用哪个插件"。
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.plugin.compose) apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false
    alias(libs.plugins.compose) apply false
}
