package net.lateinit.noiseguard.presentation.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.lateinit.noiseguard.domain.audio.CalibrationConfig
import net.lateinit.noiseguard.presentation.viewmodel.HomeViewModel

@Composable
fun SettingsScreen(viewModel: HomeViewModel) {
    val userOffset by CalibrationConfig.userOffsetDb.collectAsState()
    val baselineDb by CalibrationConfig.baselineDb.collectAsState()
    val relativeDisplay by CalibrationConfig.relativeDisplay.collectAsState()
    val currentDb by viewModel.currentDecibel.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "환경 보정", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "현재 소음: ${currentDb.toInt()} dB", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { CalibrationConfig.setBaselineTo(currentDb) }) {
                Text("현재값을 기준점으로")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { CalibrationConfig.setBaselineTo(null) }) {
                Text("기준점 지우기")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (baselineDb) {
                null -> "기준점: 미설정"
                else -> "기준점: ${baselineDb!!.toInt()} dB"
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("기준 대비로 표시", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = relativeDisplay, onCheckedChange = { CalibrationConfig.setRelativeDisplay(it) })
        }
        Text(
            text = "켜면 화면에 기준점 대비 ΔdB로 표시됩니다.",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "절대 보정(미세 조정)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = userOffset,
            onValueChange = { CalibrationConfig.setUserOffset(it) },
            valueRange = -40f..40f,
            steps = 80,
            colors = SliderDefaults.colors()
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            val rounded = kotlin.math.round(userOffset * 10f) / 10f
            Text(text = "보정값: ${rounded} dB")
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { CalibrationConfig.setUserOffset(0f) }) { Text("초기화") }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            // 현재값을 35dB로 보이게 절대 보정 자동 설정
            val target = 35f
            CalibrationConfig.setUserOffset((target - currentDb).coerceIn(-40f, 40f))
        }, modifier = Modifier.fillMaxWidth()) {
            Text("현재값을 35dB로 보정")
        }
    }
}
