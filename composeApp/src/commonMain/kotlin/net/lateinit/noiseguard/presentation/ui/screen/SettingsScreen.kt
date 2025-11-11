package net.lateinit.noiseguard.presentation.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import net.lateinit.noiseguard.domain.audio.*
import net.lateinit.noiseguard.presentation.theme.*
import net.lateinit.noiseguard.presentation.ui.component.settings.*
import net.lateinit.noiseguard.presentation.viewmodel.*

@Composable
fun SettingsScreen(viewModel: HomeViewModel) {
    val userOffset by CalibrationConfig.userOffsetDb.collectAsState()
    val baselineDb by CalibrationConfig.baselineDb.collectAsState()
    val relativeDisplay by CalibrationConfig.relativeDisplay.collectAsState()
    val currentDb by viewModel.currentDecibel.collectAsState()
    val baselineCalibrationState by viewModel.baselineCalibrationState.collectAsState()
    val autoTimerMinutes by CalibrationConfig.autoTimerMin.collectAsState()
    val autoTimerSeconds by CalibrationConfig.autoTimerSec.collectAsState()
    val autoTimerEnabled by CalibrationConfig.autoTimerEnabled.collectAsState()

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
                            CalibrationConfig.setUserOffset(
                                (target - currentDb).coerceIn(
                                    -40f,
                                    40f
                                )
                            )
                        }
                    )
                }

                item {
                    AutoTimerCard(
                        enabled = autoTimerEnabled,
                        minutes = autoTimerMinutes,
                        seconds = autoTimerSeconds,
                        onEnabledChange = { CalibrationConfig.setAutoTimerEnabled(it) },
                        onMinutesChange = { newMinutes ->
                            CalibrationConfig.setAutoTimer(newMinutes, autoTimerSeconds)
                        },
                        onSecondsChange = { newSeconds ->
                            CalibrationConfig.setAutoTimer(autoTimerMinutes, newSeconds)
                        },
                    )
                }
            }
        }
    }
}
