package com.androidkit.app.shortcuts

/**
 * 桌面图标长按菜单里的动作（Android 官方叫 App Shortcuts，iOS 叫 Home Screen Quick Actions）。
 *
 * 每个动作有**两条契约字符串**，别混用：
 * - [id]：快捷方式自己的 id。`res/xml/shortcuts.xml` 的 `android:shortcutId` 必须和它一致；
 *   系统按 id 记住「用户用了哪个」，运行时上报（[reportUsage]）也只认 id。
 * - [action]：点开快捷方式时给 Activity 的 Intent action。`shortcuts.xml` 里
 *   `<intent android:action>` 必须和它一致 —— 静态条目的 `<intent>` 带不了 extra，只能靠 action 认。
 *
 * 枚举和 `shortcuts.xml` 要一起改，`AppShortcutSyncTest` 会读 XML 对账；
 * 文案 / 图标只在 `values/strings.xml` 和 drawable 里定义，由 XML 引用（AAPT 会校验存在性）。
 *
 * 静态声明（`shortcuts.xml`）= 装完就在，不用先启动；要按用户数据变的条目（比如「继续上次对话」）
 * 在运行时用 `ShortcutManagerCompat.setDynamicShortcuts()` 追加，Intent 里带上 [EXTRA_ID]，
 * [shortcutOrNull] 会把它和静态条目一样认出来。
 */
enum class AppShortcut(
    val id: String,
    val action: String,
) {
    /** 新建待办：进聊天页，输入框预填「记一下：」并弹键盘，用户只补待办内容。 */
    NEW_TODO(id = "new_todo", action = "com.androidkit.app.shortcut.new_todo"),

    /** 问助手：进聊天页，光标直接落在输入框里。 */
    ASK(id = "ask", action = "com.androidkit.app.shortcut.ask"),

    /** 设置：直接打开设置页。 */
    SETTINGS(id = "settings", action = "com.androidkit.app.shortcut.settings"),
    ;

    companion object {
        /** 动态快捷方式的 Intent 里放 [AppShortcut.id] 的 key（静态条目用不了 extra）。 */
        const val EXTRA_ID = "com.androidkit.app.extra.shortcut_id"

        /** 按 `<intent android:action>` 认动作 —— 静态快捷方式走这条。 */
        fun fromAction(action: String?): AppShortcut? = entries.firstOrNull { it.action == action }

        /** 按 id 认动作 —— 动态快捷方式（带 extra）走这条。 */
        fun fromId(id: String?): AppShortcut? = entries.firstOrNull { it.id == id }
    }
}
