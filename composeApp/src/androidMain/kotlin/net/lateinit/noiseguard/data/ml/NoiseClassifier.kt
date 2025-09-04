package net.lateinit.noiseguard.data.ml

import android.content.Context
import android.util.Log
import android.media.AudioFormat
import android.media.AudioRecord
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifier
import com.google.mediapipe.tasks.audio.core.RunningMode
import com.google.mediapipe.tasks.components.containers.AudioData
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.components.containers.ClassificationResult
import com.google.mediapipe.tasks.components.containers.Classifications
import com.google.mediapipe.tasks.core.BaseOptions
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NoiseClassifier(
    private val context: Context,
    private val modelFileName: String = "yamnet.tflite",
    private val minScore: Float = 0.05f,
) : NoiseClassifierApi {
    private lateinit var classifier: AudioClassifier
    private lateinit var audioRecord: AudioRecord
    private lateinit var audioData: AudioData

    override fun initialize() {
        val modelPath = runBlocking { ModelFileProvider.ensureModelFile(context, modelFileName) }

        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(modelPath)
            .build()

        val options = AudioClassifier.AudioClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setMaxResults(10)
            .setScoreThreshold(minScore)
            .setRunningMode(RunningMode.AUDIO_CLIPS)
            .build()

        try {
            classifier = AudioClassifier.createFromOptions(context, options)
            Log.d("NoiseClassifier", "AudioClassifier initialized successfully.")
        } catch (e: Exception) {
            Log.e("NoiseClassifier", "Failed to initialize AudioClassifier.", e)
        }
    }

    override fun startRecordingAndClassifying(onResult: (List<ClassifiedLabel>) -> Unit) {
        // MediaPipe Tasks 유틸리티로 AudioRecord 생성 및 AudioData 준비
        audioRecord = classifier.createAudioRecord()
        audioRecord.startRecording()
        val format = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
            .setSampleRate(audioRecord.sampleRate)
            .setChannelMask(if (audioRecord.channelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO)
            .build()
        // 입력 프레임 수는 AudioRecord의 버퍼 크기에 맞춰 초기화
        audioData = AudioData.create(format, audioRecord.bufferSizeInFrames)

        // 별도 스레드에서 주기적으로 분류 실행 (예: 1초마다)
        val classificationInterval = (0.5 * 1000).toLong()
        val executor = Executors.newSingleThreadScheduledExecutor()

        executor.scheduleWithFixedDelay({
            // AudioRecord에서 float PCM을 읽어 AudioData에 적재 후 분류 실행
            val len = audioData.bufferLength
            val buf = FloatArray(len)
            audioRecord.read(buf, 0, len, AudioRecord.READ_BLOCKING)
            audioData.load(buf)
            val result = classifier.classify(audioData)

            // 라벨 + 스코어를 집계하여 콜백으로 전달 (이름별 max score)
            val scoresByName = linkedMapOf<String, Float>()
            result.classificationResults().forEach { cr: ClassificationResult ->
                cr.classifications().forEach { cls: Classifications ->
                    cls.categories().forEach { c: Category ->
                        val name = c.displayName().ifEmpty { c.categoryName() }
                        val s = c.score()
                        val prev = scoresByName[name]
                        if (prev == null || s > prev) scoresByName[name] = s
                    }
                }
            }
            val idxByName = mutableMapOf<String, Int>()
            result.classificationResults().forEach { cr ->
                cr.classifications().forEach { cls ->
                    cls.categories().forEach { c ->
                        val name = c.displayName().ifEmpty { c.categoryName() }
                        val idx = try { c.index() } catch (e: Exception) { -1 }
                        if (idx >= 0 && name !in idxByName) idxByName[name] = idx
                    }
                }
            }

            val scored = scoresByName.entries
                .map { ClassifiedLabel(it.key, it.value, idxByName[it.key]) }
                .sortedByDescending { it.score }

            onResult(scored)

        }, 0, classificationInterval, TimeUnit.MILLISECONDS)
    }

    override fun stopRecording() {
        if (this::audioRecord.isInitialized) {
            audioRecord.stop()
        }
    }
}
