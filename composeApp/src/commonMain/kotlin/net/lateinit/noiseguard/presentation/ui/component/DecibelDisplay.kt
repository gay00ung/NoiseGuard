package net.lateinit.noiseguard.presentation.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.lateinit.noiseguard.core.util.getCurrentTimeMillis
import net.lateinit.noiseguard.domain.model.NoiseLevel
import net.lateinit.noiseguard.domain.model.NoiseType
import net.lateinit.noiseguard.domain.model.NoiseStatus
import net.lateinit.noiseguard.presentation.theme.NoiseGuardTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headset

@Composable
fun DecibelDisplay(
    noiseLevel: NoiseLevel?,
    isRecording: Boolean,
    noiseType: NoiseType = NoiseType.UNKNOWN,
    topLabels: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val currentDb = noiseLevel?.current ?: 0f
    val status = NoiseStatus.fromDecibel(currentDb)

    // Animations
    val animatedDb by animateFloatAsState(
        targetValue = currentDb,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "db_animation"
    )

    val pulseAnimation by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotationAnimation by rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    )
                )
            )
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = status.color.copy(alpha = 0.3f),
                spotColor = status.color.copy(alpha = 0.2f)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background gradient effect
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        status.color.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.8f
                ),
                center = center,
                radius = size.minDimension * 0.8f * if (isRecording) pulseAnimation else 1f
            )
        }

        // Circular progress indicator
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularDecibelIndicator(
                progress = animatedDb / 100f,
                color = status.color,
                isAnimating = isRecording,
                rotation = rotationAnimation
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Current dB value
                AnimatedContent(
                    targetState = currentDb.toInt(),
                    transitionSpec = {
                        slideInVertically { it } + fadeIn() togetherWith
                                slideOutVertically { -it } + fadeOut()
                    },
                    label = "db_value"
                ) { db ->
                    Text(
                        text = "$db",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Thin,
                            letterSpacing = 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "dB",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Light
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = status.color.copy(alpha = 0.15f),
                    modifier = Modifier.scale(if (isRecording) pulseAnimation else 1f)
                ) {
                    Text(
                        text = status.label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = status.color,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                
            }
        }

        // Recording indicator
        if (isRecording) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 16.dp)
                    .offset(y = (-2).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(visible = true) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = status.color.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Headset,
                                contentDescription = null,
                                tint = status.color,
                                modifier = Modifier.size(16.dp)
                            )
                            val primaryLabel = topLabels.firstOrNull()?.ifBlank { null } ?: noiseType.toKoreanLabel()
                            Text(
                                text = primaryLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = status.color
                            )
                        }
                    }
                }
                RecordingIndicator()
            }
        }
    }
}

private fun NoiseType.toKoreanLabel(): String = when (this) {
    NoiseType.FOOTSTEP -> "발소리"
    NoiseType.HAMMERING -> "망치/충격음"
    NoiseType.DRAGGING -> "끌리는 소리"
    NoiseType.MUSIC -> "음악"
    NoiseType.TALKING -> "대화/말소리"
    NoiseType.DOOR -> "문 소리"
    NoiseType.WATER -> "물 소리"
    NoiseType.TYPING -> "타이핑/키보드"
    NoiseType.VACUUM -> "청소기"
    NoiseType.TRAFFIC -> "교통 소음"
    NoiseType.CONSTRUCTION -> "공사 소음"
    NoiseType.PET -> "반려동물"
    NoiseType.BABY -> "아기 울음"
    NoiseType.TV -> "TV/방송"
    NoiseType.ALARM -> "사이렌/알람"
    NoiseType.APPLIANCE -> "가전 소음"
    NoiseType.UNKNOWN -> "판정 중"
}

@Composable
private fun CircularDecibelIndicator(
    progress: Float,
    color: Color,
    isAnimating: Boolean,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 6.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val startAngle = -90f
        val sweepAngle = progress.coerceIn(0f, 1f) * 360f

        // Background arc
        drawArc(
            color = color.copy(alpha = 0.1f),
            startAngle = startAngle,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth),
            topLeft = Offset(
                (size.width - radius * 2) / 2,
                (size.height - radius * 2) / 2
            ),
            size = Size(radius * 2, radius * 2)
        )

        // Progress arc
        rotate(if (isAnimating) rotation else 0f) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        color.copy(alpha = 0.3f),
                        color,
                        color.copy(alpha = 0.3f)
                    )
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                ),
                topLeft = Offset(
                    (size.width - radius * 2) / 2,
                    (size.height - radius * 2) / 2
                ),
                size = Size(radius * 2, radius * 2)
            )
        }

        // Decorative dots
        if (isAnimating) {
            val dotCount = 12
            for (i in 0 until dotCount) {
                val angle = (360f / dotCount * i + rotation) * PI / 180f
                val dotRadius = radius - strokeWidth * 2
                val x = center.x + dotRadius * cos(angle).toFloat()
                val y = center.y + dotRadius * sin(angle).toFloat()

                drawCircle(
                    color = color.copy(alpha = 0.3f * (1f - i.toFloat() / dotCount)),
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
private fun RecordingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recording_alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color.Red.copy(alpha = alpha))
        )
        Text(
            text = "측정 중",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Preview Functions
@Preview
@Composable
fun DecibelDisplayPreview() {
    NoiseGuardTheme {
        Surface {
            DecibelDisplay(
                noiseLevel = NoiseLevel(
                    current = 65f,
                    average = 52f,
                    peak = 78f,
                    timestamp = getCurrentTimeMillis()
                ),
                isRecording = false,
                noiseType = NoiseType.UNKNOWN,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun DecibelDisplayRecordingPreview() {
    NoiseGuardTheme {
        Surface {
            DecibelDisplay(
                noiseLevel = NoiseLevel(
                    current = 72f,
                    average = 58f,
                    peak = 85f,
                    timestamp = getCurrentTimeMillis()
                ),
                isRecording = true,
                noiseType = NoiseType.MUSIC,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun DecibelDisplayHighNoisePreview() {
    NoiseGuardTheme {
        Surface {
            DecibelDisplay(
                noiseLevel = NoiseLevel(
                    current = 89f,
                    average = 75f,
                    peak = 95f,
                    timestamp = getCurrentTimeMillis()
                ),
                isRecording = true,
                noiseType = NoiseType.HAMMERING,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun DecibelDisplayEmptyPreview() {
    NoiseGuardTheme {
        Surface {
            DecibelDisplay(
                noiseLevel = null,
                isRecording = false,
                noiseType = NoiseType.UNKNOWN,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
