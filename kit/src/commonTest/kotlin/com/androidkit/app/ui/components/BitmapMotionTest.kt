package com.androidkit.app.ui.components

import com.androidkit.app.ui.theme.quantize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 位图（Bitmap）主题里几个纯逻辑的自检 —— 都是"算错了 UI 上直接乱"的逻辑：
 * 量化（帧制跳变）、网点抖动密度、溶解顺序、点阵柱高归一化、数字翻滚错峰。
 */
class BitmapMotionTest {

    // ── 量化：帧制动效的底座

    @Test
    fun quantizeSnapsToSteps() {
        assertEquals(0f, quantize(0.1f, 4))
        assertEquals(0.25f, quantize(0.3f, 4))
        assertEquals(0.5f, quantize(0.5f, 4))
        assertEquals(0.75f, quantize(0.9f, 4))   // 向下取整：档位到点才跳
        assertEquals(1f, quantize(1f, 4))
    }

    @Test
    fun quantizeClampsOutOfRange() {
        assertEquals(0f, quantize(-1f, 4))
        assertEquals(1f, quantize(2f, 4))
        // steps <= 0：没有档位，直接终态/初态
        assertEquals(0f, quantize(0.5f, 0))
        assertEquals(1f, quantize(1f, 0))
    }

    // ── 抖动网点：四档密度

    @Test
    fun bayerLevelsAreMonotonic() {
        // 档位越高网点越多：低档亮了的格子，高档必须还亮着
        for (y in 0 until 4) {
            for (x in 0 until 4) {
                for (level in 1..3) {
                    assertTrue(
                        !bayerOn(x, y, level) || bayerOn(x, y, level + 1),
                        "档位 $level→${level + 1} 出现网点忽亮忽灭 @($x,$y)"
                    )
                }
            }
        }
    }

    @Test
    fun bayerDensityMatchesLevels() {
        // 一档 25% / 二档 50% / 三档 75% / 四档 100%
        val densities = (1..4).map { level ->
            var n = 0
            for (y in 0 until 4) for (x in 0 until 4) if (bayerOn(x, y, level)) n++
            n / 16f
        }
        assertEquals(listOf(0.25f, 0.5f, 0.75f, 1f), densities)
    }

    @Test
    fun bayerLevelZeroIsEmptyAndFourIsFull() {
        for (y in 0 until 4) {
            for (x in 0 until 4) {
                assertTrue(!bayerOn(x, y, 0), "0 档不该有网点")
                assertTrue(bayerOn(x, y, 4), "4 档应该满格")
            }
        }
    }

    // ── 溶解顺序：整排列 + 同 seed 稳定

    @Test
    fun dissolveOrderIsPermutation() {
        val cols = 7
        val rows = 5
        val order = dissolveOrder(cols, rows, 42)
        assertEquals(cols * rows, order.size)
        assertEquals((0 until cols * rows).toSet(), order.toSet())
    }

    @Test
    fun dissolveOrderIsDeterministic() {
        assertEquals(
            dissolveOrder(4, 3, 7).toList(),
            dissolveOrder(4, 3, 7).toList()
        )
    }

    // ── 柱状图归一化

    @Test
    fun normalizeColumnsScalesToMaxRows() {
        val h = normalizeColumns(listOf(1, 5, 10), 8)
        assertEquals(3, h.size)
        assertEquals(8, h[2])       // 最大值占满
        assertEquals(1, h[0])       // 最小非零值至少 1 点
        assertTrue(h[1] in 1..8)
    }

    @Test
    fun normalizeColumnsKeepsZeroColumnsEmpty() {
        assertEquals(listOf(0, 4, 0), normalizeColumns(listOf(0, 8, 0), 4).toList())
    }

    @Test
    fun normalizeColumnsEmptyInput() {
        assertEquals(0, normalizeColumns(emptyList(), 8).size)
    }

    // ── 数字翻滚错峰：个位先停，高位后停

    @Test
    fun staggeredRollStopsFromRightToLeft() {
        // 3 位数：indexFromRight = 0 是个位
        val total = 3
        val rollSteps = 3
        // 进度刚起步：所有位都还没滚
        assertEquals(0f, staggeredRoll(0f, 0, total, rollSteps))
        // 走过了个位自己的窗口（3/5 = 0.6）：个位已停稳，高位还在滚
        assertEquals(1f, staggeredRoll(0.7f, 0, total, rollSteps), "个位应先停")
        assertTrue(staggeredRoll(0.7f, 2, total, rollSteps) < 1f, "高位此时不该停")
        // 全程走完：所有位都停稳
        assertEquals(1f, staggeredRoll(1f, 0, total, rollSteps))
        assertEquals(1f, staggeredRoll(1f, 2, total, rollSteps))
    }

    @Test
    fun dotGlyphsCoverDigitsAndSigns() {
        for (c in "0123456789-.".toList()) {
            assertEquals(7, DOT_GLYPHS.getValue(c).size, "字形 $c 应有 7 行")
        }
        assertEquals(null, glyphOf('A'))
    }
}
