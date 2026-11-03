// 桌面对话 demo：验证 kit + agent 的对话链路。
// 跑：gradlew :chat-demo:run   自检：gradlew :chat-demo:run --args="--check"
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.compose")
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":kit"))
    implementation(project(":kit-agent-koog"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "com.androidkit.app.chat.MainKt"
    }
}
