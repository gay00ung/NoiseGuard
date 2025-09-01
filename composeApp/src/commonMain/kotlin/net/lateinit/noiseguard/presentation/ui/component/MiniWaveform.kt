package net.lateinit.noiseguard.presentation.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import net.lateinit.noiseguard.presentation.theme.Primary700
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.sin

@Composable
fun MiniWaveform(modifier: Modifier = Modifier) {
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
            val height =
                size.height * (0.3f + 0.7f * sin((i + phase * barCount) * 0.5f))
            drawRect(
                color = Primary700.copy(alpha = 0.6f),
                topLeft = Offset(i * barWidth * 2, (size.height - height) / 2),
                size = Size(barWidth, height)
            )
        }
    }
}

@Preview
@Composable
fun MiniWaveformPreview() {
    MiniWaveform(modifier = Modifier)
}