package net.lateinit.noiseguard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import net.lateinit.noiseguard.core.util.getCurrentTimeMillis
import net.lateinit.noiseguard.domain.audio.AudioRecorderFactory
import net.lateinit.noiseguard.domain.audio.CalibrationConfig
import net.lateinit.noiseguard.domain.audio.RecordingState
import net.lateinit.noiseguard.domain.model.NoiseLevel
import net.lateinit.noiseguard.domain.model.NoiseType
import net.lateinit.noiseguard.domain.permission.PermissionHandler
import net.lateinit.noiseguard.domain.usecase.ClassifyNoiseTypeUseCase
import net.lateinit.noiseguard.data.ml.NoiseClassifierApi
import net.lateinit.noiseguard.data.ml.ClassifiedLabel
import net.lateinit.noiseguard.domain.label.LabelLocalizer
import net.lateinit.noiseguard.notification.LiveUpdateController
import kotlin.math.ceil

class HomeViewModel(
    private val permissionHandler: PermissionHandler,
    private val noiseClassifier: NoiseClassifierApi,
    private val classifyNoiseType: ClassifyNoiseTypeUseCase,
    private val labelLocalizer: LabelLocalizer,
    private val liveUpdateController: LiveUpdateController
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

    private val _baselineCalibrationState = MutableStateFlow<BaselineCalibrationState>(BaselineCalibrationState.Idle)
    val baselineCalibrationState: StateFlow<BaselineCalibrationState> = _baselineCalibrationState

    private val _countdownSeconds = MutableStateFlow<Long?>(null)
    val countdownSeconds: StateFlow<Long?> = _countdownSeconds

    private var classifierInitialized = false
    private var classificationRunning = false
    private var autoStopJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.Default) {
            runCatching { 
                println("NoiseClassifier initializing...")
                noiseClassifier.initialize() 
            }
                .onSuccess { 
                    classifierInitialized = true 
                    println("NoiseClassifier initialized successfully.")
                    if (recordingState.value == RecordingState.RECORDING) {
                        startClassificationIfNeeded()
                    }
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
        viewModelScope.launch {
            CalibrationConfig.autoTimerEnabled.collect { enabled ->
                if (!enabled) {
                    cancelAutoStopJob()
                } else if (recordingState.value == RecordingState.RECORDING) {
                    scheduleAutoStopIfNeeded()
                }
            }
        }
        viewModelScope.launch {
            combine(
                CalibrationConfig.autoTimerMin,
                CalibrationConfig.autoTimerSec
            ) { min, sec -> min to sec }
                .collect {
                    if (CalibrationConfig.autoTimerEnabled.value &&
                        recordingState.value == RecordingState.RECORDING
                    ) {
                        scheduleAutoStopIfNeeded()
                    }
                }
        }
    }

    // 녹음 시작
    fun startRecording() {
        viewModelScope.launch {
            audioRecorder.startRecording()
            scheduleAutoStopIfNeeded()
        }
    }
    
    // 녹음 중지
    fun stopRecording() {
        viewModelScope.launch {
            cancelAutoStopJob()
            audioRecorder.stopRecording()
        }
    }
    
    // 녹음 일시정지
    fun pauseRecording() {
        cancelAutoStopJob()
        audioRecorder.pauseRecording()
    }
    
    // 녹음 재개
    fun resumeRecording() {
        viewModelScope.launch {
            audioRecorder.resumeRecording()
            scheduleAutoStopIfNeeded()
        }
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
                    scheduleAutoStopIfNeeded()
                }
                RecordingState.RECORDING -> {
                    cancelAutoStopJob()
                    audioRecorder.stopRecording()
                }
                RecordingState.PAUSED -> {
                    audioRecorder.resumeRecording()
                    scheduleAutoStopIfNeeded()
                }
                RecordingState.ERROR -> {
                    // 에러 처리 - 다시 시도
                    audioRecorder.startRecording()
                    scheduleAutoStopIfNeeded()
                }
            }
        }
    }
    
    private fun scheduleAutoStopIfNeeded() {
        autoStopJob?.cancel()
        if (!CalibrationConfig.autoTimerEnabled.value) {
            _countdownSeconds.value = null
            liveUpdateController.cancel()
            return
        }

        val totalSeconds = CalibrationConfig.autoTimerMin.value * 60 + CalibrationConfig.autoTimerSec.value
        if (totalSeconds <= 0L) {
            _countdownSeconds.value = null
            liveUpdateController.cancel()
            return
        }

        val job = viewModelScope.launch {
            val stopAt = getCurrentTimeMillis() + totalSeconds * 1000
            liveUpdateController.start(totalSeconds)
            _countdownSeconds.value = totalSeconds
            liveUpdateController.update(
                totalSeconds,
                displayDecibel(currentDecibel.value)
            )

            var lastReported = totalSeconds
            while (isActive) {
                val remainingMillis = (stopAt - getCurrentTimeMillis()).coerceAtLeast(0)
                val remainingSeconds = ceil(remainingMillis / 1000.0).toLong()

                if (remainingSeconds != lastReported) {
                    lastReported = remainingSeconds
                    _countdownSeconds.value = remainingSeconds.takeIf { it >= 0 }
                    liveUpdateController.update(
                        remainingSeconds.coerceAtLeast(0),
                        displayDecibel(currentDecibel.value)
                    )
                }

                if (remainingMillis <= 0) break
                delay(250L)
            }

            if (!isActive) return@launch
            liveUpdateController.complete()
            _countdownSeconds.value = null
            audioRecorder.stopRecording()
        }

        job.invokeOnCompletion { cause ->
            if (cause != null) {
                liveUpdateController.cancel()
            }
            _countdownSeconds.value = null
            if (autoStopJob === job) {
                autoStopJob = null
            }
        }

        autoStopJob = job
    }

    private fun cancelAutoStopJob() {
        autoStopJob?.cancel()
        autoStopJob = null
        _countdownSeconds.value = null
        liveUpdateController.cancel()
    }

    private fun displayDecibel(rawDb: Float): Float {
        val baseline = CalibrationConfig.baselineDb.value
        val useRelative = CalibrationConfig.relativeDisplay.value
        return if (useRelative && baseline != null) {
            (rawDb - baseline).coerceAtLeast(0f)
        } else {
            rawDb
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


    fun startBaselineCalibration(durationMillis: Long = 5_000L) {
        if (_baselineCalibrationState.value is BaselineCalibrationState.InProgress) return

        viewModelScope.launch {
            if (!permissionHandler.hasAudioPermission()) {
                val granted = permissionHandler.requestAudioPermission()
                if (!granted) {
                    _baselineCalibrationState.value = BaselineCalibrationState.Failed("마이크 권한이 필요해요.")
                    return@launch
                }
            }

            val readings = mutableListOf<Float>()
            val wasRecording = recordingState.value == RecordingState.RECORDING
            _baselineCalibrationState.value = BaselineCalibrationState.InProgress(progress = 0f)

            try {
                if (!wasRecording) {
                    audioRecorder.startRecording()
                }

                val startTime = getCurrentTimeMillis()

                withContext(Dispatchers.Default) {
                    try {
                        withTimeout(durationMillis) {
                            audioRecorder.decibelFlow
                                .onEach { value ->
                                    if (!value.isFinite()) return@onEach
                                    readings.add(value)
                                    val elapsed = (getCurrentTimeMillis() - startTime).coerceAtLeast(0L)
                                    val progress = (elapsed.toFloat() / durationMillis).coerceIn(0f, 1f)
                                    _baselineCalibrationState.value = BaselineCalibrationState.InProgress(progress)
                                }
                                .collect()
                        }
                    } catch (_: TimeoutCancellationException) {
                        // Expected after duration elapsed
                    }
                }

                _baselineCalibrationState.value = BaselineCalibrationState.InProgress(progress = 1f)

                val averaged = readings.ifEmpty { null }?.average()?.toFloat()
                if (averaged != null && averaged.isFinite()) {
                    CalibrationConfig.setBaselineTo(averaged)
                    _baselineCalibrationState.value = BaselineCalibrationState.Completed(averaged)
                } else {
                    _baselineCalibrationState.value = BaselineCalibrationState.Failed(message = null)
                }
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                _baselineCalibrationState.value = BaselineCalibrationState.Failed(message = t.message)
            } finally {
                if (!wasRecording) {
                    audioRecorder.stopRecording()
                }
            }
        }
    }

    fun resetBaselineCalibrationState() {
        if (_baselineCalibrationState.value !is BaselineCalibrationState.InProgress) {
            _baselineCalibrationState.value = BaselineCalibrationState.Idle
        }
    }


    override fun onCleared() {
        super.onCleared()
        // ViewModel이 정리될 때 녹음 중지
        audioRecorder.stopRecording()
    }
}

sealed interface BaselineCalibrationState {
    object Idle : BaselineCalibrationState
    data class InProgress(val progress: Float) : BaselineCalibrationState
    data class Completed(val baselineDb: Float) : BaselineCalibrationState
    data class Failed(val message: String?) : BaselineCalibrationState
}
