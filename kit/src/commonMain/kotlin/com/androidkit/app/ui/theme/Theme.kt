package com.androidkit.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

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

// Bitmap Color Schemes — 冷灰白 + 橙强调（位图主题），槽位结构与 Mono 一致
private val BitmapLightColorScheme = lightColorScheme(
    primary = Bitmap.primaryLight,
    onPrimary = Bitmap.onPrimaryLight,
    primaryContainer = Bitmap.primaryContainerLight,
    onPrimaryContainer = Bitmap.onPrimaryContainerLight,
    secondary = Bitmap.secondaryLight,
    onSecondary = Bitmap.onSecondaryLight,
    secondaryContainer = Bitmap.secondaryContainerLight,
    onSecondaryContainer = Bitmap.onSecondaryContainerLight,
    tertiary = Bitmap.tertiaryLight,
    onTertiary = Bitmap.onTertiaryLight,
    tertiaryContainer = Bitmap.tertiaryContainerLight,
    onTertiaryContainer = Bitmap.onTertiaryContainerLight,
    error = Bitmap.errorLight,
    onError = Bitmap.onErrorLight,
    errorContainer = Bitmap.errorContainerLight,
    onErrorContainer = Bitmap.onErrorContainerLight,
    background = Bitmap.backgroundLight,
    onBackground = Bitmap.onBackgroundLight,
    surface = Bitmap.surfaceLight,
    onSurface = Bitmap.onSurfaceLight,
    surfaceVariant = Bitmap.surfaceVariantLight,
    onSurfaceVariant = Bitmap.onSurfaceVariantLight,
    outline = Bitmap.outlineLight,
    outlineVariant = Bitmap.outlineVariantLight,
    scrim = Bitmap.scrimLight,
    inverseSurface = Bitmap.inverseSurfaceLight,
    inverseOnSurface = Bitmap.inverseOnSurfaceLight,
    inversePrimary = Bitmap.inversePrimaryLight,
    surfaceTint = Bitmap.outlineLight,
    surfaceDim = Bitmap.surfaceDimLight,
    surfaceBright = Bitmap.surfaceBrightLight,
    surfaceContainerLowest = Bitmap.surfaceContainerLowestLight,
    surfaceContainerLow = Bitmap.surfaceContainerLowLight,
    surfaceContainer = Bitmap.surfaceContainerLight,
    surfaceContainerHigh = Bitmap.surfaceContainerHighLight,
    surfaceContainerHighest = Bitmap.surfaceContainerHighestLight
)

private val BitmapDarkColorScheme = darkColorScheme(
    primary = Bitmap.primaryDark,
    onPrimary = Bitmap.onPrimaryDark,
    primaryContainer = Bitmap.primaryContainerDark,
    onPrimaryContainer = Bitmap.onPrimaryContainerDark,
    secondary = Bitmap.secondaryDark,
    onSecondary = Bitmap.onSecondaryDark,
    secondaryContainer = Bitmap.secondaryContainerDark,
    onSecondaryContainer = Bitmap.onSecondaryContainerDark,
    tertiary = Bitmap.tertiaryDark,
    onTertiary = Bitmap.onTertiaryDark,
    tertiaryContainer = Bitmap.tertiaryContainerDark,
    onTertiaryContainer = Bitmap.onTertiaryContainerDark,
    error = Bitmap.errorDark,
    onError = Bitmap.onErrorDark,
    errorContainer = Bitmap.errorContainerDark,
    onErrorContainer = Bitmap.onErrorContainerDark,
    background = Bitmap.backgroundDark,
    onBackground = Bitmap.onBackgroundDark,
    surface = Bitmap.surfaceDark,
    onSurface = Bitmap.onSurfaceDark,
    surfaceVariant = Bitmap.surfaceVariantDark,
    onSurfaceVariant = Bitmap.onSurfaceVariantDark,
    outline = Bitmap.outlineDark,
    outlineVariant = Bitmap.outlineVariantDark,
    scrim = Bitmap.scrimDark,
    inverseSurface = Bitmap.inverseSurfaceDark,
    inverseOnSurface = Bitmap.inverseOnSurfaceDark,
    inversePrimary = Bitmap.inversePrimaryDark,
    surfaceTint = Bitmap.outlineDark,
    surfaceDim = Bitmap.surfaceDimDark,
    surfaceBright = Bitmap.surfaceBrightDark,
    surfaceContainerLowest = Bitmap.surfaceContainerLowestDark,
    surfaceContainerLow = Bitmap.surfaceContainerLowDark,
    surfaceContainer = Bitmap.surfaceContainerDark,
    surfaceContainerHigh = Bitmap.surfaceContainerHighDark,
    surfaceContainerHighest = Bitmap.surfaceContainerHighestDark
)

/**
 * 主题风格。底座预置能力：App 层自行选择并持久化，kit 不带设置页。
 * - [ThemeStyle.Mono] — 默认，暖中性单色 + M3 动效
 * - [ThemeStyle.Bitmap] — 位图（冷灰白 + 橙强调、像素字体、直角、帧跳动效）
 */
enum class ThemeStyle { Mono, Bitmap }

/** 位图主题全直角：像素 UI 没有圆角，层次靠 1px 硬边框和色阶 */
private val BitmapShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

@Composable
fun AppTheme(
    /** 主题风格，默认单色（历史行为不变） */
    style: ThemeStyle = ThemeStyle.Mono,
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
    val bitmap = style == ThemeStyle.Bitmap
    val colorScheme = when {
        bitmap && dark -> BitmapDarkColorScheme
        bitmap -> BitmapLightColorScheme
        dark -> DarkColorScheme
        else -> LightColorScheme
    }
    val extended = when {
        bitmap && dark -> BitmapDarkExtended
        bitmap -> BitmapLightExtended
        dark -> DarkExtended
        else -> LightExtended
    }
    // 像素字体从资源加载（@Composable，可能 suspend），只能在这里取
    val typography = if (bitmap) bitmapTypography() else Typography

    CompositionLocalProvider(
        LocalReducedMotion provides reducedMotion,
        LocalIsDarkTheme provides dark,
        LocalThemeStyle provides style,
        LocalExtendedColors provides extended,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = if (bitmap) BitmapShapes else Shapes(), // Mono = M3 默认圆角 4/8/12/16/28
            content = content
        )
    }
}

/** 当前主题风格，位图组件/色板分发用；预览/测试默认 Mono */
val LocalThemeStyle = staticCompositionLocalOf { ThemeStyle.Mono }

/**
 * 当前生效的深浅色（已包含用户在设置里强制浅色/深色的选择）。
 *
 * 别再用 `isSystemInDarkTheme()`：它只读系统设置，跟 `appearanceMode` 覆盖冲突 ——
 * 用户强制深色时系统是浅色，读到的还是 false，透明度/色阶就全反了。
 */
val LocalIsDarkTheme = staticCompositionLocalOf { false }
