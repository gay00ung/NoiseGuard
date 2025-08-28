package net.lateinit.noiseguard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.lateinit.noiseguard.core.util.getCurrentTimeMillis
import net.lateinit.noiseguard.domain.audio.AudioRecorderFactory
import net.lateinit.noiseguard.domain.audio.RecordingState
import net.lateinit.noiseguard.domain.model.NoiseLevel

class HomeViewModel : ViewModel() {
    private val audioRecorder = AudioRecorderFactory.createAudioRecorder()
    
    // 실시간 데시벨 Flow를 StateFlow로 변환
    val currentDecibel: StateFlow<Float> = audioRecorder.decibelFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 30f
        )
    
    // 녹음 상태
    val recordingState: StateFlow<RecordingState> = audioRecorder.recordingState
    
    // 소음 레벨 정보
    val noiseLevel: StateFlow<NoiseLevel> = audioRecorder.noiseLevelFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NoiseLevel(
                current = 30f,
                average = 30f,
                peak = 30f,
                timestamp = getCurrentTimeMillis()
            )
        )
    
    // 녹음 시작
    fun startRecording() {
        viewModelScope.launch {
            audioRecorder.startRecording()
        }
    }
    
    // 녹음 중지
    fun stopRecording() {
        viewModelScope.launch {
            audioRecorder.stopRecording()
        }
    }
    
    // 녹음 일시정지
    fun pauseRecording() {
        audioRecorder.pauseRecording()
    }
    
    // 녹음 재개
    fun resumeRecording() {
        audioRecorder.resumeRecording()
    }
    
    // 녹음 시작/중지 토글
    fun toggleRecording() {
        viewModelScope.launch {
            when (recordingState.value) {
                RecordingState.IDLE -> {
                    audioRecorder.startRecording()
                }
                RecordingState.RECORDING -> {
                    audioRecorder.stopRecording()
                }
                RecordingState.PAUSED -> {
                    audioRecorder.resumeRecording()
                }
                RecordingState.ERROR -> {
                    // 에러 처리 - 다시 시도
                    audioRecorder.startRecording()
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // ViewModel이 정리될 때 녹음 중지
        audioRecorder.stopRecording()
    }
}