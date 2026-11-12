package com.androidkit.app

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.components.AppCard
import com.androidkit.app.ui.components.ComponentGallery
import com.androidkit.app.ui.components.SecondaryButton
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.LocalIsDarkTheme
import com.androidkit.app.ui.theme.LocalReducedMotion
import com.androidkit.app.ui.theme.LocalThemeStyle
import com.androidkit.app.ui.theme.ThemeStyle

/**
 * 三端共用的演示页。
 *
 * 装进新项目后换成自己的首页就行 —— 这里只是让 kit 自己能跑起来、能截图、能验证编译。
 *
 * 页面 = 风格切换器 + [ComponentGallery]（kit 全部组件的分类目录）。
 * 目录里的样例复用各组件文件里的 `*Preview` 函数，所以组件改了目录就跟着改。
 *
 * 顺带演示几套预置主题风格：切风格只是给 [AppTheme] 换个 [ThemeStyle] 参数，
 * 底座预置几套配色/排版/形状/动效，App 层自己决定用哪套并持久化 —— kit 不带设置页。
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Android-Kit", style = MaterialTheme.typography.headlineMedium)

            StyleSwitcher(
                style = style,
                dark = dark,
                onStyle = { style = it },
                onDark = { dark = it }
            )

            // 目录自己滚动（内部是 LazyColumn），所以外层不能再套 verticalScroll
            // weight(1f) = 占掉标题和风格切换器之外剩下的高度
            ComponentGallery(
                style = style,
                modifier = Modifier.weight(1f),
            )
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
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SecondaryButton(
                text = "Mono",
                onClick = { onStyle(ThemeStyle.Mono) },
                enabled = style != ThemeStyle.Mono
            )
            SecondaryButton(
                text = "Swiss",
                onClick = { onStyle(ThemeStyle.Swiss) },
                enabled = style != ThemeStyle.Swiss
            )
            SecondaryButton(
                text = "Notion",
                onClick = { onStyle(ThemeStyle.Notion) },
                enabled = style != ThemeStyle.Notion
            )
            SecondaryButton(
                text = "E-Ink",
                onClick = { onStyle(ThemeStyle.EInk) },
                enabled = style != ThemeStyle.EInk
            )
            SecondaryButton(
                text = "macOS",
                onClick = { onStyle(ThemeStyle.MacOS) },
                enabled = style != ThemeStyle.MacOS
            )
        }
        Spacer(Modifier.height(8.dp))
        SecondaryButton(
            text = if (dark) "深色：开" else "深色：关",
            onClick = { onDark(!dark) }
        )
    }
}
