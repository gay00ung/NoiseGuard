package net.lateinit.noiseguard.presentation.ui.component.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.lateinit.noiseguard.presentation.theme.Primary700
import net.lateinit.noiseguard.presentation.viewmodel.BaselineCalibrationState
import kotlin.math.roundToInt

@Composable
fun BaselineSettingsCard(
    baselineDb: Float?,
    calibrationState: BaselineCalibrationState,
    onStartCalibration: () -> Unit,
    onClearBaseline: () -> Unit
) {
    val isMeasuring = calibrationState is BaselineCalibrationState.InProgress
    val progress = if (calibrationState is BaselineCalibrationState.InProgress) {
        calibrationState.progress.coerceIn(0f, 1f)
    } else 0f

    val baselineLabel = baselineDb?.let { "현재 기준점 ${it.toInt()} dB" } ?: "아직 기준점이 없어요"
    val statusText = when (calibrationState) {
        is BaselineCalibrationState.InProgress -> "조용한 환경을 유지해 주세요. ${(progress * 100).roundToInt()}% 측정 중"
        is BaselineCalibrationState.Completed -> "새 기준점 ${calibrationState.baselineDb.toInt()} dB가 적용되었어요."
        is BaselineCalibrationState.Failed -> calibrationState.message ?: "측정에 실패했어요. 다시 시도해 주세요."
        BaselineCalibrationState.Idle -> null
    }
    val statusColor = when (calibrationState) {
        is BaselineCalibrationState.Completed -> Primary700
        is BaselineCalibrationState.Failed -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

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
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "기준점 설정",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = baselineLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AnimatedVisibility(
                        visible = statusText != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        statusText?.let {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = statusColor
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isMeasuring) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = ProgressIndicatorDefaults.linearColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onStartCalibration,
                    enabled = !isMeasuring
                ) {
                    Text(if (isMeasuring) "측정 중" else "5초간 환경 측정")
                }
                TextButton(
                    onClick = onClearBaseline,
                    enabled = baselineDb != null && !isMeasuring
                ) {
                    Text("기준점 초기화")
                }
            }
        }
    }
}
