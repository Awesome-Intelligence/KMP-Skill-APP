package com.androidkit.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.androidkit.app.ui.components.AppCard
import com.androidkit.app.ui.components.PrimaryButton

/**
 * 三端共用的演示页。
 *
 * 装进新项目后换成自己的首页就行 —— 这里只是让 kit 自己能跑起来、能截图、能验证编译。
 */
@Composable
fun App() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Android-Kit", style = MaterialTheme.typography.headlineMedium)
        AppCard {
            Text("Compose Multiplatform 演示", style = MaterialTheme.typography.bodyLarge)
            Text(
                "同一套组件：Android / iOS / Desktop",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        PrimaryButton(text = "点一下", onClick = {})
    }
}
