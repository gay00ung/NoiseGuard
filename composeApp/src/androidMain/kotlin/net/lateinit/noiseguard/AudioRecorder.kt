package net.lateinit.noiseguard

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.*
import kotlin.math.*

actual class AudioRecorder {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var isRecordingFlag = false

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var currentDb = 0f
    private var averageDb = 0f
    private var peakDb = 0f
    private val dbValues = mutableListOf<Float>()

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    actual fun startRecording() {
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        audioRecord?.startRecording()
        isRecordingFlag = true

        recordingJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ShortArray(bufferSize)

            while (isActive && isRecordingFlag) {
                val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (read > 0) {
                    currentDb = calculateDecibel(buffer)
                    dbValues.add(currentDb)

                    if (currentDb > peakDb) {
                        peakDb = currentDb
                    }

                    averageDb = dbValues.takeLast(100).average().toFloat()
                }
                delay(100) // 100ms 마다 업데이트
            }
        }
    }

    actual fun stopRecording() {
        isRecordingFlag = false
        recordingJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        dbValues.clear()
    }

    actual fun getDecibelLevel(): Float = currentDb

    actual fun isRecording(): Boolean = isRecordingFlag

    actual fun getAverageDecibelLevel(): Float = averageDb

    actual fun getPeakDecibelLevel(): Float = peakDb

    private fun calculateDecibel(buffer: ShortArray): Float {
        var sum = 0.0
        for (sample in buffer) {
            sum += sample * sample
        }

        val rms = sqrt(sum / buffer.size)
        return if (rms > 0) {
            (20 * log10(rms / 32768.0) + 90).toFloat() // 캘리브레이션 오프셋
        } else {
            -160f
        }
    }
}