package net.lateinit.noiseguard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.lateinit.noiseguard.core.util.getCurrentTimeMillis
import net.lateinit.noiseguard.domain.audio.AudioRecorderFactory
import net.lateinit.noiseguard.domain.audio.RecordingState
import net.lateinit.noiseguard.domain.model.NoiseLevel
import net.lateinit.noiseguard.domain.model.NoiseType
import net.lateinit.noiseguard.domain.permission.PermissionHandler
import net.lateinit.noiseguard.domain.usecase.ClassifyNoiseTypeUseCase
import net.lateinit.noiseguard.data.ml.NoiseClassifierApi
import net.lateinit.noiseguard.data.ml.ClassifiedLabel
import net.lateinit.noiseguard.domain.label.LabelLocalizer

class HomeViewModel(
    private val permissionHandler: PermissionHandler,
    private val noiseClassifier: NoiseClassifierApi,
    private val classifyNoiseType: ClassifyNoiseTypeUseCase,
    private val labelLocalizer: LabelLocalizer
) : ViewModel() {
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

    private val _noiseType = MutableStateFlow(NoiseType.UNKNOWN)
    val noiseType: StateFlow<NoiseType> = _noiseType

    private val _topLabels = MutableStateFlow<List<String>>(emptyList())
    val topLabels: StateFlow<List<String>> = _topLabels

    private var classifierInitialized = false
    private var classificationRunning = false

    init {
        viewModelScope.launch(Dispatchers.Default) {
            runCatching { 
                println("NoiseClassifier initializing...")
                noiseClassifier.initialize() 
            }
                .onSuccess { 
                    classifierInitialized = true 
                    println("NoiseClassifier initialized successfully.")
                }
                .onFailure { e ->
                    println("NoiseClassifier initialization failed: ${e.message}")
                    e.printStackTrace()
                }
        }
        viewModelScope.launch {
            recordingState.collect { state ->
                when (state) {
                    RecordingState.RECORDING -> startClassificationIfNeeded()
                    else -> stopClassificationIfRunning()
                }
            }
        }
    }

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
            // 권한 체크
            if (!permissionHandler.hasAudioPermission()) {
                val granted = permissionHandler.requestAudioPermission()
                if (!granted) {
                    // 권한 거부됨
                    println("❌ 오디오 녹음 권한이 필요합니다")
                    return@launch
                }
            }
            
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

    private fun startClassificationIfNeeded() {
        if (!classifierInitialized || classificationRunning) return
        classificationRunning = true
        noiseClassifier.startRecordingAndClassifying { labels: List<ClassifiedLabel> ->
            val type = classifyNoiseType(labels)
            val names = labels.take(3).map { labelLocalizer.localize(it.index, it.name) }
            println("[NoiseGuard] Mapped noise type: $type from labels: $names")
            viewModelScope.launch {
                _noiseType.emit(type)
                _topLabels.emit(names)
            }
        }
    }

    private fun stopClassificationIfRunning() {
        if (!classificationRunning) return
        classificationRunning = false
        noiseClassifier.stopRecording()
        _noiseType.value = NoiseType.UNKNOWN
    }


    override fun onCleared() {
        super.onCleared()
        // ViewModel이 정리될 때 녹음 중지
        audioRecorder.stopRecording()
    }
}
