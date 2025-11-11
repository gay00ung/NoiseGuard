package net.lateinit.noiseguard.presentation.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.lateinit.noiseguard.domain.model.NoiseLevel
import net.lateinit.noiseguard.presentation.theme.AccentPurple
import net.lateinit.noiseguard.presentation.theme.Warning

@Composable
fun AIAnalysisCard(
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