package com.androidkit.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

// Light Color Scheme - M3 Tonal Palette based (default to Mono)
// surfaceTint 用 outline（中性）而非 primary：Mono 的 primary 是近黑，全强度 tint 会把
// 抬升表面染黑；单色主题的 M3 正解就是拿中性色当 tint，层次由 surfaceContainer* 色阶承担。
// ponytail: 想要真正的品牌色 tint 就得先给 primary 一个真实色相，那是换主题不是修 bug。
private val LightColorScheme = lightColorScheme(
    primary = Mono.primaryLight,
    onPrimary = Mono.onPrimaryLight,
    primaryContainer = Mono.primaryContainerLight,
    onPrimaryContainer = Mono.onPrimaryContainerLight,
    secondary = Mono.secondaryLight,
    onSecondary = Mono.onSecondaryLight,
    secondaryContainer = Mono.secondaryContainerLight,
    onSecondaryContainer = Mono.onSecondaryContainerLight,
    tertiary = Mono.tertiaryLight,
    onTertiary = Mono.onTertiaryLight,
    tertiaryContainer = Mono.tertiaryContainerLight,
    onTertiaryContainer = Mono.onTertiaryContainerLight,
    error = Mono.errorLight,
    onError = Mono.onErrorLight,
    errorContainer = Mono.errorContainerLight,
    onErrorContainer = Mono.onErrorContainerLight,
    background = Mono.backgroundLight,
    onBackground = Mono.onBackgroundLight,
    surface = Mono.surfaceLight,
    onSurface = Mono.onSurfaceLight,
    surfaceVariant = Mono.surfaceVariantLight,
    onSurfaceVariant = Mono.onSurfaceVariantLight,
    outline = Mono.outlineLight,
    outlineVariant = Mono.outlineVariantLight,
    scrim = Mono.scrimLight,
    inverseSurface = Mono.inverseSurfaceLight,
    inverseOnSurface = Mono.inverseOnSurfaceLight,
    inversePrimary = Mono.inversePrimaryLight,
    surfaceTint = Mono.outlineLight,
    surfaceDim = Mono.surfaceDimLight,
    surfaceBright = Mono.surfaceBrightLight,
    surfaceContainerLowest = Mono.surfaceContainerLowestLight,
    surfaceContainerLow = Mono.surfaceContainerLowLight,
    surfaceContainer = Mono.surfaceContainerLight,
    surfaceContainerHigh = Mono.surfaceContainerHighLight,
    surfaceContainerHighest = Mono.surfaceContainerHighestLight
)

// Dark Color Scheme - M3 Tonal Palette based (default to Mono)
private val DarkColorScheme = darkColorScheme(
    primary = Mono.primaryDark,
    onPrimary = Mono.onPrimaryDark,
    primaryContainer = Mono.primaryContainerDark,
    onPrimaryContainer = Mono.onPrimaryContainerDark,
    secondary = Mono.secondaryDark,
    onSecondary = Mono.onSecondaryDark,
    secondaryContainer = Mono.secondaryContainerDark,
    onSecondaryContainer = Mono.onSecondaryContainerDark,
    tertiary = Mono.tertiaryDark,
    onTertiary = Mono.onTertiaryDark,
    tertiaryContainer = Mono.tertiaryContainerDark,
    onTertiaryContainer = Mono.onTertiaryContainerDark,
    error = Mono.errorDark,
    onError = Mono.onErrorDark,
    errorContainer = Mono.errorContainerDark,
    onErrorContainer = Mono.onErrorContainerDark,
    background = Mono.backgroundDark,
    onBackground = Mono.onBackgroundDark,
    surface = Mono.surfaceDark,
    onSurface = Mono.onSurfaceDark,
    surfaceVariant = Mono.surfaceVariantDark,
    onSurfaceVariant = Mono.onSurfaceVariantDark,
    outline = Mono.outlineDark,
    outlineVariant = Mono.outlineVariantDark,
    scrim = Mono.scrimDark,
    inverseSurface = Mono.inverseSurfaceDark,
    inverseOnSurface = Mono.inverseOnSurfaceDark,
    inversePrimary = Mono.inversePrimaryDark,
    surfaceTint = Mono.outlineDark,
    surfaceDim = Mono.surfaceDimDark,
    surfaceBright = Mono.surfaceBrightDark,
    surfaceContainerLowest = Mono.surfaceContainerLowestDark,
    surfaceContainerLow = Mono.surfaceContainerLowDark,
    surfaceContainer = Mono.surfaceContainerDark,
    surfaceContainerHigh = Mono.surfaceContainerHighDark,
    surfaceContainerHighest = Mono.surfaceContainerHighestDark
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /**
     * 系统是否关掉了动画（无障碍设置）。
     *
     * ponytail: 读系统开关是平台活儿（Android 读 Settings.Global，iOS 读 UIKit），
     * 公共层不碰平台 API —— 由各端启动时读出来传进来。
     */
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    // ponytail: 原本这里读 SettingsRepository.appearanceMode（跟随/浅色/深色）。
    // Kit 不带仓库依赖 —— 新项目把下面这行换成自己的设置流即可：
    //   val appearanceMode by settingsRepo.appearanceMode.collectAsState()
    val dark = darkTheme
    val colorScheme = if (dark) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalReducedMotion provides reducedMotion,
        LocalIsDarkTheme provides dark,
        LocalExtendedColors provides if (dark) DarkExtended else LightExtended,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * 当前生效的深浅色（已包含用户在设置里强制浅色/深色的选择）。
 *
 * 别再用 `isSystemInDarkTheme()`：它只读系统设置，跟 `appearanceMode` 覆盖冲突 ——
 * 用户强制深色时系统是浅色，读到的还是 false，透明度/色阶就全反了。
 */
val LocalIsDarkTheme = staticCompositionLocalOf { false }
