package com.androidkit.app.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.androidkit.app.ui.theme.CornerStyle
import com.androidkit.app.ui.theme.ThemeTokens
import com.androidkit.app.ui.theme.themeTokens

/**
 * 示例：**App 自己的品牌主题** —— 一行都不用改 kit。
 *
 * 用法（放在各端入口）：
 *
 * ```kotlin
 * AppTheme(tokens = if (systemDark) DemoBrand.dark() else DemoBrand.light()) {
 *     App()
 * }
 * ```
 *
 * 这里只覆盖 M3 的主色 / 容器色，扩展语义色、排版、圆角沿用 [themeTokens] 的默认值。
 * 真做品牌时：
 * - 把 hex 换成设计给的色值
 * - 要自定义 success / warning / AI 强调色，传 `extendedColors = ExtendedColors(...)`
 * - 要直角就 `cornerStyle = CornerStyle.Square`，要自己的标签色板就传 `tagColors`
 *
 * demo 没在入口里接它：模板不该把某个示例品牌强加给所有 fork 出去的 app。
 * 这份文件的用途是「可编译的样板」——拷到你的 app 里改色即可。
 */
object DemoBrand {

    fun light(): ThemeTokens = themeTokens(
        colorScheme = lightColorScheme(
            primary = Color(0xFF00696D),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF9CF1F5),
            onPrimaryContainer = Color(0xFF002022),
            secondary = Color(0xFF4A6365),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFCCE8E9),
            onSecondaryContainer = Color(0xFF051F21),
            tertiary = Color(0xFF4B607C),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFD3E4FF),
            onTertiaryContainer = Color(0xFF041C35),
        ),
        isDark = false,
        cornerStyle = CornerStyle.Rounded,
    )

    fun dark(): ThemeTokens = themeTokens(
        colorScheme = darkColorScheme(
            primary = Color(0xFF80D4D8),
            onPrimary = Color(0xFF003739),
            primaryContainer = Color(0xFF004F52),
            onPrimaryContainer = Color(0xFF9CF1F5),
            secondary = Color(0xFFB0CCCD),
            onSecondary = Color(0xFF1B3536),
            secondaryContainer = Color(0xFF324B4D),
            onSecondaryContainer = Color(0xFFCCE8E9),
            tertiary = Color(0xFFB3C8E8),
            onTertiary = Color(0xFF1D314B),
            tertiaryContainer = Color(0xFF344863),
            onTertiaryContainer = Color(0xFFD3E4FF),
        ),
        isDark = true,
        cornerStyle = CornerStyle.Rounded,
    )
}
