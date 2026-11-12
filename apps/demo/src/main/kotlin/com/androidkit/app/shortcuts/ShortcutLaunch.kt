package com.androidkit.app.shortcuts

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutManagerCompat

/**
 * 一次「从桌面长按菜单进来」的启动。
 *
 * [seq] 每次自增：同一个动作连点两次（新建 → 返回 → 再新建）也得让页面再执行一遍。
 * Compose 侧拿它当 key —— 只比枚举值的话第二次会被当成同一个状态吞掉。
 */
data class ShortcutLaunch(val shortcut: AppShortcut, val seq: Long)

/**
 * 从启动 Intent 认出快捷方式动作；不是快捷方式启动就返回 null（就是正常点桌面图标）。
 *
 * 静态条目（`res/xml/shortcuts.xml`）靠 [AppShortcut.action] 认 —— 静态 `<intent>` 带不了 extra；
 * 动态条目（`ShortcutManagerCompat.setDynamicShortcuts()` 发的）靠 [AppShortcut.EXTRA_ID] 认。
 */
fun Intent.shortcutOrNull(): AppShortcut? =
    AppShortcut.fromAction(action) ?: AppShortcut.fromId(getStringExtra(AppShortcut.EXTRA_ID))

/**
 * 把「用户用了这个快捷方式」报给系统：桌面据此调整长按菜单里的排序。
 *
 * 极少数 ROM / 老系统会抛 IllegalArgumentException（id 在系统里查不到），
 * 但快捷方式已经打开了，排序没报上不影响功能，静默跳过。
 */
fun AppShortcut.reportUsage(context: Context) {
    runCatching { ShortcutManagerCompat.reportShortcutUsed(context, id) }
}
