package net.lateinit.noiseguard.domain.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import net.lateinit.noiseguard.core.util.getCurrentTimeMillis
import net.lateinit.noiseguard.domain.model.NoiseLevel
import platform.AVFAudio.AVAudioQualityMax
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTimer
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual class AudioRecorder {
    private var audioRecorder: AVAudioRecorder? = null
    private val audioSession = AVAudioSession.sharedInstance()
    private var meteringTimer: NSTimer? = null

    private var currentDb = AudioConstants.MIN_DECIBEL
    private var dbValues = mutableListOf<Float>()
    private var peakDb = AudioConstants.MIN_DECIBEL
    private var averageDb = AudioConstants.MIN_DECIBEL
    private var lastEmitTime = 0L
    
    // Flow 구현
    private val _decibelFlow = MutableSharedFlow<Float>(replay = 1)
    actual val decibelFlow: Flow<Float> = _decibelFlow.asSharedFlow()
    
    private val _noiseLevelFlow = MutableSharedFlow<NoiseLevel>(replay = 1)
    actual val noiseLevelFlow: Flow<NoiseLevel> = _noiseLevelFlow.asSharedFlow()
    
    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    actual val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    init {
        setupAudioSession()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun setupAudioSession() {
        audioSession.setCategory(
            category = AVAudioSessionCategoryRecord,
            error = null
        )
        audioSession.setActive(true, null)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun startRecording() {
        val documentsPath = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).first() as String

        val audioFilePath = "$documentsPath/temp_recording.m4a"
        val audioFileURL = NSURL.fileURLWithPath(audioFilePath)

        val settings = mapOf<Any?, Any?>(
            AVFormatIDKey to kAudioFormatMPEG4AAC,
            AVSampleRateKey to 44100.0,
            AVNumberOfChannelsKey to 1,
            AVEncoderAudioQualityKey to AVAudioQualityMax
        )

        audioRecorder = AVAudioRecorder(
            uRL = audioFileURL,
            settings = settings,
            error = null
        )

        audioRecorder?.apply {
            setMeteringEnabled(true)
            record()
            _recordingState.value = RecordingState.RECORDING

            // 모니터링 시작
            startMetering()
        }
    }

    actual fun stopRecording() {
        _recordingState.value = RecordingState.IDLE
        // 타이머 중단 및 정리
        meteringTimer?.invalidate()
        meteringTimer = null

        audioRecorder?.stop()
        audioRecorder = null
        dbValues.clear()
        _decibelFlow.tryEmit(AudioConstants.MIN_DECIBEL)
        _noiseLevelFlow.tryEmit(
            NoiseLevel(
                current = AudioConstants.MIN_DECIBEL,
                average = AudioConstants.MIN_DECIBEL,
                peak = AudioConstants.MIN_DECIBEL,
                timestamp = getCurrentTimeMillis()
            )
        )
    }
    
    actual fun pauseRecording() {
        if (_recordingState.value == RecordingState.RECORDING) {
            _recordingState.value = RecordingState.PAUSED
            audioRecorder?.pause()
            // 일시정지 시 타이머 즉시 중단
            meteringTimer?.invalidate()
            meteringTimer = null
        }
    }
    
    actual fun resumeRecording() {
        if (_recordingState.value == RecordingState.PAUSED) {
            audioRecorder?.record()
            _recordingState.value = RecordingState.RECORDING
            // 재개 시 메터링 재시작
            startMetering()
        }
    }

    actual fun getDecibelLevel(): Float {
        audioRecorder?.updateMeters()
        val db = audioRecorder?.averagePowerForChannel(0u) ?: -160.0f
        currentDb = db
        return currentDb
    }

    actual fun isRecording(): Boolean = _recordingState.value == RecordingState.RECORDING

    actual fun getAverageDecibelLevel(): Float {
        if (dbValues.isEmpty()) return -160.0f
        return dbValues.takeLast(100).average().toFloat()
    }

    actual fun getPeakDecibelLevel(): Float = peakDb

    private fun startMetering() {
        // 기존 타이머가 있으면 정리
        meteringTimer?.invalidate()
        meteringTimer = null

        // 간단한 타이머로 주기적으로 레벨 측정
        // 더 자주 측정(0.1초)하되, 실제 emit은 UPDATE_INTERVAL_MS로 제한
        lastEmitTime = 0L
        meteringTimer = NSTimer.scheduledTimerWithTimeInterval(
            interval = 0.1,
            repeats = true
        ) { timer ->
            if (_recordingState.value != RecordingState.RECORDING) {
                timer?.invalidate()
                return@scheduledTimerWithTimeInterval
            }
            
            audioRecorder?.updateMeters()
            val db = audioRecorder?.averagePowerForChannel(0u) ?: -160.0f

            if (db > -160.0f) {
                currentDb = db
                dbValues.add(db)

                if (db > peakDb) {
                    peakDb = db
                }

                // 최대 1000개만 유지
                if (dbValues.size > 1000) {
                    dbValues.removeAt(0)
                }

                // 평균 계산 (최근 100개 기준)
                averageDb = if (dbValues.isNotEmpty()) {
                    dbValues.takeLast(100).average().toFloat()
                } else {
                    currentDb
                }

                // 공통 상수 주기에 맞춰 Flow로 emit
                val currentTime = getCurrentTimeMillis()
                if (currentTime - lastEmitTime >= AudioConstants.UPDATE_INTERVAL_MS) {
                    _decibelFlow.tryEmit(currentDb)
                    _noiseLevelFlow.tryEmit(
                        NoiseLevel(
                            current = currentDb,
                            average = averageDb,
                            peak = peakDb,
                            timestamp = currentTime
                        )
                    )
                    lastEmitTime = currentTime
                }
            }
        }
    }
}
