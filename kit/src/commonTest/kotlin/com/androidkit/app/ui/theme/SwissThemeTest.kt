package com.androidkit.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwissThemeTest {

    @Test
    fun Swiss预设_黑白画布直角且标签色板已填充() {
        val light = swissThemeTokens(isDark = false)
        val dark = swissThemeTokens(isDark = true)

        assertEquals(Color(0xFF000000), light.colorScheme.primary)
        assertEquals(Color(0xFFFFFFFF), light.colorScheme.background)
        // 深色是自建反转：黑画布 + 白字
        assertEquals(Color(0xFF000000), dark.colorScheme.background)
        assertEquals(Color(0xFFFFFFFF), dark.colorScheme.onBackground)
        assertEquals(ThemeStyle.Swiss, light.themeStyle)
        assertEquals(CornerStyle.Square, light.cornerStyle)
        // 瑞士风格零圆角
        assertEquals(RoundedCornerShape(0.dp), light.shapes.small)
        assertEquals(RoundedCornerShape(0.dp), light.shapes.large)
        // 排版只用 400/700 两档字重
        assertEquals(SwissTypography.titleMedium.fontWeight, light.typography.titleMedium.fontWeight)
        assertEquals(TagColorNames.toSet(), light.tagColors.keys)
        assertEquals(TagColorNames.toSet(), dark.tagColors.keys)
    }

    @Test
    fun Swiss预设_关键文字对比度过AA() {
        // 正文 / 次级文字 / 强调红 / 链接蓝 对白底
        assertTrue(contrast(Swiss.onSurfaceLight, Swiss.backgroundLight) >= 4.5)
        assertTrue(contrast(Swiss.onSurfaceVariantLight, Swiss.backgroundLight) >= 4.5)
        assertTrue(contrast(Swiss.redInk, Swiss.backgroundLight) >= 4.5)
        assertTrue(contrast(Swiss.secondaryLight, Swiss.backgroundLight) >= 4.5)
        // 深色是自建反转，同样要过 AA
        assertTrue(contrast(Swiss.onSurfaceDark, Swiss.backgroundDark) >= 4.5)
        assertTrue(contrast(Swiss.onSurfaceVariantDark, Swiss.backgroundDark) >= 4.5)
        assertTrue(contrast(Swiss.redOnDark, Swiss.backgroundDark) >= 4.5)
    }

    private fun contrast(a: Color, b: Color): Double {
        val l1 = luminance(a)
        val l2 = luminance(b)
        return (max(l1, l2) + 0.05) / (min(l1, l2) + 0.05)
    }

    private fun luminance(color: Color): Double {
        fun channel(c: Float): Double {
            val v = c.toDouble()
            return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(color.red) +
            0.7152 * channel(color.green) +
            0.0722 * channel(color.blue)
    }
}
