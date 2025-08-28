package net.lateinit.noiseguard.domain.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.lateinit.noiseguard.domain.model.NoiseLevel

/**
 * Android 플랫폼의 AudioRecorder 구현
 * Flow 기반 실시간 데시벨 측정 제공
 */
actual class AudioRecorder {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    // 기존 변수들 (호환성)
    private var currentDb = AudioConstants.MIN_DECIBEL
    private var averageDb = AudioConstants.MIN_DECIBEL
    private var peakDb = AudioConstants.MIN_DECIBEL
    private val dbValues = mutableListOf<Float>()
    
    // Flow 구현
    private val _decibelFlow = MutableSharedFlow<Float>(replay = 1)
    actual val decibelFlow: Flow<Float> = _decibelFlow.asSharedFlow()
    
    private val _noiseLevelFlow = MutableSharedFlow<NoiseLevel>(replay = 1)
    actual val noiseLevelFlow: Flow<NoiseLevel> = _noiseLevelFlow.asSharedFlow()
    
    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    actual val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    // 오디오 설정
    private val sampleRate = AudioConstants.SAMPLE_RATE
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize: Int by lazy {
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            AudioConstants.BUFFER_SIZE * 2
        } else {
            maxOf(minBufferSize, AudioConstants.BUFFER_SIZE * 2)
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    actual fun startRecording() {
        if (_recordingState.value == RecordingState.RECORDING) return
        
        try {
            // AudioRecord 초기화
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            ).apply {
                if (state != AudioRecord.STATE_INITIALIZED) {
                    _recordingState.value = RecordingState.ERROR
                    throw IllegalStateException("AudioRecord initialization failed")
                }
            }

            audioRecord?.startRecording()
            _recordingState.value = RecordingState.RECORDING
            
            // 값 초기화
            dbValues.clear()
            peakDb = AudioConstants.MIN_DECIBEL

            recordingJob = coroutineScope.launch {
                val buffer = ShortArray(bufferSize)
                var lastEmitTime = System.currentTimeMillis()

                while (isActive && _recordingState.value == RecordingState.RECORDING) {
                    val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                    
                    if (read > 0) {
                        // 데시벨 계산 (DecibelCalculator 사용)
                        val validBuffer = if (read < bufferSize) {
                            buffer.sliceArray(0 until read)
                        } else {
                            buffer
                        }
                        
                        currentDb = DecibelCalculator.samplesArrayToDecibel(validBuffer)
                        dbValues.add(currentDb)

                        // Peak 업데이트
                        if (currentDb > peakDb) {
                            peakDb = currentDb
                        }

                        // 평균 계산
                        averageDb = if (dbValues.isNotEmpty()) {
                            dbValues.takeLast(100).average().toFloat()
                        } else {
                            currentDb
                        }
                        
                        // Flow로 전송 (100ms마다)
                        val currentTime = System.currentTimeMillis()
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
                    
                    delay(10) // CPU 사용량 감소
                }
            }
        } catch (e: SecurityException) {
            println("Permission denied: RECORD_AUDIO")
            _recordingState.value = RecordingState.ERROR
        } catch (e: Exception) {
            e.printStackTrace()
            _recordingState.value = RecordingState.ERROR
            stopRecording()
        }
    }

    actual fun stopRecording() {
        _recordingState.value = RecordingState.IDLE
        
        recordingJob?.cancel()
        recordingJob = null
        
        audioRecord?.apply {
            if (state == AudioRecord.STATE_INITIALIZED) {
                stop()
                release()
            }
        }
        audioRecord = null
        
        // 값 초기화
        dbValues.clear()
        currentDb = AudioConstants.MIN_DECIBEL
        averageDb = AudioConstants.MIN_DECIBEL
        _decibelFlow.tryEmit(AudioConstants.MIN_DECIBEL)
    }
    
    actual fun pauseRecording() {
        if (_recordingState.value == RecordingState.RECORDING) {
            _recordingState.value = RecordingState.PAUSED
            audioRecord?.stop()
        }
    }
    
    actual fun resumeRecording() {
        if (_recordingState.value == RecordingState.PAUSED) {
            audioRecord?.startRecording()
            _recordingState.value = RecordingState.RECORDING
        }
    }

    // 기존 메서드들 (호환성 유지)
    actual fun getDecibelLevel(): Float = currentDb
    actual fun isRecording(): Boolean = _recordingState.value == RecordingState.RECORDING
    actual fun getAverageDecibelLevel(): Float = averageDb
    actual fun getPeakDecibelLevel(): Float = peakDb
}