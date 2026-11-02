package com.androidkit.app.ui.preview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.ui.graphics.Color
import com.androidkit.app.ui.components.ActiveFilter
import com.androidkit.app.ui.components.BottomNavItem
import com.androidkit.app.ui.components.CalendarDay
import com.androidkit.app.ui.components.ContactItem
import com.androidkit.app.ui.components.DropdownOption
import com.androidkit.app.ui.components.FilterType
import com.androidkit.app.ui.components.GoalAIMenuItem
import com.androidkit.app.ui.components.NotificationItem
import com.androidkit.app.ui.components.SimpleMenuItem
import com.androidkit.app.ui.components.StatItemData
import com.androidkit.app.ui.templates.ListItem
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import com.androidkit.app.ui.components.plusDays
import com.androidkit.app.ui.components.minusDays

/**
 * 演示数据统一入口。
 *
 * 所有 `@Preview` 用的假数据都放在这里，Preview 函数里不再内联对象或文案。
 *
 * 约定：
 * - **数据对象**（目标、人脉、标签、记录…）放对应的分组里，用固定 id，方便关系引用。
 * - **文案**放 [Text]，**枚举值/颜色键/数字**放 [Value]。
 * - 新增预览时先来这里加 fixture，再在 Preview 里引用；不要在 Preview 里写字面量。
 */
object DemoData {

    // ─── 演示文案 ────────────────────────────────────────────────────────────
    object Text {
        // 通用动作
        const val SAVE = "保存"
        const val CANCEL = "取消"
        const val ADD = "添加"
        const val EDIT = "编辑"
        const val DELETE = "删除"
        const val UNDO = "撤销"
        const val VIEW_ALL = "查看全部"
        const val MORE_FIELDS = "更多字段"
        const val ADD_GOAL = "添加目标"
        const val NEW_GOAL = "新建目标"
        const val QUICK_ADD = "快速添加"

        // 通用名词
        const val ALL = "全部"
        const val GOAL = "目标"
        const val GOAL_LIST = "目标列表"
        const val GOAL_COUNT_3 = "3 个目标"
        const val TAG = "标签"
        const val CONTACT = "人脉"
        const val MINE = "我的"
        const val HOME = "首页"
        const val RECORD = "记录"
        const val RECORD_COUNT_3 = "3 条记录"
        const val PRIORITY = "优先级"
        const val IMPORTANCE = "重要度"
        const val HIGH_IMPORTANCE = "高重要性"
        const val SETTING_ITEM = "设置项"
        const val FEATURE_ENTRY = "功能入口"
        const val SUBTITLE_TEXT = "副标题说明文字"
        const val TITLE = "标题"
        const val NAME = "姓名"
        const val NOTES = "备注"
        const val PHONE = "电话"
        const val EMAIL = "邮箱"
        const val BIRTHDAY = "生日"
        const val BLOOD_TYPE = "血型"
        const val KEYWORD = "关键词"
        const val FILTER = "筛选"
        const val FILTER_CONTENT = "筛选内容"
        const val BASIC_INFO = "基本信息"
        const val BUILT_IN_TAGS = "内置标签"
        const val GENERAL = "通用"
        const val DARK_MODE = "深色模式"
        const val NOTIFICATIONS = "消息提醒"
        const val STORAGE_LOCATION = "数据存储位置"
        const val NO_MORE_PUSH = "关闭后不再收到推送"
        const val UPGRADE_QUOTA = "升级解锁更多额度"
        const val SAVED = "已保存"
        const val LOADING = "加载中…"
        const val ONE_LINE_INTRO = "一句话介绍"

        // 排序 / 选择器标题
        const val SORT_BY = "排序方式"
        const val BY_NAME = "按名称"
        const val BY_MODIFIED = "按修改时间"
        const val SELECT_TAG = "选择标签"
        const val SELECT_GOAL = "选择目标"
        const val SELECT_CONTACT = "选择人脉"
        const val RENAME_TAG = "重命名标签"
        const val NEW_NAME_INPUT = "输入新名称"
        const val DELETE_GOAL = "删除目标"
        const val DELETE_IRREVERSIBLE = "删除后无法恢复，确定要删除吗？"
        const val UPPER_GOAL = "上级目标"
        const val DUE_DATE = "截止日期"
        const val CHECKIN_RECORDS = "打卡记录"
        const val STREAK = "连续打卡"

        // 空状态 / 占位
        const val NO_GOALS_YET = "还没有目标"
        const val CREATE_FIRST_GOAL = "创建第一个目标，开始规划生活"
        const val PAGE_CONTENT = "页面内容"
        const val MAIN_CONTENT = "主内容区域"
        const val EXPANDED_CONTENT = "展开区域内容"
        const val NORMAL_CARD_TEXT = "普通卡片：圆角 12dp，内边距 16dp"
        const val SMALL_CARD_TEXT = "小卡片：内边距 12dp"
        const val CLICKABLE_CARD = "可点击卡片"
        const val CLICKABLE_SMALL_CARD = "可点击小卡片"
        const val INPUT_CONTENT = "输入内容"
        const val PLEASE_INPUT = "请输入"

        // 状态档位
        const val HIGH = "高"
        const val MEDIUM = "中"
        const val LOW = "低"
        const val IN_PROGRESS = "进行中"
        const val COMPLETED = "已完成"
        const val OVERDUE_2_DAYS = "逾期 2 天"

        // 标签 / 分类
        const val WORK = "工作"
        const val LIFE = "生活"
        const val STUDY = "学习"
        const val HEALTH = "健康"
        const val READING = "阅读"
        const val GROWTH = "成长"
        const val TECH = "技术"
        const val DESIGN = "设计"
        const val SPORT = "运动"
        const val FITNESS = "健身"
        const val TRAVEL = "旅行"
        const val FINANCE = "财务"
        const val IMPORTANT = "重要"
        const val URGENT = "紧急"
        const val SELF_IMPROVEMENT = "自我提升"
        const val READ_BOOK = "读书"
        const val FRIEND = "朋友"
        const val COLLEAGUE = "同事"
        const val CLIENT = "客户"
        const val FAMILY = "家人"
        const val MENTOR = "导师"
        const val PARTNER = "合作伙伴"
        const val UNIVERSITY_MATE = "大学同学"
        const val OTHER = "其他"
        const val KEY_CLIENT = "重要客户"
        const val SHANGHAI = "上海"
        const val HEALTH_STUDY_WORK = "健康 · 学习 · 工作"
        const val YEAR_GOAL_2026 = "2026 年度目标"
        const val THIS_YEAR_FOCUS = "今年主攻的方向"
        const val THIS_YEAR_FOCUS_DETAIL = "今年主攻的方向：读完 12 本书，跑 500 公里。"
        const val TWO_BOOKS_MONTHLY = "每月两本"
        const val HEALTH_TAG_DESC = "这个标签用于标记与健康相关的目标和记录"

        // 人名 / 单位 / 职位
        const val ZHANG_SAN = "张三"
        const val LI_SI = "李四"
        const val WANG_WU = "王五"
        const val ZHAO_LIU = "赵六"
        const val WANG_FANG = "王芳"
        const val ZHAO_QIANG = "赵强"
        const val LI_MING = "李明"
        const val XIAO_MING = "小明"
        const val PRODUCT_MANAGER = "产品经理"
        const val DESIGNER = "设计师"
        const val DESIGN_LEAD = "设计负责人"
        const val ENGINEER = "工程师"
        const val PHOTOGRAPHER = "摄影师"
        const val TECH_LEAD = "技术总监"
        const val FREELANCER = "自由职业"
        const val INDEPENDENT_DEVELOPER = "独立开发者"
        const val TECH_COMPANY = "某科技公司"
        const val MOX_TECH = "某某科技"
        const val DESIGN_COMPANY = "某设计公司"
        const val DESIGN_STUDIO = "设计工作室"
        const val BLUE_SEA_TECH = "蓝海科技"
        const val ADDRESS_ZHANGJIANG = "上海市浦东新区张江路 100 号"
        const val CONTACT_DETAIL_NOTES =
            "去年技术峰会上认识，聊过 AI Agent 的落地场景。周末常一起爬山。"

        // 目标
        const val QUARTERLY_REPORT = "完成季度报告"
        const val FITNESS_CHECKIN = "健身打卡"
        const val EXPENSE_CLAIM = "提交报销单"
        const val BOOK_CHECKUP = "预约体检"
        const val ANNUAL_READING = "年度阅读计划"
        const val ANNUAL_READING_PLAN = "完成年度阅读计划"
        const val BOOK_ZHONGSHINEI = "读完《置身事内》"
        const val BOOK_DEEP_WORK = "读完《深度工作》"
        const val ANNUAL_CHECKUP = "完成年度体检"
        const val LEARN_SWIMMING = "学会游泳"
        const val TECH_PLAN_2026 = "2026 年度技术规划"
        const val HIRING_PLAN = "团队招聘计划"
        const val ANNUAL_REVIEW = "年度复盘"
        const val ANNUAL_GROWTH = "年度成长"
        const val LEARN_KOTLIN = "学好 Kotlin"
        const val DAILY_RUN_3KM = "每天跑步 3 公里"
        const val RUN_3_TIMES_WEEK = "每周跑步三次"
        const val RUN_5KM = "完成 5 公里慢跑，状态不错。"
        const val EARLY_RISE = "早起"
        const val SHORT_TERM_GOAL = "短期目标"
        const val READ_30_MIN = "读书 30 分钟"
        const val READ_30_MIN_TASK = "完成 30 分钟阅读"
        const val FIVE_TIMES_WEEK_12_DAYS = "每周 5 次 · 已坚持 12 天"
        const val TWELVE_DAYS = "12 天"
        const val READ_24_BOOKS_DESC = "今年读完 24 本书，每读完一本写一篇短评。"
        const val READ_2_BOOKS_REVIEW = "每周读 2 本并写短评"
        const val READ_24_BY_YEAR_END = "年底前读完 24 本"
        const val ORGANIZE_BOOKLIST = "整理本年度书单"
        const val ORGANIZE_REQUIREMENTS = "整理需求文档"
        const val SPLIT_24_BOOKS = "把 24 本书拆成季度书单"
        const val READ_TWO_NIGHTS = "每周固定两晚读书时间"

        // 记录
        const val WEEKLY_REVIEW = "周度复盘"
        const val WEEKLY_KEY_RESULTS = "本周完成了三个关键结果，阅读时长 5 小时。"
        const val MORNING_DIARY = "晨间日记"
        const val MORNING_READING = "今天早起完成了半小时阅读。"
        const val PROJECT_MEETING = "项目周会"
        const val MILESTONE_SYNCED = "同步了里程碑进度，下周进入验收。"
        const val OCT_27 = "10月27日"
        const val OCT_26 = "10月26日"
        const val CALM = "平静"
        const val HAPPY = "开心"
        const val TIRED = "疲惫"
        const val FULFILLED = "充实"
        const val SUNNY = "晴"

        // 通知
        const val GOAL_REMINDER = "目标提醒"
        const val DAILY_READING_12_DAYS = "「每日阅读」已连续打卡 12 天"
        const val GOAL_DUE_SOON = "目标即将到期"
        const val QUARTERLY_DUE_2_DAYS = "「完成季度报告」还有 2 天到期"
        const val WEEKLY_REPORT_READY = "周报已生成"
        const val WEEKLY_SUMMARY_READY = "上周的 x记 总结已生成，点击查看"
        const val YESTERDAY = "昨天"
        const val TEN_MIN_AGO = "10 分钟前"

        // AI
        const val AI_INSIGHT = "AI 洞察"
        const val WEEKLY_COMPLETION_75 = "本周目标完成率 75%，比上周提升了 12%。"
        const val SPLIT_WEEKLY_REPORT = "把「写周报」拆成三个小步骤更容易坚持"
        const val DECOMPOSE_SUGGESTION = "建议拆解为：收集素材 → 列提纲 → 成文"
        const val AI_DECOMPOSE = "AI 拆解"
        const val AI_SUMMARY = "AI 总结"
        const val AI_SMART_DECOMPOSE = "AI 智能拆解"

        // Markdown / 富文本
        const val MARKDOWN_EDITOR = "# 今日记录\n\n完成了 **核心目标** 的第一步"
        const val MARKDOWN_PREVIEW = "## 小节\n\n- 列表项一\n- 列表项二 [[goals/demo-goal|示例目标]]"
        const val BOLD = "粗体"
        const val ITALIC = "斜体"

        // 记录编辑器 / 时段总结
        const val RECORD_EDITOR_CONTENT =
            "# 今日记录\n\n完成了 **核心目标** 的第一步，和设计对齐了标签页交互。"
        const val WEEKLY_SUMMARY =
            "本周共记录 5 条，主要围绕 **项目进度** 与 **健康** 展开。\n\n- 完成标签页交互对齐\n- 跑步 3 次，共 12 公里"
        const val DATE_2026_10_27 = "2026年10月27日"

        // 里程碑
        const val TECH_CONFERENCE = "年度技术大会"
        const val PROJECT_LAUNCH = "项目上线"
        const val BOOK_MEETING_ROOM = "记得提前订会议室"
        const val SAME_TEAM_COLLEAGUE = "同组同事"

        // 灵感
        const val IDEA_CONTENT = "周末去图书馆读《置身事内》，顺便整理一下年度目标"

        // 活动记录
        const val PROGRESS_UPDATED = "更新了目标进度"

        // 计费
        const val STANDARD_MONTHLY = "标准版月付"
        const val STANDARD_MONTHLY_DESC = "标准版月度订阅"
    }

    // ─── 演示用配色 ──────────────────────────────────────────────────────────
    object Palette {
        val rose = Color(0xFFB87070)
        val clay = Color(0xFFAD7A5A)
        val slate = Color(0xFF6B7A8A)
        val tagGreen = Color(0xFF5A8A6E)
        val primary = Color(0xFF1A73E8)
        val success = Color(0xFF188038)
        val danger = Color(0xFFD93025)
    }

    // ─── 演示用原始值（枚举值 / 颜色键 / 数字 / 日期） ────────────────────────
    object Value {
        // 标签颜色键
        const val COLOR_ROSE = "rose"
        const val COLOR_SAGE = "sage"
        const val COLOR_MAUVE = "mauve"
        const val COLOR_CLAY = "clay"
        const val COLOR_SLATE = "slate"
        const val COLOR_NEUTRAL = "neutral"
        const val COLOR_PRIMARY = "primary"
        const val COLOR_SECONDARY = "secondary"
        const val COLOR_ERROR = "error"
        const val COLOR_TERTIARY = "tertiary"
        const val COLOR_AMBER = "amber"

        // 重要度档位
        const val LEVEL_LOW = "low"
        const val LEVEL_MEDIUM = "medium"
        const val LEVEL_HIGH = "high"

        // 人脉类型 id
        const val TYPE_FRIEND = "friend"
        const val TYPE_COLLEAGUE = "colleague"
        const val TYPE_CLIENT = "client"
        const val TYPE_FAMILY = "family"
        const val TYPE_MENTOR = "mentor"
        const val TYPE_OTHER = "other"

        // 逗号分隔的标签串
        const val TAGS_TECH_FRIEND = "技术,朋友"
        const val TAGS_READING_GROWTH = "阅读,成长"
        const val TAGS_KEY_CLIENT = "重要客户,技术,上海"

        // 数字 / 百分比
        const val COUNT_12 = "12"
        const val COUNT_48 = "48"
        const val PERCENT_60 = "60%"
        const val PROGRESS_30 = "30%"
        const val PROGRESS_60 = "60%"
        const val PRICE_STANDARD = "¥12"

        // 整数计数（预览里直接当数值用，避免 .toInt()）
        const val INT_ONE = 1
        const val INT_THREE = 3
        const val INT_FOUR = 4
        const val INT_EIGHT = 8
        const val INT_TEN = 10
        const val INT_TWELVE = 12
        const val INT_FIFTEEN = 15
        const val INT_THIRTY = 30
        const val PROGRESS_075 = 0.75f

        // 相对今天的演示日期（预览每天自动对齐）
        val DATE_TODAY = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val DATE_TODAY_PLUS_2 = Clock.System.todayIn(TimeZone.currentSystemDefault()).plusDays(2)
        val DATE_TODAY_PLUS_5 = Clock.System.todayIn(TimeZone.currentSystemDefault()).plusDays(5)
        val DATE_TODAY_MINUS_1 = Clock.System.todayIn(TimeZone.currentSystemDefault()).minusDays(1)
        val DATE_TODAY_MINUS_2 = Clock.System.todayIn(TimeZone.currentSystemDefault()).minusDays(2)
        val DATE_TODAY_MINUS_4 = Clock.System.todayIn(TimeZone.currentSystemDefault()).minusDays(4)

        // 日期 / 时间
        const val TIME_1024 = "10:24"
        const val TIME_0830 = "08:30"
        const val DATE_2026_12_31 = "2026-12-31"
        const val DATE_1990_03_15 = "1990-03-15"
        const val BIRTHDAY_MILLIS = "637459200000"
        const val DATE_RANGE_START = "2026-10-01"
        const val DATE_RANGE_END = "2026-10-27"
        const val STORAGE_PATH = "/storage/emulated/0/Android-Kit"

        // 其他
        const val PHONE_MOBILE = "13800000000"
        const val PHONE_LANDLINE = "13800138000"
        const val COMPANY_ACME = "Acme"
        const val MODEL_GLM = "GLM"
        const val MBTI = "MBTI"
        const val MBTI_VALUE = "INTJ"
        const val BLOOD_A = "A"
        const val BLOOD_B = "B"
        const val BLOOD_O = "O"
        const val BLOOD_AB = "AB"
        const val ACTIVITY_PROGRESS = "progress"
        const val PRODUCT_ID_STANDARD_MONTHLY = "std-monthly"
        const val OFFER_ID_STANDARD_MONTHLY = "standard_monthly"
    }

    // ─── 目标 ────────────────────────────────────────────────────────────────

    // ─── 人脉 ────────────────────────────────────────────────────────────────

    // ─── 标签 ────────────────────────────────────────────────────────────────

    // ─── 子任务 ──────────────────────────────────────────────────────────────

    // ─── 首页 ────────────────────────────────────────────────────────────────

    // ─── 通用组件 ────────────────────────────────────────────────────────────
    object Components {
        val avatarZhang = ContactItem(id = "c1", name = Text.ZHANG_SAN)
        val avatarLi = ContactItem(id = "c2", name = Text.LI_SI)
        val avatarWang = ContactItem(id = "c3", name = Text.WANG_WU)
        val avatarZhao = ContactItem(id = "c4", name = Text.ZHAO_LIU)
        val avatarStackFour = listOf(avatarZhang, avatarLi, avatarWang, avatarZhao)
        val avatarStackThree = listOf(avatarZhang, avatarLi, avatarWang)

        val notificationGoalReminder = NotificationItem(
            id = "1",
            title = Text.GOAL_REMINDER,
            body = Text.DAILY_READING_12_DAYS,
            time = Value.TIME_1024,
            isRead = false
        )
        val notificationWeeklyReport = NotificationItem(
            id = "2",
            title = Text.WEEKLY_REPORT_READY,
            body = Text.WEEKLY_SUMMARY_READY,
            time = Text.YESTERDAY
        )
        val notificationGoalDue = NotificationItem(
            id = "n3",
            title = Text.GOAL_DUE_SOON,
            body = Text.QUARTERLY_DUE_2_DAYS,
            time = Text.TEN_MIN_AGO,
            isRead = false,
            deletable = false
        )
        val notificationsTwo = listOf(notificationGoalReminder, notificationWeeklyReport)
        val notificationsThree = listOf(
            notificationGoalReminder,
            notificationWeeklyReport,
            notificationGoalDue
        )

        val navHome = BottomNavItem(Text.HOME, Icons.Outlined.Home, Icons.Filled.Home)
        val navGoals = BottomNavItem(Text.GOAL, Icons.Outlined.Flag, Icons.Filled.Flag, badgeCount = 3)
        val navQuickAdd = BottomNavItem(Text.QUICK_ADD, Icons.Outlined.Bolt, alwaysFilled = true)
        val navContacts = BottomNavItem(Text.CONTACT, Icons.Outlined.People, Icons.Filled.People)
        val navMine = BottomNavItem(Text.MINE, Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)
        val bottomNavItems = listOf(navHome, navGoals, navQuickAdd, navContacts, navMine)

        val menuEdit = SimpleMenuItem(title = Text.EDIT, icon = Icons.Default.Edit)
        val menuDelete = SimpleMenuItem(title = Text.DELETE, icon = Icons.Default.Delete)
        val menuItems = listOf(menuEdit, menuDelete)

        val filterHighImportance = ActiveFilter(
            id = "1",
            type = FilterType.IMPORTANCE,
            label = Text.HIGH_IMPORTANCE
        )
        val filterHealth = ActiveFilter(
            id = "2",
            type = FilterType.TAG,
            label = Text.HEALTH,
            color = Palette.tagGreen
        )
        val filterGoal = ActiveFilter(id = "3", type = FilterType.TYPE, label = Text.GOAL)
        val activeFilters = listOf(filterHighImportance, filterHealth, filterGoal)

        val statInProgress = StatItemData(
            title = Text.IN_PROGRESS,
            value = Value.COUNT_12,
            icon = Icons.Filled.Flag,
            color = Palette.primary
        )
        val statCompleted = StatItemData(
            title = Text.COMPLETED,
            value = Value.COUNT_48,
            icon = Icons.Filled.CheckCircle,
            color = Palette.success
        )
        val stats = listOf(statInProgress, statCompleted)

        val optionLow = DropdownOption(label = Text.LOW, value = Value.LEVEL_LOW)
        val optionMedium = DropdownOption(label = Text.MEDIUM, value = Value.LEVEL_MEDIUM)
        val optionHigh = DropdownOption(label = Text.HIGH, value = Value.LEVEL_HIGH)
        val dropdownOptions = listOf(optionLow, optionMedium, optionHigh)

        val aiMenuDecompose = GoalAIMenuItem(Text.AI_DECOMPOSE, Icons.Default.AutoAwesome) {}
        val aiMenuSummarize = GoalAIMenuItem(Text.AI_SUMMARY, Icons.Default.Psychology) {}


        val listItemZhang = ListItem(
            id = "1",
            name = Text.ZHANG_SAN,
            category = Text.COLLEAGUE,
            isEmergency = true
        )
        val listItemLi = ListItem(
            id = "2",
            name = Text.LI_SI,
            category = Text.UNIVERSITY_MATE,
            isEmergency = false
        )
        val listItemWang = ListItem(
            id = "3",
            name = Text.WANG_WU,
            category = Text.PARTNER,
            isEmergency = true
        )

        /** 日历格子 —— 日期跟着预览当天走 */
        fun calendarDay(
            date: LocalDate,
            isToday: Boolean = false,
            isSelected: Boolean = false,
            isWeekSelected: Boolean = false
        ) = CalendarDay(
            date = date,
            dayNumber = date.dayOfMonth.toString(),
            isToday = isToday,
            isSelected = isSelected,
            isWeekSelected = isWeekSelected
        )
    }

    // ─── 页面状态 ────────────────────────────────────────────────────────────

    // ─── AI ──────────────────────────────────────────────────────────────────

    // ─── 活动记录 / 灵感 ────────────────────────────────────────────────────


    // ─── 我的资料 ────────────────────────────────────────────────────────────

    // ─── 计费 ────────────────────────────────────────────────────────────────

    // ─── 页面状态（kit 精简版） ────────────────────────────────────────────
    object Home {
        object favoriteQuarterly {
            const val progress = 0.6f
        }
    }

    object Tags {
        object work { const val name = Text.WORK }
        object life { const val name = Text.LIFE }
        object study { const val name = Text.STUDY }
    }

    object States {
        object mine {
            const val goalCount = 3
        }
    }
}
