# Android-Kit - Android 公共组件规范

> 版本：1.1.0
> 日期：2026-10-23
> 适用范围：Android 版（Kotlin + Jetpack Compose），单模块 `:app`

---

## 1. 设计令牌 (Design Tokens)

### 1.1 圆角半径

| Token | 值 | 用途 |
|-------|-----|------|
| `corner_radius` | 16dp | 卡片、容器 |
| `corner_radius_medium` | 12dp | 输入框、中按钮 |
| `corner_radius_small` | 10dp | 小元素 |
| `corner_radius_tertiary` | 8dp | 标签、Chip |
| `corner_radius_badge` | 4dp | 徽章、小指示器 |
| `corner_radius_editor` | 6dp | 文本输入区 |
| `corner_radius_large` | 20dp | 大卡片、浮层 |

> Token 名对应 `res/values/dimens.xml` 里的 `dimen` 资源名。

### 1.2 间距 (4pt Grid)

| Token | 值 | 用途 |
|-------|-----|------|
| `spacing1` | 4dp | 标签内间距 |
| `spacing2` | 8dp | 小元素间距 |
| `spacing3` | 12dp | 卡片内元素间距 |
| `spacing4` | 16dp | 页面边距、卡片间距 |
| `spacing5` | 20dp | 大区块间距 |
| `spacing6` | 24dp | 区块间间距 |
| `spacing8` | 32dp | 页面顶部/底部间距 |

### 1.3 阴影

| Token | 用途 |
|-------|------|
| `card_elevation` | 卡片阴影 |
| `elevated_elevation` | 悬浮元素 |
| `dragging_elevation` | 拖拽时 |

```xml
<!-- res/values/elevation.xml -->
<dimen name="card_elevation">2dp</dimen>
<dimen name="elevated_elevation">6dp</dimen>
<dimen name="dragging_elevation">8dp</dimen>
```

### 1.4 字体

使用 Material 3 Typography（`ui/theme/Type.kt`）：

| Style | 字号 / 行高 | 用途 |
|-------|-------------|------|
| `displayLarge` | 57 / 64 sp | 大标题 |
| `headlineLarge` | 32 / 40 sp | 页面标题 |
| `headlineMedium` | 28 / 36 sp | 区块标题 |
| `headlineSmall` | 24 / 32 sp | 次级标题 |
| `titleLarge` | 22 / 28 sp | 卡片标题 |
| `titleMedium` | 16 / 24 sp | 列表项标题 |
| `bodyLarge` | 16 / 24 sp | 正文 |
| `bodyMedium` | 14 / 20 sp | 次级正文 |
| `bodySmall` | 12 / 16 sp | 辅助说明 |
| `labelLarge` | 14 / 20 sp | 按钮文字 |
| `labelMedium` | 12 / 16 sp | 标签 |
| `labelSmall` | 11 / 16 sp | 徽章、计数 |

---

## 2. 颜色系统

### 2.1 语义颜色

| 用途 | 浅色模式 | 深色模式 | 来源 |
|------|----------|----------|------|
| Primary | #1D1915 | #E6E2DE | `Mono.primaryLight/Dark` |
| Success | #2D6A4F | #6BCB96 | `ExtendedColors.success` |
| Warning | #B75D00 | #FFB95C | `ExtendedColors.warning` |
| Error | #BA1A1A | #FFB4AB | `Mono.errorLight/Dark` |

> Primary 走单色（近黑 / 近白）主题；Success / Warning 是 Material 3 之外扩展的语义色，
> 通过 `MaterialTheme.extendedColors` 读取。

### 2.2 文本颜色

| 用途 | 浅色模式 | 深色模式 | 来源 |
|------|----------|----------|------|
| 主文本 `onSurface` | #1D1915 | #E7E3DF | `Mono.onSurfaceLight/Dark` |
| 次文本 `onSurfaceVariant` | #4C463F | #C8C3BE | `Mono.onSurfaceVariantLight/Dark` |
| 三级文本 `outline` | #7C756E | #918B84 | `Mono.outlineLight/Dark` |

### 2.3 背景颜色

| 用途 | 浅色模式 | 深色模式 | 来源 |
|------|----------|----------|------|
| 背景 `background` | #FDFAF6 | #15110D | `Mono.backgroundLight/Dark` |
| 次级背景 `surfaceVariant` | #E9E5E1 | #2F2A25 | `Mono.surfaceVariantLight/Dark` |
| 三级背景 `surfaceContainer` | #F1EDEA | #221D19 | `Mono.surfaceContainerLight/Dark` |

### 2.4 填充颜色

| 用途 | 浅色模式 | 深色模式 | 来源 |
|------|----------|----------|------|
| Fill | `outline` @ 20% | `outline` @ 36% | `Mono.outlineLight/Dark` |
| 次级填充 | `outline` @ 12% | `outline` @ 24% | `Mono.outlineLight/Dark` |

---

## 3. 按钮组件

### 3.1 主按钮 (PrimaryButton)

```
┌──────────────────────────┐
│        按钮文字           │
└──────────────────────────┘
```

**规格：**
- 高度：48dp
- 圆角：12dp
- 背景：Primary
- 文字：OnPrimary，14sp Medium
- 内边距：水平 24dp

**状态：**
- Default：Primary 背景
- Pressed：Primary @ 80% 亮度
- Disabled：Primary @ 38% 亮度

```kotlin
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(48.dp),
        shape = RoundedCorner(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
        )
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
```

### 3.2 次要按钮 (SecondaryButton)

```
┌──────────────────────────┐
│        按钮文字           │
└──────────────────────────┘
```

**规格：**
- 高度：48dp
- 圆角：12dp
- 背景：Transparent
- 边框：1dp，Primary
- 文字：Primary，14sp Medium

```kotlin
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(48.dp),
        shape = RoundedCorner(12.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
```

### 3.3 图标按钮 (IconButton)

```
┌────┐
│ +  │
└────┘
```

**规格：**
- 尺寸：48dp × 48dp
- 圆角：12dp
- 图标：24dp
- TouchTarget：48dp × 48dp

```kotlin
@Composable
fun AppIconButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    contentDescription: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        icon()
    }
}
```

### 3.4 图标按钮 (ScaleIconButton)

圆形图标按钮，**按下反馈用 M3 state layer**（onSurface 10% 圆角底），
不做缩放 —— M3 没有 "press scale" 这一条，按下反馈走 state layer。

```kotlin
@Composable
fun ScaleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // effects spring：无过冲，只改颜色
    val stateLayer by animateColorAsState(
        targetValue = if (isPressed && enabled) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        } else {
            Color.Transparent
        },
        animationSpec = M3Motion.defaultEffects(),
        label = "stateLayer"
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(stateLayer)
            .clickable(
                interactionSource = interactionSource,
                enabled = enabled,
                onClick = onClick
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
    }
}
```

---

## 4. 卡片组件

### 4.1 标准卡片 (Card)

```
┌─────────────────────────────────────┐
│                                     │
│           卡片内容                  │
│                                     │
└─────────────────────────────────────┘
```

**规格：**
- 圆角：16dp
- 内边距：16dp
- 背景：Surface
- 阴影：elevation 2dp

```kotlin
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCorner(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
```

### 4.2 小卡片 (SmallCard)

```kotlin
@Composable
fun SmallCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCorner(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            content = content
        )
    }
}
```

### 4.3 记录卡片 (RecordCard)

```kotlin
@Composable
fun RecordCard(
    record: Record,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCorner(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 顶部信息栏
            Row {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            
            // 标题
            Text(
                text = recordTitle,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2
            )
            
            // 内容预览
            if (contentPreview.isNotEmpty()) {
                Text(
                    text = contentPreview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
            
            // 心情/天气
            if (record.mood != null || record.weather != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    record.mood?.let { Text(it) }
                    record.weather?.let { Text(it) }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
```

---

## 5. 筛选/标签组件

### 5.1 筛选芯片 (FilterChip)

```
┌─────────────┐
│  标签文字    │
└─────────────┘
```

**规格：**
- 圆角：8dp
- 内边距：水平 12dp，垂直 6dp
- 文字：14sp
- 选中态：Primary @ 15% 背景，Primary 文字
- 未选中态：SurfaceVariant 背景，OnSurfaceVariant 文字

```kotlin
@Composable
fun FilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            selectedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}
```

### 5.2 标签 (Tag)

```kotlin
@Composable
fun Tag(
    text: String,
    color: Color,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCorner(8.dp),
        color = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
            if (onRemove != null) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "移除",
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
```

---

## 6. 空状态组件

### 6.1 空状态插图 (IllustratedEmptyState)

```kotlin
@Composable
fun IllustratedEmptyState(
    icon: String,           // Material Icon name
    title: String,
    subtitle: String? = null,
    actionTitle: String? = null,
    onAction: (() -> Unit)? = null,
    tintColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 图标容器
        Box(
            modifier = Modifier
                .size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = tintColor.copy(alpha = if (isSystemInDarkTheme()) 0.15f else 0.08f)
            ) {}
            
            Icon(
                imageVector = when (icon) {
                    "person.crop.circle.badge.plus" -> Icons.Default.PersonAdd
                    "target" -> Icons.Default.Flag
                    "square.and.pencil" -> Icons.Default.Edit
                    "magnifyingglass" -> Icons.Default.Search
                    else -> Icons.Default.Info
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = tintColor.copy(alpha = if (isSystemInDarkTheme()) 0.85f else 0.7f)
            )
        }
        
        // 标题
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        
        // 副标题
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
        
        // 操作按钮
        if (actionTitle != null && onAction != null) {
            Button(
                onClick = onAction,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(actionTitle)
            }
        }
    }
}

// 预设空状态
object EmptyStates {
    @Composable
    fun Contacts(onAddContact: () -> Unit) = IllustratedEmptyState(
        icon = "person.crop.circle.badge.plus",
        title = "empty_no_contacts".stringResource(),
        subtitle = "add_first_contact_subtitle".stringResource(),
        actionTitle = "add_first_contact".stringResource(),
        onAction = onAddContact,
        tintColor = Color.Blue
    )
    
    @Composable
    fun Goals(onAddGoal: () -> Unit) = IllustratedEmptyState(
        icon = "target",
        title = "empty_no_goals".stringResource(),
        subtitle = "add_first_goal_subtitle".stringResource(),
        actionTitle = "add_first_goal".stringResource(),
        onAction = onAddGoal,
        tintColor = Color.Green
    )
    
    @Composable
    fun Records(onCreateRecord: () -> Unit) = IllustratedEmptyState(
        icon = "square.and.pencil",
        title = "no_records_recent".stringResource(),
        subtitle = "start_first_record".stringResource(),
        actionTitle = "create_record".stringResource(),
        onAction = onCreateRecord,
        tintColor = Color(0xFFFF9500)
    )
}
```

---

## 7. 反馈组件

### 7.1 Toast/Snackbar

```kotlin
@Composable
fun AppSnackbar(
    message: String,
    isSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    Snackbar(
        modifier = modifier.padding(horizontal = 16.dp),
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isSuccess) Color(0xFF34C759) else Color(0xFFFF3B30)
            )
            Text(message)
        }
    }
}

// 使用方式
val snackbarHostState = remember { SnackbarHostState() }

SnackbarHost(hostState = snackbarHostState) { data ->
    AppSnackbar(
        message = data.visuals.message,
        isSuccess = data.visuals.actionLabel == "success"
    )
}

// 显示
scaffoldScope.launch {
    snackbarHostState.showSnackbar(
        message = "操作成功",
        actionLabel = "success"
    )
}
```

### 7.2 加载指示器

```kotlin
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier.size(48.dp),
        strokeWidth = 3.dp
    )
}

@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}
```

---

## 8. 工具栏组件

### 8.1 悬浮工具栏 (FloatingToolbar)

```kotlin
@Composable
fun FloatingToolbar(
    text: String,
    onTextChange: (String) -> Unit,
    images: List<ByteArray>,
    onImagesChange: (List<ByteArray>) -> Unit,
    selectedMood: String?,
    onMoodChange: (String?) -> Unit,
    showMoodSelector: Boolean,
    goals: List<Goal>,
    contacts: List<Contact>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        shape = RoundedCorner(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 8.dp
    ) {
        Column {
            // 主要工具栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 添加图片
                ToolbarIconButton(
                    icon = Icons.Default.Photo,
                    contentDescription = "添加图片",
                    onClick = { /* 打开图片选择器 */ }
                )
                
                // 心情选择
                if (showMoodSelector) {
                    MoodSelector(
                        selectedMood = selectedMood,
                        onMoodSelected = onMoodChange
                    )
                }
                
                // 目标引用
                GoalReferenceButton(
                    goals = goals,
                    onGoalSelected = { goal -> 
                        onTextChange(text + "#${goal.name} ")
                    }
                )
                
                // 人脉引用
                ContactReferenceButton(
                    contacts = contacts,
                    onContactSelected = { contact ->
                        onTextChange(text + "@${contact.name} ")
                    }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 字数统计
                Text(
                    text = "$text",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 图片缩略图（如果有）
            if (images.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    images.forEachIndexed { index, imageData ->
                        ImageThumbnail(
                            imageData = imageData,
                            onRemove = {
                                onImagesChange(images.toMutableList().apply { removeAt(index) })
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolbarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(32.dp)
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
```

---

## 9. 统计组件

### 9.1 统计项 (StatItem)

```kotlin
@Composable
fun StatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatsCard(
    stats: List<StatItemData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCorner(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            stats.forEach { stat ->
                StatItem(
                    title = stat.title,
                    value = stat.value,
                    icon = stat.icon,
                    color = stat.color,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

data class StatItemData(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)
```

---

## 10. 菜单组件

### 10.1 溢出菜单按钮 (MenuButton)

```kotlin
@Composable
fun <T> MenuButton(
    items: List<MenuItem<T>>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true }
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "更多选项",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.title) },
                    onClick = {
                        onItemSelected(item.data)
                        expanded = false
                    },
                    leadingIcon = if (item.icon != null) {
                        { Icon(item.icon, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}

data class MenuItem<T>(
    val title: String,
    val icon: ImageVector? = null,
    val data: T
)
```

---

## 11. 动画规范

动效**全部**来自 `kit/src/commonMain/kotlin/com/androidkit/app/ui/theme/Motion.kt` 的 `M3Motion`。
页面/组件里**不许手写** `tween` / `spring` 的时长与 easing 字面量 —— 需要新节奏就先加 token。

选型表（什么场景用哪个预设）在 `rules/GOOGLE_STYLE.md` §动效，**这里不重复**：两处维护必然漂移，
（这条规矩本身就是因为本文件曾长期写着一段代码里并不存在的 `AppAnimations` 才立的）。

### 11.1 Duration（M3 标准值）

| Token | 值 | 用途 |
|---|---|---|
| `SHORT1` | 50ms | 选中 / 取消选中 |
| `SHORT2` | 100ms | 简单状态切换 |
| `SHORT3` | 150ms | 小组件出现 |
| `SHORT4` | 200ms | 标准交互 |
| `MEDIUM1` | 250ms | 面板展开 |
| `MEDIUM2` | 300ms | 标准转场 |
| `MEDIUM3` | 350ms | 复杂转场 |
| `MEDIUM4` | 400ms | 全屏转场（启动页） |
| `LONG1` | 450ms | 强调动画 |
| `LONG2` | 500ms | 复杂强调 |
| `EXIT_FADE` | 90ms | 转场退场淡出（必须比位移快） |
| `FADE_THROUGH_IN` | 210ms | Fade Through 进场（叠 90ms 退场 = 300ms） |
| `FADE_THROUGH_INITIAL_SCALE` | 0.92 | Fade Through 进场初始缩放 |
| `SHARED_AXIS_OFFSET` | 30dp | Shared Axis X 位移量（不是整屏宽度） |
| `FLOAT_INITIAL_SCALE` | 0.8 | 浮层从锚点长出的初始缩放 |

### 11.2 Easing

| Token | 曲线 | 用途 |
|---|---|---|
| `emphasized` | cubic-bezier(0.2, 0, 0, 1) | 强调 |
| `emphasizedDecelerate` | cubic-bezier(0.05, 0.7, 0.1, 1) | 进场 |
| `emphasizedAccelerate` | cubic-bezier(0.3, 0, 0.8, 0.15) | 退场 |

### 11.3 Spring

| Token | damping / stiffness | 用途 |
|---|---|---|
| `fastSpatial()` | 0.9 / 1400 | 跟手位移、尺寸 |
| `defaultSpatial()` | 0.9 / 700 | 常态位移、尺寸 |
| `expressiveSpatial()` | 0.6 / 800 | 庆祝 / 强调回弹（唯一允许过冲的一档，别用于常态位移） |
| `fastEffects()` | 1.0 / 3800 | 颜色、透明度（不过冲） |
| `defaultEffects()` | 1.0 / 1600 | 同上，常态 |
| `itemPlacement()` | 1.0 / 400 | 列表条目换位（不过冲，项不会互相压过） |
| `pagerScroll()` | tween MEDIUM2 + 强调减速 | Pager 程序化翻页（只用于相邻页） |

### 11.4 页面级转场

组件内部用 `M3Motion`，**整页之间**用 `kit/src/commonMain/kotlin/com/androidkit/app/ui/navigation/NavTransitions.kt`：

| 场景 | 用什么 |
|---|---|
| 同层级、无空间关系（设置页、一级页面） | `M3Motion.fadeThrough()`（`AnimatedContent` 里）或 `NavTransitions.fadeThroughIn/Out()` |
| 层级推进 / 返回（列表 → 详情） | `NavTransitions.sharedAxisIn/Out/PopIn/PopOut(offsetPx)`，`offsetPx` 取 `sharedAxisOffsetPx()` |

### 11.5 减动效

系统「移除动画」（开发者选项 → 动画时长缩放 = 0）命中时，**所有** spec 退化为 `snap()`、
所有转场退化为 `None`：

- `AppTheme(reducedMotion = …)` 注入 `LocalReducedMotion`，`M3Motion` 内部自动判断；
- `NavTransitions` 的函数不是 `@Composable`（navigation 的转场 lambda 读不到 CompositionLocal），
  减动效由调用侧显式传 `reducedMotion`。


---

## 12. 主题集成

### 12.1 Material 3 主题

```kotlin
// Color.kt
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007AFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF007AFF).copy(alpha = 0.15f),
    onPrimaryContainer = Color(0xFF007AFF),
    secondary = Color(0xFF5856D6),
    onSecondary = Color.White,
    error = Color(0xFFFF3B30),
    onError = Color.White,
    background = Color(0xFFF2F2F7),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF3C3C43)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0A84FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0A84FF).copy(alpha = 0.15f),
    onPrimaryContainer = Color(0xFF0A84FF),
    secondary = Color(0xFF5E5CE6),
    onSecondary = Color.White,
    error = Color(0xFFFF453A),
    onError = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF1C1C1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFEBEBF5)
)

// Theme.kt
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
```

---

## 附录：组件索引

所有视觉组件都封装在 `ui/components/` 下，页面不直接写原始 Material 组件。

| 组件 | 文件 |
|------|------|
| `FilterChip` | `ui/components/Chips.kt` |
| `ScaleIconButton` | `ui/components/Buttons.kt` |
| `RecordCard` | `ui/components/Cards.kt` |
| `IllustratedEmptyState` | `ui/components/EmptyState.kt` |
| `AppSnackbar` | `ui/components/Feedback.kt` |
| `StatItem` | `ui/components/Stats.kt` |
| `PrimaryButton` / `SecondaryButton` / `EditButton` / `DeleteButton` / `TextActionButton` | `ui/components/Buttons.kt` |
| `AppCard` / `SmallCard` | `ui/components/Cards.kt` |
| `SimpleTextField` | `ui/components/TextFields.kt` |
| `ConfirmDialog` / `DeleteConfirmDialog` / `InputDialog` / `SelectDialog` | `ui/components/Dialogs.kt` |
| `SelectDropdown` | `ui/components/Dropdowns.kt` |
| `AddFab` | `ui/components/Buttons.kt` |
| `Header` | `ui/components/Header.kt` |
| `LinearProgressBar` / `CircularProgressIndicatorWithText` | `ui/components/Progress.kt` |
| 设计令牌（尺寸） | `res/values/dimens.xml` |
| 颜色 / 排版 / 主题 | `ui/theme/Color.kt`、`ColorMono.kt`、`Type.kt`、`Theme.kt` |
