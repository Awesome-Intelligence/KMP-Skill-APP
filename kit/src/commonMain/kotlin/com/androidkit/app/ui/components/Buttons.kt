package com.androidkit.app.ui.components

import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.M3Motion
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.stringResource
import com.androidkit.app.res.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.LocalThemeStyle
import com.androidkit.app.ui.theme.Swiss
import com.androidkit.app.ui.theme.ThemeStyle
import com.androidkit.app.ui.theme.themedControlShape
import com.androidkit.app.ui.theme.themedElevation
import com.androidkit.app.ui.theme.LocalReducedMotion
import com.androidkit.app.ui.theme.extendedColors
import com.androidkit.app.ui.theme.themedCircleShape
import com.androidkit.app.ui.theme.themedDisabledContentColor


/**
 * Primary Button - M3 实心按钮
 * 高度 48dp，圆角 12dp
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val swiss = LocalThemeStyle.current == ThemeStyle.Swiss
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = themedControlShape(MaterialTheme.shapes.medium),
        // M3 Button 的 hover 走 state layer（只叠一层半透明），不换底色 —— 不加位移
        colors = ButtonDefaults.buttonColors(disabledContentColor = themedDisabledContentColor()),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Secondary Button - M3 FilledTonal 按钮
 * 高度 48dp，圆角 12dp
 * 使用 FilledTonalButton 替代 OutlinedButton，使主要操作更突出
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    // 瑞士规范次级按钮：白底黑字黑边（不是色调色块）——「只用黑白原色」
    val swiss = LocalThemeStyle.current == ThemeStyle.Swiss
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = themedControlShape(MaterialTheme.shapes.medium),
        colors = ButtonDefaults.filledTonalButtonColors(
            disabledContentColor = themedDisabledContentColor(),
            containerColor = if (swiss) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (swiss) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        border = if (swiss) BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface) else null,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Small Secondary Button - M3 Outlined 文字按钮
 * 紧凑内边距，用于卡片内的操作按钮；按钮文字按 M3 规范统一用 labelLarge
 */
@Composable
fun SmallSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = themedControlShape(MaterialTheme.shapes.medium),
        colors = ButtonDefaults.outlinedButtonColors(disabledContentColor = themedDisabledContentColor()),
        contentPadding = ButtonDefaults.TextButtonContentPadding
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Filled Icon Button - M3 标准 IconButton
 * 40×40dp, 无背景色
 */
@Composable
fun FilledIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Scale Icon Button - 圆形图标按钮（无背景边框）
 * 40×40dp, 无背景色；按下反馈用 M3 state layer（onSurface 10%），不做缩放
 * @param isActive 激活态：primaryContainer 底 + primary 图标（预览模式、标签已选等）
 */
@Composable
fun ScaleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isActive: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // M3 交互态：按下是 state layer（onSurface 10% 圆角底），不做缩放
    // ponytail: 激活态用 onPrimaryContainer 做 state layer（压在 primaryContainer 底上）
    val stateLayer by animateColorAsState(
        targetValue = when {
            !enabled -> Color.Transparent
            isPressed && isActive -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.10f)
            isPressed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
            else -> Color.Transparent
        },
        animationSpec = M3Motion.defaultEffects(),
        label = "stateLayer"
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(themedCircleShape())
            .background(
                if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            )
            .background(stateLayer)
            .clickable(
                interactionSource = interactionSource,
                enabled = enabled,
                onClick = onClick
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                isActive -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/**
 * Small Scale Icon Button - 32dp 版本，用于工具栏
 * 按下反馈同 ScaleIconButton：M3 state layer，不做缩放
 */
@Composable
fun SmallScaleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // M3 交互态：按下是 state layer（onSurface 10% 圆角底），不做缩放
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
            .size(32.dp)
            .clip(themedCircleShape())
            .background(stateLayer)
            .clickable(
                interactionSource = interactionSource,
                enabled = enabled,
                onClick = onClick
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

/**
 * Circle Icon Button - 圆形图标按钮
 * 40×40dp, 无背景色
 */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Edit Button - M3 IconButton 风格编辑按钮
 * 40×40dp，图标使用 Outlined 版本
 */
@Composable
fun EditButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String = stringResource(Res.string.edit)
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        enabled = enabled
    ) {
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Add Button - M3 IconButton 风格添加按钮
 * 40×40dp，图标使用 Outlined 版本
 */
@Composable
fun AddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String = stringResource(Res.string.add_placeholder)
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        enabled = enabled
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Add FAB - Floating Action Button（Google 风格）
 * 56×56dp 圆形，elevation 3dp（扁平化优先，见 GOOGLE_STYLE FAB 规格）
 *
 * 颜色可覆盖：底部导航中间那个按钮要压住底栏上沿，用 primary/onPrimary 更压得住
 */
@Composable
fun AddFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(Res.string.add_placeholder),
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = themedElevation(3.dp),
            pressedElevation = themedElevation(6.dp),
            hoveredElevation = themedElevation(4.dp)
        ),
        // 瑞士风格无圆角 —— 只有这一颗按钮改成直角
        shape = if (LocalThemeStyle.current == ThemeStyle.Swiss) RoundedCornerShape(0.dp) else CircleShape
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Expand Button - 展开/收起按钮
 * 40×40dp，图标使用 Outlined 版本，根据 expanded 状态切换图标
 */
@Composable
fun ExpandButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String = stringResource(Res.string.expand)
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        enabled = enabled
    ) {
        Icon(
            imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Star Favorite Button - M3 收藏星标按钮
 * 40×40dp IconButton，图标在 Filled.Star / StarBorder 间切换。
 * 点亮收藏时星标先收缩再弹性回弹（M3 强调动效），取消收藏直接切图标不庆祝。
 * 减动效（LocalReducedMotion）时不缩放
 */
@Composable
fun StarFavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val reducedMotion = LocalReducedMotion.current
    // macOS 毛玻璃规范：不做缩放回弹 —— 只换图标
    // 瑞士风格同理：Rational Restraint —— 交互不许加 scale
    val macos = LocalThemeStyle.current == ThemeStyle.MacOS
    val swiss = LocalThemeStyle.current == ThemeStyle.Swiss
    // 这三种情况都不许加 scale
    val noScale = reducedMotion || macos || swiss
    // 令牌是 @Composable（要读 LocalReducedMotion），只能在组合期取，LaunchedEffect 里只是消费
    val reclaim = M3Motion.expressiveSpatial<Float>()
    LaunchedEffect(isFavorite) {
        // 快速连点时打断回弹，先归位再起下一拍
        if (isFavorite && !noScale) {
            scale.animateTo(0.6f, tween(M3Motion.SHORT1, easing = M3Motion.emphasizedAccelerate))
            scale.animateTo(1f, reclaim)
        } else {
            scale.snapTo(1f)
        }
    }
    IconButton(onClick = onToggle, modifier = modifier.size(40.dp)) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
            contentDescription = if (isFavorite) {
                stringResource(Res.string.unfavorite)
            } else {
                stringResource(Res.string.favorite)
            },
            modifier = Modifier
                .size(24.dp)
                .scale(scale.value),
            tint = if (isFavorite) {
                MaterialTheme.extendedColors.star
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/**
 * Delete Button - 危险操作删除按钮
 * 48dp 高，12dp 圆角，Error 色彩
 */
@Composable
fun DeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = stringResource(Res.string.delete),
    contentDescription: String = stringResource(Res.string.delete)
) {
    // 零色相主题（电子纸）里 error 和 primary 同为墨色 —— 墨底墨字的「删除」按钮
    // 和主按钮长得一模一样，等于没有危险标记。改成纸底 + 墨边 + 墨字：
    // 靠填充/描边区分，不靠色相（规范：状态由形状扛）。
    val eink = LocalThemeStyle.current == ThemeStyle.EInk
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = themedControlShape(MaterialTheme.shapes.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (eink) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.error,
            contentColor = if (eink) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onError,
            disabledContentColor = themedDisabledContentColor()
        ),
        border = if (eink) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

/**
 * Text Action Button - M3 Text Button
 * 用于表单等场景的次级操作（如"更多字段"展开/收起）
 */
@Composable
fun TextActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(disabledContentColor = themedDisabledContentColor())
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
internal fun PrimaryButtonPreview() {
    AppTheme {
        PrimaryButton(text = DemoData.Text.SAVE, onClick = {}) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun SecondaryButtonPreview() {
    AppTheme {
        SecondaryButton(text = DemoData.Text.CANCEL, onClick = {}) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun SmallSecondaryButtonPreview() {
    AppTheme {
        SmallSecondaryButton(text = DemoData.Text.VIEW_ALL, onClick = {}) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun FilledIconButtonPreview() {
    AppTheme {
        FilledIconButton(
            icon = Icons.Outlined.Edit,
            contentDescription = DemoData.Text.EDIT,
            onClick = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun ScaleIconButtonPreview() {
    AppTheme {
        ScaleIconButton(
            icon = Icons.Outlined.Add,
            contentDescription = DemoData.Text.ADD,
            onClick = {},
            isActive = true
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun SmallScaleIconButtonPreview() {
    AppTheme {
        SmallScaleIconButton(
            icon = Icons.Outlined.Edit,
            contentDescription = DemoData.Text.EDIT,
            onClick = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun CircleIconButtonPreview() {
    AppTheme {
        CircleIconButton(
            icon = Icons.Outlined.Delete,
            contentDescription = DemoData.Text.DELETE,
            onClick = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun EditButtonPreview() {
    AppTheme {
        EditButton(onClick = {}) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun AddButtonPreview() {
    AppTheme {
        AddButton(onClick = {}) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun AddFabPreview() {
    AppTheme {
        AddFab(onClick = {}) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun ExpandButtonPreview() {
    AppTheme {
        ExpandButton(expanded = false, onClick = {}) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun DeleteButtonPreview() {
    AppTheme {
        DeleteButton(onClick = {}) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun TextActionButtonPreview() {
    AppTheme {
        TextActionButton(text = DemoData.Text.MORE_FIELDS, onClick = {}) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
internal fun StarFavoriteButtonPreview() {
    AppTheme {
        var favorite by remember { mutableStateOf(true) }
        StarFavoriteButton(isFavorite = favorite, onToggle = { favorite = !favorite })
    }
}
