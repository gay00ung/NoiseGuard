package net.lateinit.noiseguard.presentation.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.lateinit.noiseguard.core.util.getCurrentTimeMillis
import net.lateinit.noiseguard.domain.model.NoiseLevel
import net.lateinit.noiseguard.presentation.theme.NoiseGuardTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun StatsRow(
    noiseLevel: NoiseLevel?,
    modifier: Modifier = Modifier
) {
    val stats = listOf(
        StatItem(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            label = "현재",
            value = "${noiseLevel?.current?.toInt() ?: 0} dB",
            color = MaterialTheme.colorScheme.primary
        ),
        StatItem(
            icon = Icons.Default.BarChart,
            label = "평균",
            value = "${noiseLevel?.average?.toInt() ?: 0} dB",
            color = MaterialTheme.colorScheme.secondary
        ),
        StatItem(
            icon = Icons.Default.ArrowUpward,
            label = "최고",
            value = "${noiseLevel?.peak?.toInt() ?: 0} dB",
            color = MaterialTheme.colorScheme.tertiary
        )
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(stats.size) { index ->
            StatCard(
                stat = stats[index],
                animationDelay = index * 100
            )
        }
    }
}

@Composable
private fun StatCard(
    stat: StatItem,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        isVisible = true
    }

    // Entry animation
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "stat_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "stat_alpha"
    )

    Card(
        modifier = modifier
            .width(110.dp)
            .scale(scale)
            .alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                stat.color.copy(alpha = 0.2f),
                                stat.color.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stat.icon,
                    contentDescription = stat.label,
                    tint = stat.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Value
            AnimatedContent(
                targetState = stat.value,
                transitionSpec = {
                    slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut()
                },
                label = "stat_value"
            ) { value ->
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Label
            Text(
                text = stat.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class StatItem(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val color: Color
)

// Preview Functions
@Preview
@Composable
fun StatsRowPreview() {
    NoiseGuardTheme {
        Surface {
            StatsRow(
                noiseLevel = NoiseLevel(
                    current = 58f,
                    average = 45f,
                    peak = 72f,
                    timestamp = getCurrentTimeMillis()
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun StatsRowHighNoisePreview() {
    NoiseGuardTheme {
        Surface {
            StatsRow(
                noiseLevel = NoiseLevel(
                    current = 82f,
                    average = 67f,
                    peak = 94f,
                    timestamp = getCurrentTimeMillis()
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun StatsRowEmptyPreview() {
    NoiseGuardTheme {
        Surface {
            StatsRow(
                noiseLevel = null,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}