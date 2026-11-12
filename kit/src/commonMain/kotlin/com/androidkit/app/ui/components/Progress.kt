package com.androidkit.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import com.androidkit.app.ui.theme.themedCorner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.M3Motion

/**
 * Linear Progress Bar - M3 线性进度条
 * 高度 8dp，圆角 4dp；进度变化走 spatial 动画，减动效时直接跳变
 */
@Composable
fun LinearProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = M3Motion.defaultSpatial(),
        label = "linear_progress"
    )
    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(themedCorner(4.dp)),
        color = color,
        trackColor = trackColor,
    )
}

/**
 * Circular Progress Indicator with percentage text
 * 尺寸 60dp，进度环宽度 6dp，圆角端点；进度与百分比文字都随动画滚动
 */
@Composable
fun CircularProgressIndicatorWithText(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    size: Int = 60,
    strokeWidth: Int = 6,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = M3Motion.defaultSpatial(),
        label = "circular_progress"
    )
    androidx.compose.foundation.layout.Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = trackColor,
            strokeWidth = strokeWidth.dp,
            strokeCap = StrokeCap.Round
        )
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = progressColor,
            strokeWidth = strokeWidth.dp,
            strokeCap = StrokeCap.Round
        )
        Text(
            text = stringResource(Res.string.progress_format, (animatedProgress * 100).toInt()),
            style = if (size >= 60) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LinearProgressBarPreview() {
    AppTheme {
        LinearProgressBar(progress = DemoData.Home.favoriteQuarterly.progress) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun CircularProgressIndicatorWithTextPreview() {
    AppTheme {
        CircularProgressIndicatorWithText(progress = DemoData.Value.PROGRESS_075)
    }
}
