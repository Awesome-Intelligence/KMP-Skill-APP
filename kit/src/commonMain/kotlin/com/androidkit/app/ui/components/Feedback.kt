package com.androidkit.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.M3Motion

/**
 * App Snackbar
 * 显示操作反馈（成功/失败）
 */
@Composable
fun AppSnackbar(
    message: String,
    isSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    Snackbar(
        modifier = modifier.padding(horizontal = 16.dp),
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = if (isSuccess) stringResource(Res.string.operation_success) else stringResource(Res.string.operation_failed),
                tint = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Text(message)
        }
    }
}

/**
 * Loading Indicator - 加载指示器
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    val loadingText = stringResource(Res.string.loading)
    CircularProgressIndicator(
        modifier = modifier
            .size(48.dp)
            .semantics { contentDescription = loadingText },
        strokeWidth = 4.dp,
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

/**
 * Loading Overlay - 全屏加载遮罩
 */
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        content()

        // 遮罩淡入淡出，不硬切
        AnimatedVisibility(
            visible = isLoading,
            enter = M3Motion.appear(),
            exit = M3Motion.disappear()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/**
 * Simple centered loading
 */
@Composable
fun CenteredLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator()
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun AppSnackbarPreview() {
    AppTheme {
        AppSnackbar(message = DemoData.Text.SAVED, isSuccess = true) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun LoadingIndicatorPreview() {
    AppTheme {
        LoadingIndicator() // ponytail: demo data only
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun LoadingOverlayPreview() {
    AppTheme {
        LoadingOverlay(
            isLoading = true,
            content = {
                Text(
                    text = DemoData.Text.PAGE_CONTENT,
                    modifier = Modifier.padding(32.dp)
                )
            }
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun CenteredLoadingPreview() {
    AppTheme {
        CenteredLoading() // ponytail: demo data only
    }
}
