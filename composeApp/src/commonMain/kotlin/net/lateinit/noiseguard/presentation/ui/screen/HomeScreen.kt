package net.lateinit.noiseguard.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.lateinit.noiseguard.domain.model.NoiseLevel
import net.lateinit.noiseguard.presentation.theme.NoiseGuardTheme
import net.lateinit.noiseguard.presentation.viewmodel.HomeViewModel
import net.lateinit.noiseguard.presentation.ui.component.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRecording: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Background gradient animation
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f + 0.2f * gradientOffset)
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Scaffold(
            topBar = {
                ModernTopAppBar()
            },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        ) + fadeIn()
                    ) {
                        DecibelDisplay(
                            noiseLevel = uiState.currentNoiseLevel,
                            isRecording = uiState.isRecording,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                item {
                    AnimatedVisibility(
                        visible = uiState.decibelHistory.isNotEmpty(),
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                delayMillis = 300
                            )
                        ) + fadeIn()
                    ) {
                        RealtimeGraph(
                            data = uiState.decibelHistory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
                
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                delayMillis = 600
                            )
                        ) + fadeIn()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            RecordButton(
                                isRecording = uiState.isRecording,
                                onClick = {
                                    if (uiState.isRecording) {
                                        viewModel.onStopRecording()
                                    } else {
                                        viewModel.onStartRecording()
                                        onNavigateToRecording()
                                    }
                                }
                            )
                        }
                    }
                }
                
                item {
                    AnimatedVisibility(
                        visible = uiState.currentNoiseLevel != null,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                delayMillis = 900
                            )
                        ) + fadeIn()
                    ) {
                        StatsRow(
                            noiseLevel = uiState.currentNoiseLevel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Quick Actions Section
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                delayMillis = 1200
                            )
                        ) + fadeIn()
                    ) {
                        QuickActionsSection()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopAppBar() {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Column {
                    Text(
                        text = "소음지킴이",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "실시간 소음 모니터링",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            Surface(
                onClick = { /* Settings */ },
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "설정",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun QuickActionsSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "빠른 실행",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.History,
                title = "기록 보기",
                subtitle = "측정 이력",
                onClick = { /* Navigate to history */ },
                modifier = Modifier.weight(1f)
            )
            
            QuickActionCard(
                icon = Icons.Default.Analytics,
                title = "분석",
                subtitle = "소음 패턴",
                onClick = { /* Navigate to analysis */ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Sample Data Generators
private fun createSampleNoiseLevel(db: Float = 45f): NoiseLevel {
    return NoiseLevel(
        current = db,
        average = db * 0.8f,
        peak = db * 1.3f,
        timestamp = System.currentTimeMillis()
    )
}

private fun createSampleNoiseLevels(): List<NoiseLevel> {
    return listOf(
        createSampleNoiseLevel(32f),
        createSampleNoiseLevel(38f),
        createSampleNoiseLevel(42f),
        createSampleNoiseLevel(47f),
        createSampleNoiseLevel(51f),
        createSampleNoiseLevel(48f),
        createSampleNoiseLevel(45f),
        createSampleNoiseLevel(43f),
        createSampleNoiseLevel(41f),
        createSampleNoiseLevel(39f),
        createSampleNoiseLevel(37f),
        createSampleNoiseLevel(35f),
        createSampleNoiseLevel(33f),
        createSampleNoiseLevel(31f),
        createSampleNoiseLevel(34f),
        createSampleNoiseLevel(36f),
        createSampleNoiseLevel(40f),
        createSampleNoiseLevel(44f),
        createSampleNoiseLevel(49f),
        createSampleNoiseLevel(52f)
    )
}

// Preview Functions
@Preview
@Composable
fun HomeScreenPreview() {
    NoiseGuardTheme {
        Surface {
            HomeScreenContent(
                currentNoiseLevel = createSampleNoiseLevel(67f),
                decibelHistory = createSampleNoiseLevels(),
                isRecording = false,
                onNavigateToRecording = { },
                onStartRecording = { },
                onStopRecording = { }
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenRecordingPreview() {
    NoiseGuardTheme {
        Surface {
            HomeScreenContent(
                currentNoiseLevel = createSampleNoiseLevel(72f),
                decibelHistory = createSampleNoiseLevels(),
                isRecording = true,
                onNavigateToRecording = { },
                onStartRecording = { },
                onStopRecording = { }
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenEmptyPreview() {
    NoiseGuardTheme {
        Surface {
            HomeScreenContent(
                currentNoiseLevel = null,
                decibelHistory = emptyList(),
                isRecording = false,
                onNavigateToRecording = { },
                onStartRecording = { },
                onStopRecording = { }
            )
        }
    }
}

@Composable
private fun HomeScreenContent(
    currentNoiseLevel: NoiseLevel?,
    decibelHistory: List<NoiseLevel>,
    isRecording: Boolean,
    onNavigateToRecording: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    // Background gradient animation
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f + 0.2f * gradientOffset)
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Scaffold(
            topBar = {
                ModernTopAppBar()
            },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        ) + fadeIn()
                    ) {
                        DecibelDisplay(
                            noiseLevel = currentNoiseLevel,
                            isRecording = isRecording,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                item {
                    AnimatedVisibility(
                        visible = decibelHistory.isNotEmpty(),
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                delayMillis = 300
                            )
                        ) + fadeIn()
                    ) {
                        RealtimeGraph(
                            data = decibelHistory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
                
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                delayMillis = 600
                            )
                        ) + fadeIn()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            RecordButton(
                                isRecording = isRecording,
                                onClick = {
                                    if (isRecording) {
                                        onStopRecording()
                                    } else {
                                        onStartRecording()
                                        onNavigateToRecording()
                                    }
                                }
                            )
                        }
                    }
                }
                
                item {
                    AnimatedVisibility(
                        visible = currentNoiseLevel != null,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                delayMillis = 900
                            )
                        ) + fadeIn()
                    ) {
                        StatsRow(
                            noiseLevel = currentNoiseLevel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Quick Actions Section
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                delayMillis = 1200
                            )
                        ) + fadeIn()
                    ) {
                        QuickActionsSection()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}