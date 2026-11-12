package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.LocalThemeStyle
import com.androidkit.app.ui.theme.ThemeStyle
import coil3.compose.AsyncImage

/**
 * 头像 Hero —— 人脉详情页与"我"详情页共用的头部区域：
 * 头像主色淡渐变背景 + 居中圆形头像，点击头像更换图片。
 *
 * @param imageModel Coil 可识别的图片来源（Uri / 路径 / 字符串），null 时显示姓名首字母
 * @param fallbackColor 渐变背景的主色
 *
 * ponytail: 原来这里用 androidx palette 从头像图里取主色 —— Android 专属，且要多跑一次解码。
 * 公共层改成调用方直接给色值（要取色就在 app 层做，那边知道图片来源）。
 */
@Composable
fun AvatarHero(
    name: String,
    imageModel: Any?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fallbackColor: Color = MaterialTheme.colorScheme.primary,
    avatarSize: Dp = 96.dp,
    contentDescription: String? = null
) {
    // macOS 毛玻璃 / 瑞士风格：禁渐变 —— 平涂一层底色
    val swiss = LocalThemeStyle.current == ThemeStyle.Swiss
    val macos = LocalThemeStyle.current == ThemeStyle.MacOS
    // 瑞士的栏线是 1px 黑白，不容许 1.5dp 这种半档
    val hairline = if (macos || swiss) 1.dp else 1.5.dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                when {
                    swiss -> SolidColor(fallbackColor.copy(alpha = 0.20f))
                    macos -> SolidColor(MaterialTheme.colorScheme.surfaceContainer)
                    else -> Brush.verticalGradient(
                        listOf(fallbackColor.copy(alpha = 0.20f), Color.Transparent)
                    )
                }
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(avatarSize),
                contentAlignment = Alignment.Center
            ) {
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = contentDescription ?: name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .border(hairline, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .clickable(onClick = onClick)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .border(hairline, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .background(Color.Black)
                            .clickable(onClick = onClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun AvatarHeroPreview() {
    AppTheme {
        AvatarHero(
            name = DemoData.Text.ZHANG_SAN,
            imageModel = null,
            onClick = {}
        ) // ponytail: demo data only
    }
}
