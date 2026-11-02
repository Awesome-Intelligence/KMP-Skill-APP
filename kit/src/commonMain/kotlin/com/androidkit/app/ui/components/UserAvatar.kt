package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import coil3.compose.AsyncImage

/**
 * 用户头像圆 —— 有头像图显示图，没有就退回首字母。
 * 「我」相关页面（我的资料 / 我的 / 抽屉头部）共用同一套渲染。
 */
@Composable
fun UserAvatar(
    avatar: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    initialStyle: TextStyle = MaterialTheme.typography.titleMedium,
    contentDescription: String = name
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        if (!avatar.isNullOrBlank()) {
            AsyncImage(
                model = avatar,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = name.firstOrNull()?.toString() ?: "?",
                style = initialStyle,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserAvatarPreview() {
    AppTheme {
        UserAvatar(
            avatar = null,
            name = DemoData.Text.ZHANG_SAN
        ) // ponytail: demo data only
    }
}
