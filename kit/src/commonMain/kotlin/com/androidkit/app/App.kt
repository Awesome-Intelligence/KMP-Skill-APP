package com.androidkit.app

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.components.AppCard
import com.androidkit.app.ui.components.EInkInkAction
import com.androidkit.app.ui.components.EInkPaperField
import com.androidkit.app.ui.components.EInkPanel
import com.androidkit.app.ui.components.EInkReadingRow
import com.androidkit.app.ui.components.eInkGrain
import com.androidkit.app.ui.components.MacOSListRow
import com.androidkit.app.ui.components.MacOSSegmentedControl
import com.androidkit.app.ui.components.MacOSSystemButton
import com.androidkit.app.ui.components.MacOSToolbar
import com.androidkit.app.ui.components.MacOSVibrancyPanel
import com.androidkit.app.ui.components.MacOSVibrancySidebar
import com.androidkit.app.ui.components.NotionBlockRow
import com.androidkit.app.ui.components.NotionCallout
import com.androidkit.app.ui.components.NotionGhostButton
import com.androidkit.app.ui.components.NotionInlineInput
import com.androidkit.app.ui.components.PrimaryButton
import com.androidkit.app.ui.components.SecondaryButton
import com.androidkit.app.ui.components.SwissColorField
import com.androidkit.app.ui.components.SwissGrid
import com.androidkit.app.ui.components.SwissInlineButton
import com.androidkit.app.ui.components.SwissPage
import com.androidkit.app.ui.components.SwissSection
import com.androidkit.app.ui.components.SwissSectionLabel
import com.androidkit.app.ui.components.SwissStructuralRow
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.LocalIsDarkTheme
import com.androidkit.app.ui.theme.LocalReducedMotion
import com.androidkit.app.ui.theme.LocalThemeStyle
import com.androidkit.app.ui.theme.Swiss
import com.androidkit.app.ui.theme.ThemeStyle

/**
 * 三端共用的演示页。
 *
 * 装进新项目后换成自己的首页就行 —— 这里只是让 kit 自己能跑起来、能截图、能验证编译。
 *
 * 顺带演示几套预置主题风格：切风格只是给 [AppTheme] 换个 [ThemeStyle] 参数，
 * 底座预置几套配色/排版/形状/动效，App 层自己决定用哪套并持久化 —— kit 不带设置页。
 */
@Composable
fun App() {
    // 初值继承外层 AppTheme（各端入口定的默认风格），下面的开关改的是本页自己的
    val outerStyle = LocalThemeStyle.current
    val outerDark = LocalIsDarkTheme.current
    var style by remember { mutableStateOf(outerStyle) }
    var dark by remember { mutableStateOf(outerDark) }
    // 外层 AppTheme 已经定过减动效，内层别把它重置回 false
    val reducedMotion = LocalReducedMotion.current

    // 内层 AppTheme 覆盖外层的（调用方已经包了一层 AppTheme），
    // 这样本页能演示风格切换而不必改各端入口。
    AppTheme(style = style, darkTheme = dark, reducedMotion = reducedMotion) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Android-Kit", style = MaterialTheme.typography.headlineMedium)

            StyleSwitcher(
                style = style,
                dark = dark,
                onStyle = { style = it },
                onDark = { dark = it }
            )

            AppCard {
                Text("Compose Multiplatform 演示", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "同一套组件：Android / iOS / Desktop",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (style == ThemeStyle.Swiss) {
                SwissShowcase()
            }

            if (style == ThemeStyle.Notion) {
                NotionShowcase()
            }

            if (style == ThemeStyle.EInk) {
                EInkShowcase()
            }

            if (style == ThemeStyle.MacOS) {
                MacOSShowcase()
            }

            PrimaryButton(text = "点一下", onClick = {})
        }
    }
}

/** 风格切换 —— 演示用，不是设置页（App 层自己存选择） */
@Composable
private fun StyleSwitcher(
    style: ThemeStyle,
    dark: Boolean,
    onStyle: (ThemeStyle) -> Unit,
    onDark: (Boolean) -> Unit,
) {
    AppCard {
        Text("主题风格", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SecondaryButton(
                text = "Mono",
                onClick = { onStyle(ThemeStyle.Mono) },
                enabled = style != ThemeStyle.Mono
            )
            SecondaryButton(
                text = "Swiss",
                onClick = { onStyle(ThemeStyle.Swiss) },
                enabled = style != ThemeStyle.Swiss
            )
            SecondaryButton(
                text = "Notion",
                onClick = { onStyle(ThemeStyle.Notion) },
                enabled = style != ThemeStyle.Notion
            )
            SecondaryButton(
                text = "E-Ink",
                onClick = { onStyle(ThemeStyle.EInk) },
                enabled = style != ThemeStyle.EInk
            )
            SecondaryButton(
                text = "macOS",
                onClick = { onStyle(ThemeStyle.MacOS) },
                enabled = style != ThemeStyle.MacOS
            )
        }
        Spacer(Modifier.height(8.dp))
        SecondaryButton(
            text = if (dark) "深色：开" else "深色：关",
            onClick = { onDark(!dark) }
        )
    }
}

/** 瑞士风格组件样例 —— 规范里的四个角色各来一个，用十二栏网格排，不套卡片 */
@Composable
private fun SwissShowcase() {
    SwissPage {
        Text("瑞士风格组件", style = MaterialTheme.typography.titleMedium)

        SwissSection {
            SwissSectionLabel(title = "Index", numeral = "01")

            SwissStructuralRow(
                numeral = "01",
                title = "Helvetica Neue",
                supporting = "Neue Haas Grotesk, 1957",
                onClick = {}
            )
            SwissStructuralRow(
                numeral = "02",
                title = "International Style",
                supporting = "Zürich · Basel",
                onClick = {}
            )
        }

        SwissSection {
            SwissGrid(
                columns = listOf(
                    8 to {
                        SwissInlineButton(text = "Explore", onClick = {})
                    },
                    4 to {
                        SwissColorField(color = Swiss.redBlock, title = "Swiss")
                    }
                )
            )
        }
    }
}

/** Notion 风格组件样例 —— 规范里的四个角色各来一个 */
@Composable
private fun NotionShowcase() {
    var note by remember { mutableStateOf("") }

    AppCard {
        Text("Notion 风格组件", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(12.dp))
        NotionBlockRow(title = "文档块", supporting = "悬停出拖拽手柄 ⋮⋮")

        Spacer(Modifier.height(12.dp))
        NotionGhostButton(text = "新建页面", onClick = {})

        Spacer(Modifier.height(12.dp))
        NotionInlineInput(
            value = note,
            onValueChange = { note = it },
            placeholder = "行内输入，聚焦只有一条底线"
        )

        Spacer(Modifier.height(12.dp))
        NotionCallout(text = "提示块：淡底 + 4px 圆角，正文 14px/1.6。")
    }
}

/** 电子纸风格组件样例 —— 规范里的角色各来一个 */
@Composable
private fun EInkShowcase() {
    var query by remember { mutableStateOf("") }

    EInkPanel {
        Text("电子纸组件", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(12.dp))
        EInkReadingRow(
            index = "01",
            title = "样例数据",
            supporting = "示例值，非真机读数",
            value = "21.4",
            unit = "°C",
            state = "样例"
        )
        EInkReadingRow(
            index = "02",
            title = "已选中的行",
            value = "128",
            unit = "条",
            selected = true
        )
        EInkReadingRow(
            index = "03",
            title = "离线行",
            supporting = "规范：离线用空心标记 + 文字",
            value = "0",
            unit = "次",
            state = "离线",
            stateFilled = false
        )

        Spacer(Modifier.height(16.dp))
        EInkPaperField(
            value = query,
            onValueChange = { query = it },
            label = "搜索归档",
            placeholder = "输入关键词"
        )

        Spacer(Modifier.height(12.dp))
        EInkInkAction(text = "打开阅读列表", onClick = {})
    }

    Spacer(Modifier.height(12.dp))
    // 网点底纹只做装饰 —— 正文和控件保持实心
    EInkPanel(modifier = Modifier.eInkGrain()) {
        Text("装饰性网点（5dp 一格）", style = MaterialTheme.typography.bodySmall)
    }
}

/** macOS 毛玻璃组件样例 —— 规范里的四个角色各来一个 */
@Composable
private fun MacOSShowcase() {
    var segment by remember { mutableIntStateOf(0) }
    var selectedRow by remember { mutableStateOf(false) }

    MacOSToolbar(title = "待办")

    Spacer(Modifier.height(12.dp))
    // 规范布局：240px 侧边栏 + 内容列
    Row(modifier = Modifier.fillMaxWidth()) {
        MacOSVibrancySidebar {
            Column {
                MacOSListRow(title = "全部", trailing = "12")
                MacOSListRow(title = "今天", trailing = "3")
                MacOSListRow(title = "已完成", trailing = "1")
            }
        }
        Column(modifier = Modifier.weight(1f).padding(12.dp)) {
            MacOSSegmentedControl(
                options = listOf("全部", "今天", "已完成"),
                selectedIndex = segment,
                onSelect = { segment = it }
            )

            Spacer(Modifier.height(12.dp))
            MacOSVibrancyPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    MacOSListRow(title = "侧边栏行 1", trailing = "12")
                    MacOSListRow(title = "侧边栏行 2", trailing = "3")
                    MacOSListRow(
                        title = "选中态：系统蓝 18%",
                        trailing = "1",
                        selected = selectedRow,
                        onClick = { selectedRow = !selectedRow }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            MacOSSystemButton(text = "新建待办", onClick = {})
        }
    }
}
