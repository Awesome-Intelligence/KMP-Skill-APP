package com.androidkit.app.shortcuts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * `res/xml/shortcuts.xml` 是**手写**的声明，编译器不检查它和 [AppShortcut] 枚举还对不对得上。
 * 比如改了 action 却忘了改 XML：编译通过、长按菜单也在，但点进去 app 认不出动作 ——
 * 这种漂移只能靠测试拦，所以这里读 XML 跟枚举对账。
 */
class AppShortcutSyncTest {

    @Test
    fun `shortcuts_xml 和 AppShortcut 枚举双向一致`() {
        val declared = parseShortcuts()

        // 枚举 → XML：每个动作都得在长按菜单里露脸，否则这个动作等于没有入口
        assertEquals("枚举里的动作数和 shortcuts.xml 对不上", AppShortcut.entries.size, declared.size)

        declared.forEach { shortcut ->
            val id = shortcut.attr("shortcutId")
            val intent = shortcut.getElementsByTagName("intent").item(0) as? Element
            assertNotNull("<shortcut shortcutId=\"$id\"> 缺 <intent>", intent)

            val match = AppShortcut.fromId(id)
            assertNotNull("shortcuts.xml 里的 shortcutId=$id 在 AppShortcut 枚举里不存在", match)
            assertEquals("shortcutId=$id 的 action 和枚举不一致", match?.action, intent?.attr("action"))
            assertEquals(
                "shortcutId=$id 的 targetClass 应指向 MainActivity",
                MAIN_ACTIVITY,
                intent?.attr("targetClass"),
            )
        }
    }

    @Test
    fun `id 和 action 都不重复，未知输入不会被误认`() {
        assertEquals("id 有重复", AppShortcut.entries.size, AppShortcut.entries.map { it.id }.toSet().size)
        assertEquals("action 有重复", AppShortcut.entries.size, AppShortcut.entries.map { it.action }.toSet().size)

        assertNull("普通启动（没有 action）不该认出快捷方式", AppShortcut.fromAction(null))
        assertNull("不是我们的 action 不该认出快捷方式", AppShortcut.fromAction("android.intent.action.MAIN"))
        assertNull("未知 id 不该认出快捷方式", AppShortcut.fromId("not_a_shortcut"))
    }

    private fun parseShortcuts(): List<Element> {
        // 单测的工作目录是模块目录（apps/demo）；从仓库根跑也认一次，省得换 IDE 就红
        val xml = listOf(
            File("src/main/res/xml/shortcuts.xml"),
            File("apps/demo/src/main/res/xml/shortcuts.xml"),
        ).firstOrNull { it.isFile } ?: error("找不到 res/xml/shortcuts.xml，工作目录=${File("").absolutePath}")

        assertTrue("shortcuts.xml 是空的", xml.length() > 0)

        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val nodes = factory.newDocumentBuilder().parse(xml).getElementsByTagName("shortcut")
        return (0 until nodes.length).map { nodes.item(it) as Element }
    }

    /** `android:` 命名空间下的属性；带前缀的 getAttribute 在 namespace-aware 解析下不可靠。 */
    private fun Element.attr(name: String): String =
        getAttributeNS("http://schemas.android.com/apk/res/android", name)

    private companion object {
        /** 跟着 manifest 的 `android:name=".MainActivity"` 走；fork 改 namespace 时这里也要改。 */
        const val MAIN_ACTIVITY = "com.androidkit.app.MainActivity"
    }
}
