package net.lateinit.noiseguard.presentation.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.lateinit.noiseguard.presentation.theme.NoiseGuardTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Animations
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_scale"
    )

    val pulseAnimation by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val colorTransition by animateColorAsState(
        targetValue = if (isRecording) Color(0xFFFF4444) else MaterialTheme.colorScheme.primary,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "color_transition"
    )

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ripple effect
        if (isRecording) {
            repeat(3) { index ->
                val animationDelay = index * 400
                val rippleScale by rememberInfiniteTransition(label = "ripple_$index").animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            2000,
                            delayMillis = animationDelay,
                            easing = LinearOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "ripple_scale_$index"
                )

                val rippleAlpha by rememberInfiniteTransition(label = "ripple_alpha_$index").animateFloat(
                    initialValue = 0.6f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            2000,
                            delayMillis = animationDelay,
                            easing = LinearOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "ripple_alpha_$index"
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(rippleScale)
                        .clip(CircleShape)
                        .background(colorTransition.copy(alpha = rippleAlpha * 0.3f))
                )
            }
        }

        // Main button
        Surface(
            onClick = {
                onClick()
            },
            modifier = Modifier
                .size(90.dp)
                .scale(scale)
                .shadow(
                    elevation = if (isRecording) 25.dp else 15.dp,
                    shape = CircleShape,
                    ambientColor = colorTransition.copy(alpha = 0.4f),
                    spotColor = colorTransition.copy(alpha = 0.3f)
                ),
            shape = CircleShape,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colorTransition,
                                colorTransition.copy(alpha = 0.8f)
                            ),
                            center = Offset.Infinite
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Glass morphism overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.1f)
                            )
                        ),
                        radius = size.minDimension / 2,
                        center = center
                    )
                }

                // Inner content
                AnimatedContent(
                    targetState = isRecording,
                    transitionSpec = {
                        scaleIn(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        ) + fadeIn() togetherWith
                                scaleOut(
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                ) + fadeOut()
                    },
                    label = "button_content"
                ) { recording ->
                    if (recording) {
                        // Stop recording - Square icon
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .scale(if (isRecording) pulseAnimation else 1f)
                        )
                    } else {
                        // Start recording - Circular icon with dot
                        Box(
                            modifier = Modifier.size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Outer circle
                                drawCircle(
                                    color = Color.White,
                                    radius = size.minDimension / 2,
                                    center = center,
                                    style = Stroke(width = 3.dp.toPx())
                                )

                                // Inner dot
                                drawCircle(
                                    color = Color.White,
                                    radius = size.minDimension / 6,
                                    center = center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Button label
        if (!isRecording) {
            Text(
                text = "녹음 시작",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = 16.dp)
            )
        }
    }
}

// Preview Functions
@Preview
@Composable
fun RecordButtonPreview() {
    NoiseGuardTheme {
        Surface {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RecordButton(
                    isRecording = false,
                    onClick = { }
                )

                Text(
                    text = "녹음 시작 상태",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Preview
@Composable
fun RecordButtonRecordingPreview() {
    NoiseGuardTheme {
        Surface {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RecordButton(
                    isRecording = true,
                    onClick = { }
                )

                Text(
                    text = "녹음 중 상태",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}