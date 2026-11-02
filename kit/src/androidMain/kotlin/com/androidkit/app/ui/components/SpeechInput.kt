package com.androidkit.app.ui.components

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.androidkit.app.res.*
import org.jetbrains.compose.resources.stringResource
import java.util.Locale

/**
 * 语音输入 —— 权限检查 + 识别器启动都在这儿，返回一个「点了就说话」的 lambda。
 *
 * 灵感速记和「此刻」页共用，免得两处各抄一遍权限逻辑。
 *
 * ponytail: 语音识别是 Android 平台能力（RecognizerIntent），所以这个函数只在 androidMain，
 * 公共层不给它留位置。iOS 端要语音输入就在 iosMain 里另写一份同样签名的实现。
 */
@Composable
fun rememberSpeechLauncher(
    /** 识别到的文本 */
    onText: (String) -> Unit,
    /** 出错时回调，参数是已解析好的提示文案 */
    onError: (String) -> Unit = {}
): () -> Unit {
    val context = LocalContext.current
    // 回调每次重组都可能变，用 rememberUpdatedState 保住最新的一份
    val currentOnText by rememberUpdatedState(onText)
    val currentOnError by rememberUpdatedState(onError)
    val voiceNotAvailable = stringResource(Res.string.voice_not_available)
    val voicePermissionRequired = stringResource(Res.string.voice_permission_required)

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (!spoken.isNullOrBlank()) {
            currentOnText(spoken)
        }
    }

    val launchSpeech = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            // 必须用 BCP-47（zh-CN），Locale.toString() 给的是 zh_CN，部分识别器不认
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            currentOnError(voiceNotAvailable)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchSpeech() else currentOnError(voicePermissionRequired)
    }

    return {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            launchSpeech()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
