package net.lateinit.noiseguard.domain.audio

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import net.lateinit.noiseguard.domain.model.NoiseLevel

/**
 * 오디오 녹음 및 데시벨 측정을 위한 멀티플랫폼 인터페이스
 * Flow 기반으로 실시간 데이터 스트림 제공
 */
expect class AudioRecorder {
    fun startRecording()
    fun stopRecording()
    fun pauseRecording()
    fun resumeRecording()
    
    // 단일 값 반환 (기존 호환성)
    fun getDecibelLevel(): Float
    fun isRecording(): Boolean
    fun getAverageDecibelLevel(): Float
    fun getPeakDecibelLevel(): Float
    
    // Flow 기반 실시간 스트림 (새로운 기능)
    val decibelFlow: Flow<Float>
    val noiseLevelFlow: Flow<NoiseLevel>
    val recordingState: StateFlow<RecordingState>
}

/**
 * 녹음 상태
 */
enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    ERROR
}