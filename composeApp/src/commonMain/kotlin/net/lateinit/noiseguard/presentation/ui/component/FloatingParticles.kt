package net.lateinit.noiseguard.presentation.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import net.lateinit.noiseguard.presentation.theme.Caution
import net.lateinit.noiseguard.presentation.theme.Danger
import net.lateinit.noiseguard.presentation.theme.Safe
import net.lateinit.noiseguard.presentation.theme.Warning
import kotlin.random.Random

@Composable
fun FloatingParticles(
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

fun getNoiseColor(decibel: Float): Color {
    return when {
        decibel < 40 -> Safe
        decibel < 50 -> Caution
        decibel < 60 -> Warning
        else -> Danger
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
            x = Random.nextFloat(),
            y = 1f,
            size = Random.nextFloat() * 4 + 2,
            speed = Random.nextFloat() * 0.005f + 0.002f,
            alpha = 1f
        )
    }
}