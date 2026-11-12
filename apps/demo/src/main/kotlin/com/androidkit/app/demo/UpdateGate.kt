package com.androidkit.app.demo

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.androidkit.app.BuildConfig
import com.androidkit.app.data.update.FileUpdateStateStore
import com.androidkit.app.data.update.GitHubReleaseSource
import com.androidkit.app.data.update.HttpUpdateSource
import com.androidkit.app.data.update.UpdateChecker
import com.androidkit.app.data.update.UpdateResult
import com.androidkit.app.data.update.UpdateSource
import com.androidkit.app.ui.components.UpdateDialog
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath

/**
 * 「启动时自动发现新版本」的接线示例（app 层，不是 kit 的一部分）。
 *
 * 做的事只有三件：
 * 1. 冷启动时调一次 `UpdateChecker.check(BuildConfig.VERSION_NAME)` —— 节流在 checker 里，
 *    不会每次启动都联网
 * 2. 有新版本就弹 kit 的 [UpdateDialog]
 * 3. 用户选择“跳过此版本”时落盘（下次出现更新的版本会重新提示）
 *
 * “立即更新”在这里是打开浏览器/应用商店 —— 各端 App 自己换实现
 * （Android 也可以接 Play In-App Updates 或 PackageInstaller）。
 *
 * 配置见 `apps/demo/build.gradle.kts` 文件头的 `updateManifestUrl` / `updateGithubRepo`。
 * 清单 URL 优先；没配就用 GitHub Releases。
 */
@Composable
fun UpdateGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val checker = remember { createUpdateChecker(context.applicationContext) }
    var available by remember { mutableStateOf<UpdateResult.Available?>(null) }

    LaunchedEffect(checker) {
        // 失败/节流/已最新都静默：更新检查不该打扰正在用 app 的人
        available = checker?.check(BuildConfig.VERSION_NAME) as? UpdateResult.Available
    }

    content()

    available?.let { update ->
        val canUpdate = !update.manifest.downloadUrl.isNullOrBlank()
        // 清单要求强制更新、却没给下载地址：别把用户卡死在死胡同里，降级成可关闭
        val mandatory = update.mandatory && canUpdate
        UpdateDialog(
            latestVersion = update.manifest.latestVersion,
            currentVersion = BuildConfig.VERSION_NAME,
            releaseNotes = update.manifest.releaseNotes,
            mandatory = mandatory,
            updateEnabled = canUpdate,
            onUpdate = {
                openDownload(context, update.manifest.downloadUrl)
                // 强制更新不关弹窗：用户从浏览器回来还在这个版本，得继续挡着
                if (!mandatory) available = null
            },
            onLater = { available = null },
            onSkipVersion = {
                available = null
                scope.launch { checker?.skip(update.manifest.latestVersion) }
            },
        )
    }
}

/** 按构建配置选版本源；两个都没配就返回 null（内测包常见：完全不检查）。 */
private fun createUpdateChecker(context: Context): UpdateChecker? {
    if (BuildConfig.UPDATE_MANIFEST_URL.isBlank() && BuildConfig.UPDATE_GITHUB_REPO.isBlank()) return null

    // 引擎归 app 挂（kit 不写 expect/actual）：这里用 Android/Desktop 都有的 OkHttp
    val client = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
            connectTimeoutMillis = 5_000
        }
    }
    val source: UpdateSource = when {
        BuildConfig.UPDATE_MANIFEST_URL.isNotBlank() ->
            HttpUpdateSource(client, BuildConfig.UPDATE_MANIFEST_URL)

        else -> GitHubReleaseSource(client, BuildConfig.UPDATE_GITHUB_REPO)
    }
    return UpdateChecker(
        source = source,
        stateStore = FileUpdateStateStore(context.filesDir.absolutePath.toPath()),
    )
}

/**
 * 打开下载页 / 商店。没有能处理的应用就静默算了 —— 更新提示不该把 app 弄崩。
 */
private fun openDownload(context: Context, url: String?) {
    if (url.isNullOrBlank()) return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
