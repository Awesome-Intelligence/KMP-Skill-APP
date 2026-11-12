package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.androidkit.app.res.*
import com.androidkit.app.ui.components.PrimaryButton
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme
import com.androidkit.app.ui.theme.LocalIsDarkTheme
import com.androidkit.app.ui.theme.themedCircleShape

import com.androidkit.app.ui.theme.themedTextAlign
import com.androidkit.app.ui.theme.themedHorizontalAlignment


/**
 * Illustrated Empty State
 * 图标容器：100dp 圆形背景 + 40dp 图标
 * 标题：titleLarge, SemiBold
 * 副标题：bodyMedium, OnSurfaceVariant
 */
@Composable
fun IllustratedEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    actionTitle: String? = null,
    onAction: (() -> Unit)? = null,
    tintColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = themedHorizontalAlignment(Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 图标容器
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        color = tintColor.copy(alpha = if (isDark) 0.15f else 0.08f),
                        shape = themedCircleShape()
                    )
            )

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = tintColor.copy(alpha = if (isDark) 0.85f else 0.7f)
            )
        }

        // 标题
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = themedTextAlign(TextAlign.Center)
        )

        // 副标题
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = themedTextAlign(TextAlign.Center),
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        // 操作按钮
        if (actionTitle != null && onAction != null) {
            PrimaryButton(
                text = actionTitle,
                onClick = onAction,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * 预设空状态工厂
 */
object EmptyStates {

    /**
     * 列表页空态：页面自带悬浮添加按钮，这里不再重复给"添加"操作
     */
    @Composable
    fun Contacts() = IllustratedEmptyState(
        icon = Icons.Default.PersonAdd,
        title = stringResource(Res.string.empty_no_contacts),
        subtitle = stringResource(Res.string.add_first_contact_subtitle),
        tintColor = MaterialTheme.colorScheme.primary
    )

    @Composable
    fun Goals() = IllustratedEmptyState(
        icon = Icons.Default.Flag,
        title = stringResource(Res.string.empty_no_goals),
        subtitle = stringResource(Res.string.add_first_goal_subtitle),
        tintColor = MaterialTheme.colorScheme.primary
    )

    @Composable
    fun Records() = IllustratedEmptyState(
        icon = Icons.Default.Edit,
        title = stringResource(Res.string.no_records_recent),
        subtitle = stringResource(Res.string.start_first_record),
        tintColor = MaterialTheme.colorScheme.primary
    )

    @Composable
    fun NoSearchResults(
        query: String
    ) = IllustratedEmptyState(
        icon = Icons.Default.Search,
        title = stringResource(Res.string.no_results_found),
        subtitle = stringResource(Res.string.try_different_search),
        tintColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview(showBackground = true)
@Composable
private fun IllustratedEmptyStatePreview() {
    AppTheme {
        IllustratedEmptyState(
            icon = Icons.Default.Flag,
            title = DemoData.Text.NO_GOALS_YET,
            subtitle = DemoData.Text.CREATE_FIRST_GOAL,
            actionTitle = DemoData.Text.NEW_GOAL,
            onAction = {}
        ) // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatesContactsPreview() {
    AppTheme {
        EmptyStates.Contacts() // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatesGoalsPreview() {
    AppTheme {
        EmptyStates.Goals() // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatesRecordsPreview() {
    AppTheme {
        EmptyStates.Records() // ponytail: demo data only
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatesNoSearchResultsPreview() {
    AppTheme {
        EmptyStates.NoSearchResults(query = DemoData.Text.KEYWORD) // ponytail: demo data only
    }
}
