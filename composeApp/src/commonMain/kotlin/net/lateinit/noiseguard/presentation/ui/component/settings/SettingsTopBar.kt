package net.lateinit.noiseguard.presentation.ui.component.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import net.lateinit.noiseguard.presentation.theme.Gray700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(currentDb: Float) {
    CenterAlignedTopAppBar(
        title = {
            Column {
                Text(
                    text = "환경 보정 센터",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "실시간 측정치 ${currentDb.toInt()} dB",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray700
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = Color.Unspecified,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = Color.Unspecified
        )
    )
}
