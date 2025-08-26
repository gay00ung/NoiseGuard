package net.lateinit.noiseguard.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
//    val viewModel = remember { NoiseViewModel() }
//    val noiseLevel by viewModel.noiseLevel.collectAsState()
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF5F5F5)),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // 현재 시간
//        CurrentTimeCard()
//
//        // 데시벨 표시
//        DecibelDisplay(
//            currentDb = noiseLevel.current,
//            status = getNoiseStatus(noiseLevel.current)
//        )
//
//        // 실시간 그래프
//        RealtimeGraph(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(150.dp)
//                .padding(16.dp)
//        )
//
//        // 녹음 버튼
//        RecordButton(
//            isRecording = viewModel.isRecording,
//            onClick = {
//                if (viewModel.isRecording) {
//                    viewModel.stopMeasuring()
//                } else {
//                    viewModel.startMeasuring()
//                }
//            }
//        )
//
//        // 오늘의 통계
//        DailyStatsCard(
//            avgDb = noiseLevel.average,
//            peakDb = noiseLevel.peak
//        )
//    }
}