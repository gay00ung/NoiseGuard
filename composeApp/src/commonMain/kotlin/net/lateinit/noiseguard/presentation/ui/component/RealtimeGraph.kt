package net.lateinit.noiseguard.presentation.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.lateinit.noiseguard.core.util.getCurrentTimeMillis
import net.lateinit.noiseguard.domain.model.NoiseLevel
import net.lateinit.noiseguard.domain.model.NoiseStatus
import net.lateinit.noiseguard.presentation.theme.NoiseGuardTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun RealtimeGraph(
    data: List<NoiseLevel>,
    modifier: Modifier = Modifier,
    showGrid: Boolean = true
) {
    val animatedData by remember(data) {
        derivedStateOf { data.takeLast(50) } // Show last 50 data points
    }

    // Smooth animation for new data points
    val dataAnimations = animatedData.map { noiseLevel ->
        val animatedValue by animateFloatAsState(
            targetValue = noiseLevel.current,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "data_point_${noiseLevel.timestamp}"
        )
        animatedValue
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f)
                    )
                )
            )
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "실시간 소음 레벨",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (animatedData.isNotEmpty()) {
                val currentLevel = animatedData.last()
                val status = NoiseStatus.fromDecibel(currentLevel.current)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = status.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${currentLevel.current.toInt()} dB",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = status.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Graph
        val primaryColor = MaterialTheme.colorScheme.primary
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            if (dataAnimations.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val maxValue = 100f
            val minValue = 0f

            // Grid
            if (showGrid) {
                drawGrid(width, height, maxValue, minValue)
            }

            // Data path
            val path = Path()
            val gradientPath = Path()

            val denom = maxOf(dataAnimations.size - 1, 1)
            dataAnimations.forEachIndexed { index, value ->
                val x = (index.toFloat() / denom) * width
                val y = height - ((value - minValue) / (maxValue - minValue)) * height

                if (index == 0) {
                    path.moveTo(x, y)
                    gradientPath.moveTo(x, height)
                    gradientPath.lineTo(x, y)
                } else {
                    // Smooth curve using quadratic bezier
                    val prevX = ((index - 1).toFloat() / denom) * width
                    val prevY =
                        height - ((dataAnimations[index - 1] - minValue) / (maxValue - minValue)) * height

                    val controlX = (prevX + x) / 2
                    val controlY = (prevY + y) / 2

                    path.quadraticTo(controlX, prevY, x, y)
                    gradientPath.lineTo(x, y)
                }
            }

            gradientPath.lineTo(width, height)
            gradientPath.close()

            // Gradient fill
            drawPath(
                path = gradientPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.3f),
                        primaryColor.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Main line
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Data points
            dataAnimations.forEachIndexed { index, value ->
                val x = (index.toFloat() / (dataAnimations.size - 1)) * width
                val y = height - ((value - minValue) / (maxValue - minValue)) * height
                val status = NoiseStatus.fromDecibel(value)

                // Outer glow
                drawCircle(
                    color = status.color.copy(alpha = 0.3f),
                    radius = 8.dp.toPx(),
                    center = Offset(x, y)
                )

                // Inner point
                drawCircle(
                    color = status.color,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )

                // Center highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        // Animated indicator for latest data point
        if (animatedData.isNotEmpty()) {
            val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
                initialValue = 1f,
                targetValue = 1.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 60.dp, end = 20.dp)
                    .size(8.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(50))
                    .background(primaryColor)
            )
        }
    }
}

private fun DrawScope.drawGrid(
    width: Float,
    height: Float,
    maxValue: Float,
    minValue: Float
) {
    val gridColor = Color.Gray.copy(alpha = 0.2f)
    val strokeWidth = 1.dp.toPx()

    // Horizontal grid lines
    for (i in 0..4) {
        val y = (i.toFloat() / 4) * height
        val value = maxValue - (i.toFloat() / 4) * (maxValue - minValue)

        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )
    }

    // Vertical grid lines
    for (i in 0..6) {
        val x = (i.toFloat() / 6) * width

        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )
    }
}

// Sample data generator for previews
private fun createSampleGraphData(): List<NoiseLevel> {
    val baseTime = getCurrentTimeMillis()
    return (0..19).map { index ->
        val time = baseTime - (19 - index) * 1000L
        val noise =
            30f + sin(index * 0.3f) * 15f + (index % 3) * 5f + Random.nextFloat() * 8f
        NoiseLevel(
            current = noise,
            average = noise * 0.8f,
            peak = noise * 1.2f,
            timestamp = time
        )
    }
}

// Preview Functions
@Preview
@Composable
fun RealtimeGraphPreview() {
    NoiseGuardTheme {
        Surface {
            RealtimeGraph(
                data = createSampleGraphData(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun RealtimeGraphEmptyPreview() {
    NoiseGuardTheme {
        Surface {
            RealtimeGraph(
                data = emptyList(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun RealtimeGraphHighNoisePreview() {
    NoiseGuardTheme {
        Surface {
            val highNoiseData = (0..19).map { index ->
                val baseTime = getCurrentTimeMillis()
                val time = baseTime - (19 - index) * 1000L
                val noise = 70f + sin(index * 0.4f) * 20f + Random.nextFloat() * 10f
                NoiseLevel(
                    current = noise.coerceIn(50f, 95f),
                    average = noise * 0.8f,
                    peak = noise * 1.3f,
                    timestamp = time
                )
            }

            RealtimeGraph(
                data = highNoiseData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(16.dp)
            )
        }
    }
}
