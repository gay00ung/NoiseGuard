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
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.lateinit.noiseguard.presentation.theme.Primary700
import net.lateinit.noiseguard.presentation.ui.component.DurationWheelPicker
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AutoTimerCard(
    enabled: Boolean,
    minutes: Long,
    seconds: Long,
    onEnabledChange: (Boolean) -> Unit,
    onMinutesChange: (Long) -> Unit,
    onSecondsChange: (Long) -> Unit,
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "자동 타이머 설정",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = enabled,
                            onCheckedChange = { checked ->
                                if (checked && minutes == 0L && seconds == 0L) {
                                    onMinutesChange(1L)
                                }
                                onEnabledChange(checked)
                            },
                            modifier = Modifier
                                .padding(start = 8.dp, bottom = 2.dp),
                            colors = SwitchDefaults.colors(
                                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                uncheckedThumbColor = Color.White,
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                    Text(
                        text = "일정 시간 동안 소음을 측정합니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DurationWheelPicker(
                minutes = minutes.toInt(),
                seconds = seconds.toInt(),
                onMinutesChange = { onMinutesChange(it.toLong()) },
                onSecondsChange = { onSecondsChange(it.toLong()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (enabled) 1f else 0.4f)
            )
        }
    }
}


@Preview
@Composable
fun AutoTimerCardPreview() {
    AutoTimerCard(
        enabled = true,
        minutes = 1,
        seconds = 30,
        onEnabledChange = {},
        onMinutesChange = {},
        onSecondsChange = {}
    )
}
