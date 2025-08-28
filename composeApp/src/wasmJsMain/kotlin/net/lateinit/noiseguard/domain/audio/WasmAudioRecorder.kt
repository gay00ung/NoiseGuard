package net.lateinit.noiseguard.domain.audio

import kotlinx.browser.window
import kotlinx.coroutines.flow.*

/**
 * Web 플랫폼의 AudioRecorder 구현
 * 웹 브라우저에서는 녹음 기능이 제한적이므로 더미 구현
 */
actual class AudioRecorder {
    
    // Flow 구현 (더미)
    private val _decibelFlow = MutableSharedFlow<Float>(replay = 1)
    actual val decibelFlow: Flow<Float> = _decibelFlow.asSharedFlow()
    
    private val _noiseLevelFlow = MutableSharedFlow<NoiseLevel>(replay = 1)
    actual val noiseLevelFlow: Flow<NoiseLevel> = _noiseLevelFlow.asSharedFlow()
    
    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    actual val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    actual fun startRecording() {
        window.alert("웹 브라우저에서는 녹음 기능을 사용할 수 없습니다.\n모바일 앱을 이용해주세요.")
        _recordingState.value = RecordingState.ERROR
    }

    actual fun stopRecording() {
        _recordingState.value = RecordingState.IDLE
    }
    
    actual fun pauseRecording() {
        // 웹에서는 동작 없음
    }
    
    actual fun resumeRecording() {
        // 웹에서는 동작 없음
    }

    actual fun getDecibelLevel(): Float = AudioConstants.MIN_DECIBEL

    actual fun isRecording(): Boolean = false

    actual fun getAverageDecibelLevel(): Float = AudioConstants.MIN_DECIBEL

    actual fun getPeakDecibelLevel(): Float = AudioConstants.MIN_DECIBEL
}