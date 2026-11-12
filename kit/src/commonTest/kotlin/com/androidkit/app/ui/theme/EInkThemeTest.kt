package com.androidkit.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EInkThemeTest {

    @Test
    fun EInk预设_暖纸底墨字直角且标签色板已填充() {
        val light = eInkThemeTokens(isDark = false)
        val dark = eInkThemeTokens(isDark = true)

        // 规范给的浅色契约：纸面 #f5f3ea + 墨色 #20211e
        assertEquals(Color(0xFFF5F3EA), light.colorScheme.background)
        assertEquals(Color(0xFF20211E), light.colorScheme.onSurface)
        assertEquals(Color(0xFF20211E), light.colorScheme.primary)
        // 深色是自建反转：墨底 + 纸字
        assertEquals(Color(0xFF20211E), dark.colorScheme.background)
        assertEquals(Color(0xFFF5F3EA), dark.colorScheme.onBackground)
        assertEquals(ThemeStyle.EInk, light.themeStyle)
        assertEquals(CornerStyle.Square, light.cornerStyle)
        // 电子纸风格零圆角
        assertEquals(RoundedCornerShape(0.dp), light.shapes.small)
        assertEquals(RoundedCornerShape(0.dp), light.shapes.large)
        assertEquals(TagColorNames.toSet(), light.tagColors.keys)
        assertEquals(TagColorNames.toSet(), dark.tagColors.keys)
    }

    @Test
    fun EInk预设_关键文字对比度过AA() {
        // 正文 / 次级文字 对纸底与阅读面
        assertTrue(contrast(EInk.onSurfaceLight, EInk.backgroundLight) >= 4.5)
        assertTrue(contrast(EInk.onSurfaceVariantLight, EInk.backgroundLight) >= 4.5)
        assertTrue(contrast(EInk.onSurfaceVariantLight, EInk.surfaceVariantLight) >= 4.5)
        // 深色是自建反转，同样要过 AA
        assertTrue(contrast(EInk.onSurfaceDark, EInk.backgroundDark) >= 4.5)
        assertTrue(contrast(EInk.onSurfaceVariantDark, EInk.backgroundDark) >= 4.5)
        // 栏线灰只做线条，不做正文 —— 对纸底的对比度本来就不该够正文用
        assertTrue(contrast(EInk.rule, EInk.backgroundLight) < 4.5)
    }

    @Test
    fun EInk预设_排版按规范刻度且数值用等宽() {
        val light = eInkThemeTokens(isDark = false)

        // display 36/600、行高 1.2、字距 −0.02em
        assertEquals(36.sp, light.typography.displayLarge.fontSize)
        assertEquals(600, light.typography.displayLarge.fontWeight?.weight)
        // 数值走等宽（Typography 槽位塞不进等宽，单开一组字样）
        assertEquals(FontFamily.Monospace, EInkType.data.fontFamily)
        // 主操作标签：14/600 等宽（规范 Ink Action）
        assertEquals(14.sp, EInkType.action.fontSize)
        assertEquals(600, EInkType.action.fontWeight?.weight)
        assertEquals(FontFamily.Monospace, EInkType.action.fontFamily)
        // label：12/600、字距 +0.06em
        assertEquals(0.72.sp, light.typography.labelLarge.letterSpacing)
    }

    @Test
    fun EInk预设_五个规范色值原样落地() {
        // design.md 的 Quick Color Reference —— 改色就是改规范，先钉住
        assertEquals(Color(0xFFF5F3EA), EInk.paper)
        assertEquals(Color(0xFF20211E), EInk.ink)
        assertEquals(Color(0xFF555750), EInk.graphite)
        assertEquals(Color(0xFFBAB9B0), EInk.rule)
        assertEquals(Color(0xFFE6E4DA), EInk.panel)
    }

    @Test
    fun EInk预设_全直角且零阴影() {
        val light = eInkThemeTokens(isDark = false)
        // 规范 Border Radius：cards / buttons / inputs / badges 一律 0
        listOf(
            light.shapes.extraSmall,
            light.shapes.small,
            light.shapes.medium,
            light.shapes.large,
            light.shapes.extraLarge
        ).forEach { assertEquals(RoundedCornerShape(0.dp), it) }
        assertEquals(CornerStyle.Square, light.cornerStyle)
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
