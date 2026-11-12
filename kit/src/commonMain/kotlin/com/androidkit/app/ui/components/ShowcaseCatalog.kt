package com.androidkit.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.androidkit.app.ui.agent.ChatMessageListPreview
import com.androidkit.app.ui.agent.ToolApprovalCardPreview
import com.androidkit.app.ui.settings.SettingsScreenPreview
import com.androidkit.app.ui.shell.AppShellPreview
import com.androidkit.app.ui.templates.FormFieldPreview
import com.androidkit.app.ui.templates.FormScreenPreview
import com.androidkit.app.ui.templates.FormSectionPreview
import com.androidkit.app.ui.templates.ListItemCardPreview
import com.androidkit.app.ui.templates.ListScreenPreview
import com.androidkit.app.ui.theme.Swiss
import com.androidkit.app.ui.theme.ThemeStyle

/**
 * 组件样例目录 —— 把 kit 里所有能渲染的东西按分类列一遍。
 *
 * 样例本身**复用各组件文件里的 `*Preview` 函数**（那些函数本来就是给每个组件写的最小样例），
 * 所以目录跟组件是同步的：组件改了，样例跟着改；组件加了样例，目录里加一行就行。
 *
 * 这里只做三件事：分类、命名、把样例摆出来。样例长什么样归各组件自己管。
 */

/** 目录里的一项：组件名 + 渲染它的样例。 */
internal data class ShowcaseItem(
    val name: String,
    val sample: @Composable () -> Unit,
)

/** 目录里的一个分类。 */
internal data class ShowcaseCategory(
    val title: String,
    val items: List<ShowcaseItem>,
)

/**
 * 全部样例，按分类。
 *
 * @param style 当前主题风格 —— 四个风格家族（Swiss / Notion / E-Ink / macOS）各是一套组件，
 *              只在对应风格下有意义，所以只显示当前那套。
 */
internal fun showcaseCategories(style: ThemeStyle): List<ShowcaseCategory> = listOf(
    ShowcaseCategory(
        "容器与卡片",
        listOf(
            ShowcaseItem("AppCard") { AppCardPreview() },
            ShowcaseItem("AppCard（可点击）") { AppCardClickablePreview() },
            ShowcaseItem("SmallCard") { SmallCardPreview() },
            ShowcaseItem("SmallCard（可点击）") { SmallCardClickablePreview() },
            ShowcaseItem("CardTitle") { CardTitlePreview() },
            ShowcaseItem("AppBadge") { AppBadgePreview() },
            ShowcaseItem("StatusMark") {
                Column {
                    StatusMark(filled = true)
                    StatusMark(filled = false)
                }
            },
        )
    ),
    ShowcaseCategory(
        "按钮",
        listOf(
            ShowcaseItem("PrimaryButton") { PrimaryButtonPreview() },
            ShowcaseItem("SecondaryButton") { SecondaryButtonPreview() },
            ShowcaseItem("SmallSecondaryButton") { SmallSecondaryButtonPreview() },
            ShowcaseItem("FilledIconButton") { FilledIconButtonPreview() },
            ShowcaseItem("ScaleIconButton") { ScaleIconButtonPreview() },
            ShowcaseItem("SmallScaleIconButton") { SmallScaleIconButtonPreview() },
            ShowcaseItem("CircleIconButton") { CircleIconButtonPreview() },
            ShowcaseItem("EditButton") { EditButtonPreview() },
            ShowcaseItem("AddButton") { AddButtonPreview() },
            ShowcaseItem("AddFab") { AddFabPreview() },
            ShowcaseItem("ExpandButton") { ExpandButtonPreview() },
            ShowcaseItem("StarFavoriteButton") { StarFavoriteButtonPreview() },
            ShowcaseItem("DeleteButton") { DeleteButtonPreview() },
            ShowcaseItem("TextActionButton") { TextActionButtonPreview() },
        )
    ),
    ShowcaseCategory(
        "输入",
        listOf(
            ShowcaseItem("SimpleTextField") { SimpleTextFieldPreview() },
            ShowcaseItem("SimpleTextField（简版）") { TextFieldsSimpleTextFieldPreview() },
            ShowcaseItem("ComposerField") {
                var text by remember { mutableStateOf("") }
                ComposerField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = "说点什么…",
                    onMicClick = {},
                    micContentDescription = "语音",
                )
            },
            ShowcaseItem("SearchBar") { SearchBarPreview() },
            ShowcaseItem("SearchToggleButton") { SearchToggleButtonPreview() },
            ShowcaseItem("MarkdownEditor") { MarkdownEditorPreview() },
            ShowcaseItem("MarkdownPreview") { MarkdownPreviewPreview() },
            ShowcaseItem("MarkdownToolbar") { MarkdownToolbarPreview() },
            ShowcaseItem("ToolbarIconButton") { ToolbarIconButtonPreview() },
            ShowcaseItem("ToolbarDivider") { ToolbarDividerPreview() },
        )
    ),
    ShowcaseCategory(
        "选择与标签",
        listOf(
            ShowcaseItem("FilterChip") { FilterChipPreview() },
            ShowcaseItem("AddTagChip") { AddTagChipPreview() },
            ShowcaseItem("AnswerChip") { AnswerChip("是", onClick = {}) },
            ShowcaseItem("FilterChipRow") { FilterChipRowPreview() },
            ShowcaseItem("FilterChipRow（带类型）") { FilterChipRowTypedPreview() },
            ShowcaseItem("FilterChipRow（带色点）") { FilterChipRowDotPreview() },
            ShowcaseItem("SelectableTag") { SelectableTagPreview() },
            ShowcaseItem("Checkbox") { CheckboxPreview() },
            ShowcaseItem("ColorDot") { ColorDotPreview() },
            ShowcaseItem("ColorDotRow") { ColorDotRowPreview() },
            ShowcaseItem("TagText") { TagTextPreview() },
            ShowcaseItem("ToggleRow") { ToggleRowPreview() },
        )
    ),
    ShowcaseCategory(
        "筛选",
        listOf(
            ShowcaseItem("FilterSection") { FilterSectionPreview() },
            ShowcaseItem("SingleSelectFilterChips") { SingleSelectFilterChipsPreview() },
            ShowcaseItem("MultiSelectFilterChips") { MultiSelectFilterChipsPreview() },
            ShowcaseItem("ImportanceFilterChips") { ImportanceFilterChipsPreview() },
            ShowcaseItem("TagFilterChips") { TagFilterChipsPreview() },
            ShowcaseItem("DateRangeFilter") { DateRangeFilterPreview() },
            ShowcaseItem("MoodFilterChips") { MoodFilterChipsPreview() },
            ShowcaseItem("FilterBottomSheet") { FilterBottomSheetPreview() },
            ShowcaseItem("ActiveFiltersBar") { ActiveFiltersBarPreview() },
            ShowcaseItem("TagPickerBottomSheet") { TagPickerBottomSheetPreview() },
            ShowcaseItem("TagPickerChip") { TagPickerChipPreview() },
        )
    ),
    ShowcaseCategory(
        "列表与条目",
        listOf(
            ShowcaseItem("AppListItem") { AppListItemPreview() },
            ShowcaseItem("AddListItem") { AddListItemPreview() },
            ShowcaseItem("StatItem") { StatItemPreview() },
            ShowcaseItem("StatsCard") { StatsCardPreview() },
            ShowcaseItem("ContactAvatarItem") { ContactAvatarItemPreview() },
            ShowcaseItem("CheckinHeatmapCard") { CheckinHeatmapCardPreview() },
            ShowcaseItem("HeatmapGrid") { HeatmapGridPreview() },
            ShowcaseItem("StatChip") { StatChipPreview() },
            ShowcaseItem("IllustratedEmptyState") { IllustratedEmptyStatePreview() },
            ShowcaseItem("空态：联系人") { EmptyStatesContactsPreview() },
            ShowcaseItem("空态：目标") { EmptyStatesGoalsPreview() },
            ShowcaseItem("空态：记录") { EmptyStatesRecordsPreview() },
            ShowcaseItem("空态：无结果") { EmptyStatesNoSearchResultsPreview() },
        )
    ),
    ShowcaseCategory(
        "头像",
        listOf(
            ShowcaseItem("UserAvatar") { UserAvatarPreview() },
            ShowcaseItem("AvatarHero") { AvatarHeroPreview() },
            ShowcaseItem("AvatarStack") { AvatarStackPreview() },
            ShowcaseItem("ExpandedAvatarRow") { ExpandedAvatarRowPreview() },
            ShowcaseItem("AvatarItem") { AvatarItemPreview() },
            ShowcaseItem("AvatarBadge") { AvatarBadgePreview() },
        )
    ),
    ShowcaseCategory(
        "日期与时间",
        listOf(
            ShowcaseItem("AppCalendar") { AppCalendarPreview() },
            ShowcaseItem("MonthView") { MonthViewPreview() },
            ShowcaseItem("CalendarHeader") { CalendarHeaderPreview() },
            ShowcaseItem("WeekdayLabels") { WeekdayLabelsPreview() },
            ShowcaseItem("WeekRow") { WeekRowPreview() },
            ShowcaseItem("DayView") { DayViewPreview() },
            ShowcaseItem("WeekView") { WeekViewPreview() },
            ShowcaseItem("CalendarDayCell") { CalendarDayCellPreview() },
            ShowcaseItem("HorizontalDateBar") { HorizontalDateBarPreview() },
            ShowcaseItem("DateCell") { DateCellPreview() },
        )
    ),
    ShowcaseCategory(
        "导航与头部",
        listOf(
            ShowcaseItem("Header") { HeaderPreview() },
            ShowcaseItem("CollapsingHeader") { CollapsingHeaderPreview() },
            ShowcaseItem("HeaderTitle") { HeaderTitlePreview() },
            ShowcaseItem("AppBottomNavigation") { AppBottomNavigationPreview() },
            ShowcaseItem("BottomNavIcon") { BottomNavIconPreview() },
            ShowcaseItem("PageIndicator") { PageIndicatorPreview() },
            ShowcaseItem("SimpleMenuButton") { SimpleMenuButtonPreview() },
            ShowcaseItem("ActionMenuRow") { ActionMenuRowPreview() },
            ShowcaseItem("NotificationsButton") { NotificationsButtonPreview() },
            ShowcaseItem("NotificationRow") { NotificationRowPreview() },
            ShowcaseItem("NotificationsPanel") { NotificationsPanelPreview() },
            ShowcaseItem("NotificationsPanel（空）") { NotificationsPanelEmptyPreview() },
        )
    ),
    ShowcaseCategory(
        "弹层",
        listOf(
            ShowcaseItem("ConfirmDialog") { ConfirmDialogPreview() },
            ShowcaseItem("SelectDialog") { SelectDialogPreview() },
            ShowcaseItem("DeleteConfirmDialog") { DeleteConfirmDialogPreview() },
            ShowcaseItem("InputDialog") { InputDialogPreview() },
            ShowcaseItem("LoadingDialog") { LoadingDialogPreview() },
            ShowcaseItem("SelectDropdown") { SelectDropdownPreview() },
            ShowcaseItem("UpdateDialog") { UpdateDialogPreview() },
            ShowcaseItem("UpdateDialog（强制）") { UpdateDialogMandatoryPreview() },
            ShowcaseItem("QuotaPaywall") { QuotaPaywallPreview() },
        )
    ),
    ShowcaseCategory(
        "进度与加载",
        listOf(
            ShowcaseItem("LinearProgressBar") { LinearProgressBarPreview() },
            ShowcaseItem("CircularProgressIndicatorWithText") { CircularProgressIndicatorWithTextPreview() },
            ShowcaseItem("AppSnackbar") { AppSnackbarPreview() },
            ShowcaseItem("LoadingIndicator") { LoadingIndicatorPreview() },
            ShowcaseItem("LoadingOverlay") { LoadingOverlayPreview() },
            ShowcaseItem("CenteredLoading") { CenteredLoadingPreview() },
            ShowcaseItem("ShimmerBox") { ShimmerBoxPreview() },
            ShowcaseItem("SkeletonTextLine") { SkeletonTextLinePreview() },
            ShowcaseItem("SkeletonRect") { SkeletonRectPreview() },
            ShowcaseItem("SkeletonCircle") { SkeletonCirclePreview() },
            ShowcaseItem("SkeletonListItem") { SkeletonListItemPreview() },
            ShowcaseItem("SkeletonCard") { SkeletonCardPreview() },
            ShowcaseItem("SkeletonGridItem") { SkeletonGridItemPreview() },
            ShowcaseItem("HomeFavoriteGoalsSkeleton") { HomeFavoriteGoalsSkeletonPreview() },
            ShowcaseItem("HomeScreenSkeleton") { HomeScreenSkeletonPreview() },
            ShowcaseItem("GoalListSkeleton") { GoalListSkeletonPreview() },
            ShowcaseItem("RecordListSkeleton") { RecordListSkeletonPreview() },
            ShowcaseItem("ContactListSkeleton") { ContactListSkeletonPreview() },
            ShowcaseItem("GoalDetailSkeleton") { GoalDetailSkeletonPreview() },
            ShowcaseItem("ContactDetailSkeleton") { ContactDetailSkeletonPreview() },
        )
    ),
    ShowcaseCategory(
        "AI 与 Agent",
        listOf(
            ShowcaseItem("AIInsightCard") { AIInsightCardPreview() },
            ShowcaseItem("AISuggestionBadge") { AISuggestionBadgePreview() },
            ShowcaseItem("AISuggestionItem") { AISuggestionItemPreview() },
            ShowcaseItem("AISuggestionChip") { AISuggestionChipPreview() },
            ShowcaseItem("AILoadingSkeleton") { AILoadingSkeletonPreview() },
            ShowcaseItem("BreathingIcon") { BreathingIconPreview() },
            ShowcaseItem("GoalAIButton") { GoalAIButtonPreview() },
            ShowcaseItem("AIActionButton") { AIActionButtonPreview() },
            ShowcaseItem("ChatMessageList") { ChatMessageListPreview() },
            ShowcaseItem("ToolApprovalCard") { ToolApprovalCardPreview() },
        )
    ),
    ShowcaseCategory(
        "页面模板与壳",
        listOf(
            ShowcaseItem("AppShell") { AppShellPreview() },
            ShowcaseItem("ListScreen") { ListScreenPreview() },
            ShowcaseItem("ListItemCard") { ListItemCardPreview() },
            ShowcaseItem("FormScreen") { FormScreenPreview() },
            ShowcaseItem("FormField") { FormFieldPreview() },
            ShowcaseItem("FormSection") { FormSectionPreview() },
            ShowcaseItem("SettingsScreen") { SettingsScreenPreview() },
            ShowcaseItem("AppSettingsRow") { AppSettingsRowPreview() },
            ShowcaseItem("AppSettingsIcon") { AppSettingsIconPreview() },
            ShowcaseItem("AppIconTile") { AppIconTilePreview() },
        )
    ),
    ShowcaseCategory(
        "工具类",
        listOf(
            ShowcaseItem("ScrollToTopEffect") {
                Text(
                    "滚动到顶部的联动效果 —— 没有静态外观，靠行为体现（底部导航重复点击已选中项时回顶）。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            ShowcaseItem("AdaptiveGridCells") {
                Text(
                    "网格列数计算（LazyVerticalGrid 的 GridCells）—— 没有静态外观。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    ),
    ShowcaseCategory(
        "主题风格组件",
        styleSamples(style),
    ),
)

/**
 * 四个风格家族各是一套独立组件，只在对应风格下有意义 —— 只列当前风格那一套。
 */
private fun styleSamples(style: ThemeStyle): List<ShowcaseItem> = when (style) {
    ThemeStyle.Swiss -> listOf(
        ShowcaseItem("SwissPage") {
            SwissPage {
                SwissSection {
                    SwissSectionLabel(title = "Index", numeral = "01")
                }
            }
        },
        ShowcaseItem("SwissSection") {
            SwissSection { Text("内容", style = MaterialTheme.typography.bodyMedium) }
        },
        ShowcaseItem("SwissSectionLabel") { SwissSectionLabel(title = "Index", numeral = "01") },
        ShowcaseItem("SwissStructuralRow") {
            SwissStructuralRow(
                numeral = "01",
                title = "Helvetica Neue",
                supporting = "Neue Haas Grotesk, 1957",
                onClick = {},
            )
        },
        ShowcaseItem("SwissGrid") {
            SwissGrid(
                columns = listOf(
                    8 to { SwissInlineButton(text = "Explore", onClick = {}) },
                    4 to { SwissColorField(color = Swiss.redBlock, title = "Swiss") },
                )
            )
        },
        ShowcaseItem("SwissInlineButton") { SwissInlineButton(text = "Explore", onClick = {}) },
        ShowcaseItem("SwissColorField") { SwissColorField(color = Swiss.redBlock, title = "Swiss") },
        ShowcaseItem("SwissRule") { SwissRule(modifier = Modifier.fillMaxWidth()) },
    )

    ThemeStyle.Notion -> listOf(
        ShowcaseItem("NotionBlockRow") {
            NotionBlockRow(title = "文档块", supporting = "悬停出拖拽手柄 ⋮⋮")
        },
        ShowcaseItem("NotionGhostButton") { NotionGhostButton(text = "新建页面", onClick = {}) },
        ShowcaseItem("NotionInlineInput") {
            var text by remember { mutableStateOf("") }
            NotionInlineInput(
                value = text,
                onValueChange = { text = it },
                placeholder = "行内输入，聚焦只有一条底线",
            )
        },
        ShowcaseItem("NotionCallout") { NotionCallout(text = "提示块：淡底 + 4px 圆角，正文 14px/1.6。") },
    )

    ThemeStyle.EInk -> listOf(
        ShowcaseItem("EInkPanel") {
            EInkPanel { Text("纸面面板", style = MaterialTheme.typography.bodyMedium) }
        },
        ShowcaseItem("EInkReadingRow") {
            EInkReadingRow(
                index = "01",
                title = "样例数据",
                supporting = "示例值，非真机读数",
                value = "21.4",
                unit = "°C",
                state = "样例",
            )
        },
        ShowcaseItem("EInkPaperField") {
            var text by remember { mutableStateOf("") }
            EInkPaperField(
                value = text,
                onValueChange = { text = it },
                label = "搜索归档",
                placeholder = "输入关键词",
            )
        },
        ShowcaseItem("EInkInkAction") { EInkInkAction(text = "打开阅读列表", onClick = {}) },
        ShowcaseItem("EInkStatusMarker") {
            Column {
                EInkStatusMarker(state = "在线")
                EInkStatusMarker(state = "离线", filled = false)
            }
        },
        ShowcaseItem("Modifier.eInkGrain()") {
            EInkPanel(modifier = Modifier.eInkGrain()) {
                Text("装饰性网点（5dp 一格）", style = MaterialTheme.typography.bodySmall)
            }
        },
    )

    ThemeStyle.MacOS -> listOf(
        ShowcaseItem("MacOSToolbar") { MacOSToolbar(title = "待办") },
        ShowcaseItem("MacOSVibrancySidebar") {
            MacOSVibrancySidebar {
                Column {
                    MacOSListRow(title = "全部", trailing = "12")
                    MacOSListRow(title = "今天", trailing = "3")
                }
            }
        },
        ShowcaseItem("MacOSVibrancyPanel") {
            MacOSVibrancyPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    MacOSListRow(title = "侧边栏行 1", trailing = "12")
                    MacOSListRow(title = "侧边栏行 2", trailing = "3")
                }
            }
        },
        ShowcaseItem("MacOSListRow") { MacOSListRow(title = "全部", trailing = "12") },
        ShowcaseItem("MacOSSegmentedControl") {
            var selected by remember { mutableIntStateOf(0) }
            MacOSSegmentedControl(
                options = listOf("全部", "今天", "已完成"),
                selectedIndex = selected,
                onSelect = { selected = it },
            )
        },
        ShowcaseItem("MacOSSystemButton") { MacOSSystemButton(text = "新建待办", onClick = {}) },
    )

    // Mono 是默认风格，没有独立的一套组件 —— 上面 12 个分类就是 Mono 的全部。
    ThemeStyle.Mono -> listOf(
        ShowcaseItem("（无）") {
            Text(
                "Mono 是默认风格，用的就是前面那些通用组件，没有额外的一套。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    )
}
