package net.lateinit.noiseguard.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import net.lateinit.noiseguard.domain.audio.RecordingState
import net.lateinit.noiseguard.domain.model.NoiseLevel
import net.lateinit.noiseguard.presentation.theme.AccentPurple
import net.lateinit.noiseguard.presentation.theme.AccentTeal
import net.lateinit.noiseguard.presentation.theme.Caution
import net.lateinit.noiseguard.presentation.theme.Danger
import net.lateinit.noiseguard.presentation.theme.DarkBgPrimary
import net.lateinit.noiseguard.presentation.theme.Gray100
import net.lateinit.noiseguard.presentation.theme.Gray300
import net.lateinit.noiseguard.presentation.theme.Gray700
import net.lateinit.noiseguard.presentation.theme.Primary700
import net.lateinit.noiseguard.presentation.theme.Primary900
import net.lateinit.noiseguard.presentation.theme.Safe
import net.lateinit.noiseguard.presentation.theme.Warning
import net.lateinit.noiseguard.presentation.ui.component.DecibelDisplay
import net.lateinit.noiseguard.presentation.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val currentDecibel by viewModel.currentDecibel.collectAsStateWithLifecycle()
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val noiseLevel by viewModel.noiseLevel.collectAsStateWithLifecycle()
    
    
    // 2025 iOS 트렌드: Dynamic Island 스타일 애니메이션
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
        // Floating particles effect (iOS 17 style)
        FloatingParticles(
            isActive = recordingState == RecordingState.RECORDING,
            color = getNoiseColor(currentDecibel)
        )
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                DynamicIslandTopBar(
                    isRecording = recordingState == RecordingState.RECORDING,
                    currentDb = currentDecibel,
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
                    DecibelDisplay(
                        noiseLevel = noiseLevel,
                        isRecording = recordingState == RecordingState.RECORDING,
                        modifier = Modifier.animateContentSize()
                    )
                }
                
                // Live Activity 스타일 상태 카드
                item {
                    LiveActivityCard(
                        recordingState = recordingState,
                        noiseLevel = noiseLevel
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
                
                // 실시간 그래프 (Vision Pro 스타일)
                if (recordingState != RecordingState.IDLE) {
                    item {
                        VisionProStyleGraph(
                            noiseLevel = noiseLevel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
                
                // 통계 카드들 (iOS 17 Widget 스타일)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatWidget(
                            title = "평균",
                            value = "${noiseLevel.average.toInt()}",
                            unit = "dB",
                            icon = Icons.Outlined.BarChart,
                            color = Safe,
                            modifier = Modifier.weight(1f)
                        )
                        StatWidget(
                            title = "최대",
                            value = "${noiseLevel.peak.toInt()}",
                            unit = "dB", 
                            icon = Icons.AutoMirrored.Outlined.TrendingUp,
                            color = Warning,
                            modifier = Modifier.weight(1f)
                        )
                        StatWidget(
                            title = "현재",
                            value = "${noiseLevel.current.toInt()}",
                            unit = "dB",
                            icon = Icons.AutoMirrored.Outlined.TrendingDown,
                            color = AccentTeal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // AI 분석 카드 (iOS 18 Intelligence 스타일)
                item {
                    AIAnalysisCard(
                        currentDecibel = currentDecibel,
                        noiseLevel = noiseLevel
                    )
                }
                
                // Quick Actions (iOS Control Center 스타일)
                item { 
                    QuickActionsGrid()
                }
                
                item { 
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

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
                containerColor = when(recordingState) {
                    RecordingState.RECORDING -> Danger
                    RecordingState.PAUSED -> Warning
                    else -> Primary700
                },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = when(recordingState) {
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

@Composable
private fun ControlButton(
    onClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = containerColor,
        tonalElevation = 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}

@Composable
private fun StatWidget(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    value,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = color
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AIAnalysisCard(
    currentDecibel: Float,
    noiseLevel: NoiseLevel
) {
    val analysis = getAIAnalysis(currentDecibel)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = AccentPurple.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "AI 분석",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                analysis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (currentDecibel > 50) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Warning.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "법적 기준 초과 가능성",
                            style = MaterialTheme.typography.labelMedium,
                            color = Warning
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsGrid() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "빠른 작업",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionTile(
                icon = Icons.Default.History,
                label = "기록",
                color = Primary700,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                icon = Icons.Default.Assessment,
                label = "리포트",
                color = AccentTeal,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                icon = Icons.Default.Share,
                label = "공유",
                color = AccentPurple,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                icon = Icons.Default.Settings,
                label = "설정",
                color = Gray700,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { },
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

// Helper Composables

@Composable
private fun PulsingDot(color: Color) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .size(12.dp)
            .scale(scale)
            .background(color, CircleShape)
    )
}

@Composable
private fun MiniWaveform(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        )
    )
    
    Canvas(modifier = modifier) {
        val barCount = 12
        val barWidth = size.width / (barCount * 2)
        
        for (i in 0 until barCount) {
            val height = size.height * (0.3f + 0.7f * kotlin.math.sin((i + phase * barCount) * 0.5f))
            drawRect(
                color = Primary700.copy(alpha = 0.6f),
                topLeft = Offset(i * barWidth * 2, (size.height - height) / 2),
                size = androidx.compose.ui.geometry.Size(barWidth, height)
            )
        }
    }
}

@Composable
private fun VisionProStyleGraph(
    noiseLevel: NoiseLevel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 0.5.dp)
        ) {
            // Graph implementation
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Draw grid
                val gridLines = 5
                for (i in 0..gridLines) {
                    val y = size.height * i / gridLines
                    drawLine(
                        color = Gray300.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }
                
                // Draw wave
                val path = Path()
                path.moveTo(0f, size.height / 2)
                
                for (x in 0..size.width.toInt() step 10) {
                    val y = size.height / 2 + kotlin.math.sin(x * 0.02f) * 50
                    path.lineTo(x.toFloat(), y)
                }
                
                drawPath(
                    path = path,
                    color = getNoiseColor(noiseLevel.current),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun FloatingParticles(
    isActive: Boolean,
    color: Color
) {
    if (!isActive) return
    
    var particles by remember { mutableStateOf(listOf<Particle>()) }
    
    LaunchedEffect(isActive) {
        while (isActive) {
            particles = particles.filter { it.alpha > 0.01f }.map { 
                it.copy(
                    y = it.y - it.speed,
                    alpha = it.alpha * 0.98f
                )
            } + if (particles.size < 20) {
                listOf(Particle.random())
            } else emptyList()
            
            delay(50)
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawCircle(
                color = color.copy(alpha = particle.alpha * 0.3f),
                radius = particle.size,
                center = Offset(
                    x = size.width * particle.x,
                    y = size.height * particle.y
                )
            )
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
) {
    companion object {
        fun random() = Particle(
            x = kotlin.random.Random.nextFloat(),
            y = 1f,
            size = kotlin.random.Random.nextFloat() * 4 + 2,
            speed = kotlin.random.Random.nextFloat() * 0.005f + 0.002f,
            alpha = 1f
        )
    }
}

// Helper functions
private fun getNoiseColor(decibel: Float): Color {
    return when {
        decibel < 40 -> Safe
        decibel < 50 -> Caution
        decibel < 60 -> Warning
        else -> Danger
    }
}

private fun getAIAnalysis(decibel: Float): String {
    return when {
        decibel < 35 -> "매우 조용한 환경입니다. 수면이나 집중 작업에 이상적입니다."
        decibel < 45 -> "일상적인 실내 환경입니다. 대화나 작업에 적합한 수준입니다."
        decibel < 55 -> "약간 시끄러운 환경입니다. 장시간 노출 시 피로감을 느낄 수 있습니다."
        decibel < 65 -> "시끄러운 환경입니다. 층간소음 기준에 근접하고 있습니다."
        decibel < 75 -> "매우 시끄러운 환경입니다. 법적 기준을 초과할 가능성이 높습니다."
        else -> "위험한 소음 수준입니다. 즉시 조치가 필요하며, 증거 수집을 권장합니다."
    }
}