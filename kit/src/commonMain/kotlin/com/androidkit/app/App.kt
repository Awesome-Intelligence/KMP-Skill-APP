package com.androidkit.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.components.AppCard
import com.androidkit.app.ui.components.DotMatrixBarChart
import com.androidkit.app.ui.components.DotMatrixNumber
import com.androidkit.app.ui.components.PixelDissolve
import com.androidkit.app.ui.components.PixelProgressBar
import com.androidkit.app.ui.components.PrimaryButton
import com.androidkit.app.ui.components.SecondaryButton
import com.androidkit.app.ui.components.TypewriterText
import com.androidkit.app.ui.components.pixelDitherHover
import com.androidkit.app.ui.components.pixelDotGrid
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.LocalIsDarkTheme
import com.androidkit.app.ui.theme.LocalReducedMotion
import com.androidkit.app.ui.theme.LocalThemeStyle
import com.androidkit.app.ui.theme.ThemeStyle
import kotlinx.coroutines.delay

/**
 * 三端共用的演示页。
 *
 * 装进新项目后换成自己的首页就行 —— 这里只是让 kit 自己能跑起来、能截图、能验证编译。
 *
 * 顺带演示位图（Bitmap）主题：切风格只是给 [AppTheme] 换个 [ThemeStyle] 参数，
 * 底座预置两套配色/排版/形状/动效，App 层自己决定用哪套并持久化 —— kit 不带设置页。
 */
@Composable
fun App() {
    // 初值继承外层 AppTheme（各端入口定的默认风格），下面的开关改的是本页自己的
    val outerStyle = LocalThemeStyle.current
    val outerDark = LocalIsDarkTheme.current
    var style by remember { mutableStateOf(outerStyle) }
    var dark by remember { mutableStateOf(outerDark) }
    // 外层 AppTheme 已经定过减动效，内层别把它重置回 false
    val reducedMotion = LocalReducedMotion.current

    // 内层 AppTheme 覆盖外层的（调用方已经包了一层 AppTheme），
    // 这样本页能演示风格切换而不必改各端入口。
    AppTheme(style = style, darkTheme = dark, reducedMotion = reducedMotion) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                // 位图主题的冷灰白底 + 点阵网格纹理
                .then(if (style == ThemeStyle.Bitmap) Modifier.pixelDotGrid() else Modifier)
                .then(if (style == ThemeStyle.Bitmap) Modifier.padding(16.dp) else Modifier),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Android-Kit", style = MaterialTheme.typography.headlineMedium)

            StyleSwitcher(
                style = style,
                dark = dark,
                onStyle = { style = it },
                onDark = { dark = it }
            )

            AppCard {
                Text("Compose Multiplatform 演示", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "同一套组件：Android / iOS / Desktop",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            PixelShowcase()

            PrimaryButton(text = "点一下", onClick = {})
        }
    }
}

/** 风格切换 —— 演示用，不是设置页（App 层自己存选择） */
@Composable
private fun StyleSwitcher(
    style: ThemeStyle,
    dark: Boolean,
    onStyle: (ThemeStyle) -> Unit,
    onDark: (Boolean) -> Unit,
) {
    AppCard {
        Text("主题风格", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(
                text = "Mono",
                onClick = { onStyle(ThemeStyle.Mono) },
                enabled = style != ThemeStyle.Mono
            )
            SecondaryButton(
                text = "Bitmap",
                onClick = { onStyle(ThemeStyle.Bitmap) },
                enabled = style != ThemeStyle.Bitmap
            )
        }
        Spacer(Modifier.height(8.dp))
        SecondaryButton(
            text = if (dark) "深色：开" else "深色：关",
            onClick = { onDark(!dark) }
        )
    }
}

/** 位图组件样例 —— 六个动效规格各来一个 */
@Composable
private fun PixelShowcase() {
    var number by remember { mutableIntStateOf(128) }
    var chartSeed by remember { mutableIntStateOf(0) }
    var page by remember { mutableIntStateOf(0) }

    // 进度条自动跑一段，看按帧点亮
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            progress = 0f
            delay(300)
            progress = 1f
            delay(2500)
        }
    }

    AppCard {
        Text("位图组件", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(12.dp))
        Text("1. 点阵数字滚动", style = MaterialTheme.typography.labelSmall)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            DotMatrixNumber(value = number.toString())
            Spacer(Modifier.width(12.dp))
            SecondaryButton(text = "换个数", onClick = { number += 7 })
        }

        Spacer(Modifier.height(12.dp))
        Text("2. 点阵柱状图", style = MaterialTheme.typography.labelSmall)
        DotMatrixBarChart(values = chartValues(chartSeed))
        SecondaryButton(text = "重放", onClick = { chartSeed++ })

        Spacer(Modifier.height(12.dp))
        Text("3. 像素溶解转场", style = MaterialTheme.typography.labelSmall)
        PixelDissolve(target = page) {
            Text(
                text = if (page % 2 == 0) "第一页" else "第二页",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        SecondaryButton(text = "翻页", onClick = { page++ })

        Spacer(Modifier.height(12.dp))
        Text("4. 抖动悬停", style = MaterialTheme.typography.labelSmall)
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .pixelDitherHover()
        ) {
            Text("鼠标移上来（四档网点）", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(12.dp))
        Text("5. 逐字光标", style = MaterialTheme.typography.labelSmall)
        TypewriterText(text = "待办已添加 3 项，共 128 条", style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(12.dp))
        Text("6. 按帧加载进度条", style = MaterialTheme.typography.labelSmall)
        PixelProgressBar(progress = progress, modifier = Modifier.fillMaxWidth())
    }
}

private fun chartValues(seed: Int): List<Int> =
    listOf(3, 7, 4, 9, 5, 8, 2, 6).map { (it + seed) % 10 }
