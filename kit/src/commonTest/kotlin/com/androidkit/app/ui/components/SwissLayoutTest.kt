package com.androidkit.app.ui.components

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SwissLayoutTest {

    @Test
    fun 栏位校验_跨度合法时不抛() {
        validateGridSpans(listOf(12))
        validateGridSpans(listOf(8, 4))
        validateGridSpans(listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1))
    }

    @Test
    fun 栏位校验_超界和越界都抛() {
        assertFailsWith<IllegalArgumentException> { validateGridSpans(listOf()) }
        assertFailsWith<IllegalArgumentException> { validateGridSpans(listOf(0)) }
        assertFailsWith<IllegalArgumentException> { validateGridSpans(listOf(13)) }
        assertFailsWith<IllegalArgumentException> { validateGridSpans(listOf(8, 8)) }
    }

    @Test
    fun 布局常量_跟规范对得上() {
        assertTrue(SwissLayout.COLUMNS == 12)
        assertTrue(SwissLayout.Gutter.value == 24f)
        assertTrue(SwissLayout.MaxWidth.value == 1200f)
        assertTrue(SwissLayout.SectionGap.value == 96f)
        assertTrue(SwissLayout.ElementGap.value == 16f)
    }
}
