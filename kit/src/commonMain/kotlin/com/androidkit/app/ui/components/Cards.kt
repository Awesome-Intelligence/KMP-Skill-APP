package com.androidkit.app.ui.components

import com.androidkit.app.ui.theme.M3Motion
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.LocalThemeStyle
import com.androidkit.app.ui.theme.ThemeStyle
import com.androidkit.app.ui.theme.themedElevation

/**
 * App Card - M3 Card（无点击）
 * 规格：圆角 12dp，内边距 16dp（可通过 contentPadding 覆盖），surfaceContainerLow 背景，0 elevation（M3 用色阶分层，不用阴影）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = swissCardContainer()
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = border ?: macOSCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * App Card - M3 Card（有点击）
 * 使用 Card 自带 onClick，水波纹会自动裁剪到圆角内
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = swissCardContainer()
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = themedElevation(1.dp)
        ),
        border = macOSCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * CardTitle - 卡片标题：图标 + 文本
 * 详情页各 AppCard 顶部统一用它，需要尾部操作按钮时自行套 Row。
 */
@Composable
fun CardTitle(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // decorative，标题已承载语义
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/**
 * Small Card - M3 Card（无点击）
 * 规格：圆角 12dp，内边距 12dp，surfaceContainerLow 背景，0 elevation（M3 用色阶分层，不用阴影）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = swissCardContainer()
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = themedElevation(1.dp)
        ),
        border = macOSCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            content = content
        )
    }
}

/**
 * Small Card - M3 Card（有点击）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = swissCardContainer()
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = themedElevation(1.dp)
        ),
        border = macOSCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            content = content
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun AppCardPreview() {
    AppTheme {
        AppCard {
            Text(text = DemoData.Text.NORMAL_CARD_TEXT)
        } // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun AppCardClickablePreview() {
    AppTheme {
        AppCard(onClick = {}) {
            Text(text = DemoData.Text.CLICKABLE_CARD)
        } // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun CardTitlePreview() {
    AppTheme {
        CardTitle(icon = Icons.Outlined.Flag, title = DemoData.Text.CHECKIN_RECORDS) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun SmallCardPreview() {
    AppTheme {
        SmallCard {
            Text(text = DemoData.Text.SMALL_CARD_TEXT)
        } // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun SmallCardClickablePreview() {
    AppTheme {
        SmallCard(onClick = {}) {
            Text(text = DemoData.Text.CLICKABLE_SMALL_CARD)
        } // ponytail: demo data only
    }
}

/**
 * 卡片底色 —— 瑞士风格下卡片**不存在**：不铺底色、不加边框，只剩内容和栏线。
 *
 * 规范：「Do not wrap content in cards」「Divide with 1px or 2px black rules, not card frames」。
 * 别的主题照旧用 surfaceContainer 铺一层。
 */
@Composable
private fun swissCardContainer(): Color =
    if (LocalThemeStyle.current == ThemeStyle.Swiss) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

/**
 * macOS 毛玻璃规范：卡片是「深灰底 + 1px 白 8% 边」—— 没有描边就少了一层层次。
 * 其它风格没有默认描边（沿用 M3）。
 */
@Composable
private fun macOSCardBorder(): BorderStroke? =
    if (LocalThemeStyle.current == ThemeStyle.MacOS) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    } else {
        null
    }
