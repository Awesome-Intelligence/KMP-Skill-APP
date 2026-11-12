package com.androidkit.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.data.settings.AppSettings
import com.androidkit.app.res.*
import com.androidkit.app.ui.components.AppSettingsRow
import com.androidkit.app.ui.components.Header
import com.androidkit.app.ui.components.SelectDialog
import com.androidkit.app.ui.components.ToggleRow
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.AppearanceMode
import com.androidkit.app.ui.theme.ThemeStyle
import org.jetbrains.compose.resources.stringResource

/**
 * 设置页 —— 底座自带的通用设置（外观 / 动效）。
 *
 * 状态由调用方持有并落盘（kit 自带 [com.androidkit.app.data.settings.SettingsStore]）：
 * 本页只管显示和回调，不碰存储 —— 这样 app 想存哪、怎么存都行。
 *
 * app 自己的设置项（账号、数据管理等）用 [extraSections] 插在通用项下面。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    extraSections: @Composable ColumnScope.() -> Unit = {},
) {
    var showStyleDialog by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }

    val styleLabel = themeStyleLabel(settings.themeStyle)
    val appearanceLabel = when (settings.appearanceMode) {
        AppearanceMode.System -> stringResource(Res.string.follow_system)
        AppearanceMode.Light -> stringResource(Res.string.light_mode)
        AppearanceMode.Dark -> stringResource(Res.string.dark_mode)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Header(
                title = stringResource(Res.string.settings),
                leading1 = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surface)
        ) {
            SettingsSectionHeader(stringResource(Res.string.appearance))

            SettingsCard {
                AppSettingsRow(
                    icon = Icons.Outlined.Palette,
                    title = stringResource(Res.string.theme_style),
                    subtitle = styleLabel,
                    onClick = { showStyleDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                AppSettingsRow(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(Res.string.appearance_mode),
                    subtitle = appearanceLabel,
                    onClick = { showAppearanceDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ToggleRow(
                    icon = Icons.Outlined.Animation,
                    title = stringResource(Res.string.reduce_motion),
                    subtitle = stringResource(Res.string.reduce_motion_desc),
                    isChecked = settings.reducedMotion,
                    onCheckedChange = { onSettingsChange(settings.copy(reducedMotion = it)) }
                )
            }

            // app 自己的设置项接这儿
            extraSections()
        }
    }

    if (showStyleDialog) {
        SelectDialog(
            title = stringResource(Res.string.theme_style),
            options = ThemeStyle.entries.map { themeStyleLabel(it) },
            selectedIndex = ThemeStyle.entries.indexOf(settings.themeStyle),
            onSelect = { index ->
                onSettingsChange(settings.copy(themeStyle = ThemeStyle.entries[index]))
            },
            onDismiss = { showStyleDialog = false }
        )
    }

    if (showAppearanceDialog) {
        SelectDialog(
            title = stringResource(Res.string.appearance_mode),
            options = listOf(
                stringResource(Res.string.follow_system),
                stringResource(Res.string.light_mode),
                stringResource(Res.string.dark_mode),
            ),
            selectedIndex = when (settings.appearanceMode) {
                AppearanceMode.System -> 0
                AppearanceMode.Light -> 1
                AppearanceMode.Dark -> 2
            },
            onSelect = { index ->
                val mode = when (index) {
                    1 -> AppearanceMode.Light
                    2 -> AppearanceMode.Dark
                    else -> AppearanceMode.System
                }
                onSettingsChange(settings.copy(appearanceMode = mode))
            },
            onDismiss = { showAppearanceDialog = false }
        )
    }
}

/** 小节标题（M3 list section header） */
@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/** 分组卡片：内容不自带内边距，行自带 padding —— 涟漪能铺到卡片边 */
@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column { content() }
    }
}

@Preview(showBackground = true)
@Composable
internal fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            settings = AppSettings(),
            onSettingsChange = {},
            onBackClick = {}
        ) // ponytail: demo data only
    }
}

/**
 * 主题风格的中文/英文名。
 *
 * 选项列表直接由 [ThemeStyle.entries] 生成 —— 手动维护 listOf(...) 早晚跟枚举脱节，
 * 一脱节 `selectedIndex` 就指错人。新增风格要配文案，漏了编译不过。
 */
@Composable
private fun themeStyleLabel(style: ThemeStyle): String = when (style) {
    ThemeStyle.Mono -> stringResource(Res.string.theme_mono)
    ThemeStyle.Swiss -> stringResource(Res.string.theme_swiss)
    ThemeStyle.Notion -> stringResource(Res.string.theme_notion)
    ThemeStyle.EInk -> stringResource(Res.string.theme_eink)
    ThemeStyle.MacOS -> stringResource(Res.string.theme_macos)
}
