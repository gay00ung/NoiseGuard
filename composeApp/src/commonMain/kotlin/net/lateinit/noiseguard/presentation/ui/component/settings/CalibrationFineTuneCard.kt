package net.lateinit.noiseguard.presentation.ui.component.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.lateinit.noiseguard.presentation.theme.Gray700
import net.lateinit.noiseguard.presentation.theme.Primary700
import kotlin.math.round

@Composable
fun CalibrationFineTuneCard(
    userOffset: Float,
    onOffsetChange: (Float) -> Unit,
    onReset: () -> Unit,
    onAutoTune: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "절대 보정 미세 조정",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "현장 상황에 맞춰 감도를 섬세하게 맞춰보세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Slider(
                value = userOffset,
                onValueChange = onOffsetChange,
                valueRange = -40f..40f,
                steps = 80,
                colors = SliderDefaults.colors()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val rounded = round(userOffset * 10f) / 10f
                Column {
                    Text(
                        text = "현재 보정값",
                        style = MaterialTheme.typography.labelLarge,
                        color = Gray700
                    )
                    Text(
                        text = "$rounded dB",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onReset, enabled = rounded != 0f) {
                    Text("초기화")
                }
            }

            FilledTonalButton(
                onClick = onAutoTune,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("현재값을 35dB로 자동 보정")
            }
        }
    }
}
