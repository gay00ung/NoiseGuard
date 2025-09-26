package net.lateinit.noiseguard.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import net.lateinit.noiseguard.domain.audio.CalibrationConfig
import net.lateinit.noiseguard.presentation.viewmodel.BaselineCalibrationState
import net.lateinit.noiseguard.presentation.viewmodel.HomeViewModel
import net.lateinit.noiseguard.presentation.theme.AccentTeal
import net.lateinit.noiseguard.presentation.theme.Gray700
import net.lateinit.noiseguard.presentation.theme.Primary700
import net.lateinit.noiseguard.presentation.theme.Primary900

@Composable
fun SettingsScreen(viewModel: HomeViewModel) {
    val userOffset by CalibrationConfig.userOffsetDb.collectAsState()
    val baselineDb by CalibrationConfig.baselineDb.collectAsState()
    val relativeDisplay by CalibrationConfig.relativeDisplay.collectAsState()
    val currentDb by viewModel.currentDecibel.collectAsState()
    val baselineCalibrationState by viewModel.baselineCalibrationState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Primary900.copy(alpha = 0.1f),
                        Primary700.copy(alpha = 0.05f),
                        Color.White
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SettingsTopBar(currentDb = currentDb)
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    MeasurementGuidanceCard(currentDb = currentDb)
                }

                item {
                    BaselineSettingsCard(
                        baselineDb = baselineDb,
                        calibrationState = baselineCalibrationState,
                        onStartCalibration = { viewModel.startBaselineCalibration() },
                        onClearBaseline = {
                            CalibrationConfig.setBaselineTo(null)
                            viewModel.resetBaselineCalibrationState()
                        }
                    )
                }

                item {
                    RelativeDisplayCard(
                        isEnabled = relativeDisplay,
                        hasBaseline = baselineDb != null,
                        onToggle = { CalibrationConfig.setRelativeDisplay(it) }
                    )
                }

                item {
                    CalibrationFineTuneCard(
                        userOffset = userOffset,
                        onOffsetChange = { CalibrationConfig.setUserOffset(it) },
                        onReset = { CalibrationConfig.setUserOffset(0f) },
                        onAutoTune = {
                            val target = 35f
                            CalibrationConfig.setUserOffset((target - currentDb).coerceIn(-40f, 40f))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(currentDb: Float) {
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
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun MeasurementGuidanceCard(currentDb: Float) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = AccentTeal.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.TipsAndUpdates,
                    contentDescription = null,
                    tint = AccentTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "조용한 환경에서 측정해 주세요",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "주변 소음과 기기 진동을 줄이고, 마이크를 막지 않은 상태에서 측정하면 가장 정확한 기준을 얻을 수 있어요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "현재 소음",
                        style = MaterialTheme.typography.labelLarge,
                        color = Gray700
                    )
                    Text(
                        text = "${currentDb.toInt()} dB",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BaselineSettingsCard(
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
        is BaselineCalibrationState.InProgress -> "조용한 환경을 유지해 주세요. ${ (progress * 100).roundToInt() }% 측정 중"
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
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
                    AnimatedVisibility(visible = statusText != null, enter = fadeIn(), exit = fadeOut()) {
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
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
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

@Composable
private fun RelativeDisplayCard(
    isEnabled: Boolean,
    hasBaseline: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoGraph,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ΔdB 상대 표시",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "기준점과 비교한 변화를 한눈에 확인해요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isEnabled) "상대 표시는 켜져 있어요" else "상대 표시가 꺼져 있어요",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (hasBaseline) "설정된 기준점 기준으로 ΔdB를 보여줍니다." else "기준점이 있어야 정확한 ΔdB를 계산할 수 있어요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    enabled = hasBaseline
                )
            }
        }
    }
}

@Composable
private fun CalibrationFineTuneCard(
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
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                val rounded = kotlin.math.round(userOffset * 10f) / 10f
                Column {
                    Text(
                        text = "현재 보정값",
                        style = MaterialTheme.typography.labelLarge,
                        color = Gray700
                    )
                    Text(
                        text = "${rounded} dB",
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
