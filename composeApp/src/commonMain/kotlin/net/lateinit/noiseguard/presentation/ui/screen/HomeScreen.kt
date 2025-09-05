package net.lateinit.noiseguard.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.lateinit.noiseguard.domain.audio.RecordingState
import net.lateinit.noiseguard.domain.model.NoiseLevel
import net.lateinit.noiseguard.presentation.theme.AccentTeal
import net.lateinit.noiseguard.presentation.theme.Danger
import net.lateinit.noiseguard.presentation.theme.DarkBgPrimary
import net.lateinit.noiseguard.presentation.theme.Gray100
import net.lateinit.noiseguard.presentation.theme.Gray700
import net.lateinit.noiseguard.presentation.theme.Primary700
import net.lateinit.noiseguard.presentation.theme.Primary900
import net.lateinit.noiseguard.presentation.theme.Safe
import net.lateinit.noiseguard.presentation.theme.Warning
import net.lateinit.noiseguard.presentation.ui.component.AIAnalysisCard
import net.lateinit.noiseguard.presentation.ui.component.ControlButton
import net.lateinit.noiseguard.presentation.ui.component.DecibelDisplay
import net.lateinit.noiseguard.presentation.ui.component.FloatingParticles
import net.lateinit.noiseguard.presentation.ui.component.MiniWaveform
import net.lateinit.noiseguard.presentation.ui.component.PulsingDot
import net.lateinit.noiseguard.presentation.ui.component.RealtimeGraph
import net.lateinit.noiseguard.presentation.ui.component.StatWidget
import net.lateinit.noiseguard.presentation.ui.component.getNoiseColor
import net.lateinit.noiseguard.presentation.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val currentDecibel by viewModel.currentDecibel.collectAsStateWithLifecycle()
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val noiseLevel by viewModel.noiseLevel.collectAsStateWithLifecycle()
    val noiseType by viewModel.noiseType.collectAsStateWithLifecycle()
    val topLabels by viewModel.topLabels.collectAsStateWithLifecycle()
    val relativeDisplay by net.lateinit.noiseguard.domain.audio.CalibrationConfig.relativeDisplay.collectAsState()
    val baselineDb by net.lateinit.noiseguard.domain.audio.CalibrationConfig.baselineDb.collectAsState()

    val noiseLevelHistory = remember { mutableStateListOf<NoiseLevel>() }

    LaunchedEffect(recordingState) {
        if (recordingState != RecordingState.RECORDING) {
            noiseLevelHistory.clear()
        }
    }

    LaunchedEffect(noiseLevel.timestamp, recordingState, relativeDisplay, baselineDb) {
        if (recordingState == RecordingState.RECORDING) {
            val base = baselineDb ?: 0f
            val adjusted = if (relativeDisplay && baselineDb != null) {
                val cur = (noiseLevel.current - base).coerceAtLeast(0f)
                val avg = (noiseLevel.average - base).coerceAtLeast(0f)
                val peak = (noiseLevel.peak - base).coerceAtLeast(0f)
                noiseLevel.copy(current = cur, average = avg, peak = peak)
            } else noiseLevel
            noiseLevelHistory.add(adjusted)
            if (noiseLevelHistory.size > 50) {
                noiseLevelHistory.removeAt(0)
            }
        }
    }

    val islandExpanded by animateFloatAsState(
        targetValue = if (recordingState == RecordingState.RECORDING) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // Glassmorphism 배경 효과
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Primary900.copy(alpha = 0.05f),
                        Primary700.copy(alpha = 0.02f),
                        Color.White
                    )
                )
            )
    ) {
        val baseForDisplay = baselineDb ?: 0f
        val displayCurrentDb = if (relativeDisplay && baselineDb != null) (currentDecibel - baseForDisplay).coerceAtLeast(0f) else currentDecibel

        FloatingParticles(
            isActive = recordingState == RecordingState.RECORDING,
            color = getNoiseColor(displayCurrentDb)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                DynamicIslandTopBar(
                    isRecording = recordingState == RecordingState.RECORDING,
                    currentDb = displayCurrentDb,
                    expansion = islandExpanded
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 메인 데시벨 디스플레이 카드
                item {
                    val displayNoiseLevel = if (relativeDisplay && baselineDb != null) {
                        val base = baselineDb ?: 0f
                        val cur = (noiseLevel.current - base).coerceAtLeast(0f)
                        val avg = (noiseLevel.average - base).coerceAtLeast(0f)
                        val peak = (noiseLevel.peak - base).coerceAtLeast(0f)
                        noiseLevel.copy(current = cur, average = avg, peak = peak)
                    } else noiseLevel

                    DecibelDisplay(
                        noiseLevel = displayNoiseLevel,
                        isRecording = recordingState == RecordingState.RECORDING,
                        noiseType = noiseType,
                        topLabels = topLabels,
                        modifier = Modifier.animateContentSize()
                    )
                }

                // Live Activity 스타일 상태 카드
                item {
                    val base = baselineDb ?: 0f
                    val displayNoiseLevel2 = if (relativeDisplay && baselineDb != null) {
                        val cur = (noiseLevel.current - base).coerceAtLeast(0f)
                        val avg = (noiseLevel.average - base).coerceAtLeast(0f)
                        val peak = (noiseLevel.peak - base).coerceAtLeast(0f)
                        noiseLevel.copy(current = cur, average = avg, peak = peak)
                    } else noiseLevel
                    LiveActivityCard(
                        recordingState = recordingState,
                        noiseLevel = displayNoiseLevel2
                    )
                }

                // Control Center 스타일 컨트롤
                item {
                    ControlCenterSection(
                        recordingState = recordingState,
                        onToggleRecording = { viewModel.toggleRecording() },
                        onPause = { viewModel.pauseRecording() },
                        onResume = { viewModel.resumeRecording() }
                    )
                }

                // 실시간 그래프
                if (recordingState != RecordingState.IDLE && noiseLevelHistory.isNotEmpty()) {
                    item {
                        RealtimeGraph(
                            data = noiseLevelHistory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
                }

                // 통계 카드들
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatWidget(
                            title = "평균",
                            value = "${((if (relativeDisplay && baselineDb != null) (noiseLevel.average - (baselineDb ?: 0f)) else noiseLevel.average).coerceAtLeast(0f)).toInt()}",
                            unit = "dB",
                            icon = Icons.Outlined.BarChart,
                            color = Safe,
                            modifier = Modifier.weight(1f)
                        )
                        StatWidget(
                            title = "최대",
                            value = "${((if (relativeDisplay && baselineDb != null) (noiseLevel.peak - (baselineDb ?: 0f)) else noiseLevel.peak).coerceAtLeast(0f)).toInt()}",
                            unit = "dB",
                            icon = Icons.AutoMirrored.Outlined.TrendingUp,
                            color = Warning,
                            modifier = Modifier.weight(1f)
                        )
                        StatWidget(
                            title = "현재",
                            value = "${displayCurrentDb.toInt()}",
                            unit = "dB",
                            icon = Icons.AutoMirrored.Outlined.TrendingDown,
                            color = AccentTeal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // AI 분석 카드
                item {
                    val base = baselineDb ?: 0f
                    val displayNoiseLevel3 = if (relativeDisplay && baselineDb != null) {
                        val cur = (noiseLevel.current - base).coerceAtLeast(0f)
                        val avg = (noiseLevel.average - base).coerceAtLeast(0f)
                        val peak = (noiseLevel.peak - base).coerceAtLeast(0f)
                        noiseLevel.copy(current = cur, average = avg, peak = peak)
                    } else noiseLevel
                    AIAnalysisCard(
                        currentDecibel = displayCurrentDb,
                        noiseLevel = displayNoiseLevel3
                    )
                }
            }
        }
    }
}

// noise type label mapping now handled in DecibelDisplay

@Composable
private fun DynamicIslandTopBar(
    isRecording: Boolean,
    currentDb: Float,
    expansion: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(min = 120.dp, max = 280.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                ),
            shape = RoundedCornerShape(
                (24 + 8 * expansion).dp
            ),
            color = DarkBgPrimary.copy(alpha = 0.95f),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = (16 + 8 * expansion).dp,
                        vertical = (8 + 4 * expansion).dp
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRecording) {
                    // Recording indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${currentDb.toInt()} dB",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )

                    AnimatedVisibility(visible = expansion > 0.5f) {
                        Row {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "• 녹음 중",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                } else {
                    Icon(
                        Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "NoiseGuard",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
private fun LiveActivityCard(
    recordingState: RecordingState,
    noiseLevel: NoiseLevel
) {
    AnimatedVisibility(
        visible = recordingState == RecordingState.RECORDING,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Primary900.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, Primary700.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PulsingDot(color = Color.Red)
                    Column {
                        Text(
                            "실시간 측정 중",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "평균 ${noiseLevel.average.toInt()}dB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Live waveform
                MiniWaveform(
                    modifier = Modifier.size(width = 80.dp, height = 40.dp)
                )
            }
        }
    }
}

@Composable
private fun ControlCenterSection(
    recordingState: RecordingState,
    onToggleRecording: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Gray100.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main record button
            ControlButton(
                onClick = onToggleRecording,
                containerColor = when (recordingState) {
                    RecordingState.RECORDING -> Danger
                    RecordingState.PAUSED -> Warning
                    else -> Primary700
                },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = when (recordingState) {
                        RecordingState.RECORDING -> Icons.Default.Stop
                        else -> Icons.Default.FiberManualRecord
                    },
                    contentDescription = "Record",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Secondary controls
            if (recordingState == RecordingState.RECORDING) {
                ControlButton(
                    onClick = onPause,
                    containerColor = Gray700.copy(alpha = 0.3f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (recordingState == RecordingState.PAUSED) {
                ControlButton(
                    onClick = onResume,
                    containerColor = Safe.copy(alpha = 0.3f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
