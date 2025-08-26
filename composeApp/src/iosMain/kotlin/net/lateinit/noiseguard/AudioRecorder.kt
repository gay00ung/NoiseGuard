package net.lateinit.noiseguard

import platform.Foundation.*
import platform.AVFAudio.*
import kotlinx.cinterop.*
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC

actual class AudioRecorder {
    private var audioRecorder: AVAudioRecorder? = null
    private val audioSession = AVAudioSession.sharedInstance()

    private var currentDb = -160.0f
    private var dbValues = mutableListOf<Float>()
    private var peakDb = -160.0f
    private var isRecordingFlag = false

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
            isRecordingFlag = true

            // 모니터링 시작
            startMetering()
        }
    }

    actual fun stopRecording() {
        isRecordingFlag = false
        audioRecorder?.stop()
        audioRecorder = null
        dbValues.clear()
    }

    actual fun getDecibelLevel(): Float {
        audioRecorder?.updateMeters()
        val db = audioRecorder?.averagePowerForChannel(0u) ?: -160.0f
        currentDb = db
        return currentDb
    }

    actual fun isRecording(): Boolean = isRecordingFlag

    actual fun getAverageDecibelLevel(): Float {
        if (dbValues.isEmpty()) return -160.0f
        return dbValues.takeLast(100).average().toFloat()
    }

    actual fun getPeakDecibelLevel(): Float = peakDb

    private fun startMetering() {
        // 간단한 타이머로 주기적으로 레벨 측정
        NSTimer.scheduledTimerWithTimeInterval(
            interval = 0.1,
            repeats = true
        ) { timer ->
            if (!isRecordingFlag) {
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
            }
        }
    }
}