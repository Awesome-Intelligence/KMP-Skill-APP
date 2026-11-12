package com.androidkit.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ThemeTokensTest {

    @Test
    fun 自定义主题_只传颜色其余走默认() {
        val scheme = lightColorScheme(primary = Color(0xFF00696D))

        val tokens = themeTokens(colorScheme = scheme, isDark = false)

        assertEquals(scheme, tokens.colorScheme)
        assertFalse(tokens.isDark)
        assertEquals(ThemeStyle.Mono, tokens.themeStyle)
        assertEquals(CornerStyle.Rounded, tokens.cornerStyle)
        assertTrue(tokens.tagColors.isEmpty(), "留空表示回落到主题风格的底座色板")
        assertSame(Typography, tokens.typography, "品牌没指定排版时应沿用 kit 的 Mono 排版")
    }

    @Test
    fun 明暗各取对应的扩展语义色() {
        val light = themeTokens(lightColorScheme(), isDark = false)
        val dark = themeTokens(darkColorScheme(), isDark = true)

        // 不直接比对内部常量，只验「两套确实不同」——避免测试把私有色值抄一遍
        assertNotEquals(light.extendedColors.success, dark.extendedColors.success)
        assertNotEquals(light.extendedColors.aiAccent, dark.extendedColors.aiAccent)
    }

    @Test
    fun 品牌可以覆盖圆角与标签色板() {
        val tags = mapOf("neutral" to Color(0xFF334455), "rose" to Color(0xFFAA5566))

        val tokens = themeTokens(
            colorScheme = lightColorScheme(),
            isDark = false,
            cornerStyle = CornerStyle.Square,
            tagColors = tags,
        )

        assertEquals(CornerStyle.Square, tokens.cornerStyle)
        assertEquals(tags, tokens.tagColors)
    }

    @Test
    fun Mono预设_颜色来自Mono色板且标签色板已填充() {
        val light = monoThemeTokens(isDark = false)
        val dark = monoThemeTokens(isDark = true)

        assertEquals(Mono.primaryLight, light.colorScheme.primary)
        assertEquals(Mono.primaryDark, dark.colorScheme.primary)
        assertEquals(CornerStyle.Rounded, light.cornerStyle)
        assertEquals(ThemeStyle.Mono, light.themeStyle)
        assertTrue(light.isDark.not())
        assertTrue(dark.isDark)
        // 预设必须自带标签色板，否则 LocalTagColors 会掉回默认浅色盘
        assertEquals(TagColorNames.toSet(), light.tagColors.keys)
        assertEquals(TagColorNames.toSet(), dark.tagColors.keys)
    }

    @Test
    fun Notion预设_米色纸面墨字圆角4且深浅各一套() {
        val light = notionThemeTokens(isDark = false)
        val dark = notionThemeTokens(isDark = true)

        // 规范给的浅色契约：纸面 #f7f6f3 + 白页 + 墨 #37352f
        assertEquals(Color(0xFFF7F6F3), light.colorScheme.background)
        assertEquals(Color(0xFFFFFFFF), light.colorScheme.surface)
        assertEquals(Color(0xFF37352F), light.colorScheme.onSurface)
        // 圆角一律 4px（只有 extraLarge 弹层给到 8px）
        assertEquals(RoundedCornerShape(4.dp), light.shapes.medium)
        assertEquals(RoundedCornerShape(8.dp), light.shapes.extraLarge)
        assertEquals(ThemeStyle.Notion, light.themeStyle)
        assertEquals(CornerStyle.Rounded, light.cornerStyle)
        assertEquals(TagColorNames.toSet(), light.tagColors.keys)
        // 深色是自建反转，不能跟浅色同一套
        assertNotEquals(light.colorScheme.background, dark.colorScheme.background)
        assertTrue(dark.isDark)
    }
}
