package com.androidkit.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Monochrome theme — 低色相暖中性色阶（hue 70°，OKLCH C ≤ 0.014）。
 *
 * 不是纯灰：每个中性色都带极轻的暖偏，所有表面读起来像同一块被照亮的材料，
 * 而不是一层层叠上去的灰盒子。亮度与原纯灰版逐项对齐，所以对比度不变
 * （实测漂移 < 0.05），配色替换不需要动任何组件。
 *
 * 改色相：下面的 hex 是 OKLCH 里锁定 L、只加 C 生成的。若要换偏冷改 HUE=250，
 * 重新生成整表，别手改单个值 —— 色阶的一致性比某一项好不好看更重要。
 *
 * ponytail: 亮度锁死是为了零对比度回归；若将来要整体调明暗，那是改 CMAX/色阶
 * 而不是改单个 hex，届时需重跑对比度校验。
 */
object Mono {
    // ── Light ────────────────────────────────────────────────────────
    val primaryLight = Color(0xFF1D1915)
    val onPrimaryLight = Color(0xFFFFFFFF)
    val primaryContainerLight = Color(0xFFF0ECE9)
    val onPrimaryContainerLight = Color(0xFF14100C)
    val secondaryLight = Color(0xFF49433D)
    val onSecondaryLight = Color(0xFFFFFFFF)
    val secondaryContainerLight = Color(0xFFE0DBD7)
    val onSecondaryContainerLight = Color(0xFF25201B)
    val tertiaryLight = Color(0xFF635D56)
    val onTertiaryLight = Color(0xFFFFFFFF)
    val tertiaryContainerLight = Color(0xFFDAD5D0)
    val onTertiaryContainerLight = Color(0xFF2A2520)
    val errorLight = Color(0xFFBA1A1A)
    val onErrorLight = Color(0xFFFFFFFF)
    val errorContainerLight = Color(0xFFFFDAD6)
    val onErrorContainerLight = Color(0xFF93000A)
    val backgroundLight = Color(0xFFFDFAF6)
    val onBackgroundLight = Color(0xFF1D1915)
    val surfaceLight = Color(0xFFFDFAF6)
    val onSurfaceLight = Color(0xFF1D1915)
    val surfaceVariantLight = Color(0xFFE9E5E1)
    val onSurfaceVariantLight = Color(0xFF4C463F)
    val outlineLight = Color(0xFF7C756E)
    val outlineVariantLight = Color(0xFFDED9D5)
    val scrimLight = Color(0xFF000000)
    val inverseSurfaceLight = Color(0xFF322D28)
    val inverseOnSurfaceLight = Color(0xFFF5F1EE)
    val inversePrimaryLight = Color(0xFFE3DFDB)
    val surfaceDimLight = Color(0xFFDFDAD6)
    val surfaceBrightLight = Color(0xFFFDFAF6)
    val surfaceContainerLowestLight = Color(0xFFFFFFFF)
    val surfaceContainerLowLight = Color(0xFFF7F3F0)
    val surfaceContainerLight = Color(0xFFF1EDEA)
    val surfaceContainerHighLight = Color(0xFFEBE7E3)
    val surfaceContainerHighestLight = Color(0xFFE5E1DD)

    // ── Dark ─────────────────────────────────────────────────────────
    val primaryDark = Color(0xFFE6E2DE)
    val onPrimaryDark = Color(0xFF1D1915)
    val primaryContainerDark = Color(0xFF37322C)
    val onPrimaryContainerDark = Color(0xFFE9E5E1)
    val secondaryDark = Color(0xFFC2BCB6)
    val onSecondaryDark = Color(0xFF26211C)
    val secondaryContainerDark = Color(0xFF37322C)
    val onSecondaryContainerDark = Color(0xFFE2DDD9)
    val tertiaryDark = Color(0xFFA59F98)
    val onTertiaryDark = Color(0xFF201C18)
    val tertiaryContainerDark = Color(0xFF433D37)
    val onTertiaryContainerDark = Color(0xFFD8D3CE)
    val errorDark = Color(0xFFFFB4AB)
    val onErrorDark = Color(0xFF690005)
    val errorContainerDark = Color(0xFF93000A)
    val onErrorContainerDark = Color(0xFFFFDAD6)
    val backgroundDark = Color(0xFF15110D)
    val onBackgroundDark = Color(0xFFE7E3DF)
    val surfaceDark = Color(0xFF15110D)
    val onSurfaceDark = Color(0xFFE7E3DF)
    val surfaceVariantDark = Color(0xFF2F2A25)
    val onSurfaceVariantDark = Color(0xFFC8C3BE)
    val outlineDark = Color(0xFF918B84)
    val outlineVariantDark = Color(0xFF322D28)
    val scrimDark = Color(0xFF000000)
    val inverseSurfaceDark = Color(0xFFE7E3DF)
    val inverseOnSurfaceDark = Color(0xFF322D28)
    val inversePrimaryDark = Color(0xFF1D1915)
    val surfaceDimDark = Color(0xFF15110D)
    val surfaceBrightDark = Color(0xFF3D3731)
    val surfaceContainerLowestDark = Color(0xFF100C09)
    val surfaceContainerLowDark = Color(0xFF1D1915)
    val surfaceContainerDark = Color(0xFF221D19)
    val surfaceContainerHighDark = Color(0xFF2D2823)
    val surfaceContainerHighestDark = Color(0xFF38332D)
}