package com.androidkit.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MacOSThemeTest {

    @Test
    fun macOS预设_三档灰加系统蓝且圆角按规范分配() {
        val dark = macOSThemeTokens(isDark = true)
        val light = macOSThemeTokens(isDark = false)

        // 深色是规范原生：最深灰窗底 + 中灰内容面 + 系统蓝强调
        assertEquals(Color(0xFF1C1C1E), dark.colorScheme.background)
        assertEquals(Color(0xFF2C2C2E), dark.colorScheme.surface)
        assertEquals(Color(0xFF0A84FF), dark.colorScheme.primary)
        // 浅色是自建的 macOS 浅色系统
        assertEquals(Color(0xFFF2F2F7), light.colorScheme.background)
        assertEquals(Color(0xFFFFFFFF), light.colorScheme.surface)

        assertEquals(ThemeStyle.MacOS, dark.themeStyle)
        assertEquals(CornerStyle.Rounded, dark.cornerStyle)
        // 按钮/输入 6、卡片 10、面板 14
        assertEquals(RoundedCornerShape(6.dp), dark.shapes.small)
        assertEquals(RoundedCornerShape(10.dp), dark.shapes.medium)
        assertEquals(RoundedCornerShape(14.dp), dark.shapes.large)
        // 标题走衬线，正文走无衬线
        assertEquals(FontFamily.Serif, dark.typography.headlineLarge.fontFamily)
        assertEquals(FontFamily.SansSerif, dark.typography.bodyLarge.fontFamily)

        assertEquals(TagColorNames.toSet(), dark.tagColors.keys)
        assertEquals(TagColorNames.toSet(), light.tagColors.keys)
    }

    @Test
    fun macOS预设_关键文字对比度过AA() {
        // 深色（规范原生）：白字对三档灰
        assertTrue(contrast(MacOS.onSurfaceDark, MacOS.surfaceDark) >= 4.5)
        assertTrue(contrast(MacOS.onBackgroundDark, MacOS.backgroundDark) >= 4.5)
        assertTrue(contrast(MacOS.onSurfaceVariantDark, MacOS.surfaceDark) >= 4.5)
        // 系统蓝当文字（链接 / 强调）也得够
        assertTrue(contrast(MacOS.primaryDark, MacOS.backgroundDark) >= 4.5)
        assertTrue(contrast(MacOS.errorDark, MacOS.backgroundDark) >= 4.5)

        // 浅色（自建）：同样要过 AA
        assertTrue(contrast(MacOS.onSurfaceLight, MacOS.surfaceLight) >= 4.5)
        assertTrue(contrast(MacOS.onBackgroundLight, MacOS.backgroundLight) >= 4.5)
        assertTrue(contrast(MacOS.onSurfaceVariantLight, MacOS.surfaceLight) >= 4.5)
        assertTrue(contrast(MacOS.primaryLight, MacOS.surfaceLight) >= 4.5)
        assertTrue(contrast(MacOS.errorLight, MacOS.surfaceLight) >= 4.5)
    }

    @Test
    fun macOS预设_分隔线是半透明白而不是实色() {
        // 1px 哲学：分隔 = 白 8% / 12%，靠 alpha 合成，不写死实色
        assertTrue(MacOS.outlineDark.alpha < 0.2f)
        assertTrue(MacOS.outlineVariantDark.alpha < 0.15f)
        // 选中态 = 系统蓝 18%
        assertEquals(0x2E, MacOS.selectionDark.alpha.times(255).toInt())
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
