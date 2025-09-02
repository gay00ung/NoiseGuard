package net.lateinit.noiseguard.data.ml

import android.content.Context
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
    private val minScore: Float = 0.3f,
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
            .setMaxResults(3)
            .setScoreThreshold(minScore)
            .setRunningMode(RunningMode.AUDIO_CLIPS)
            .build()

        classifier = AudioClassifier.createFromOptions(context, options)
    }

    override fun startRecordingAndClassifying(onResult: (List<String>) -> Unit) {
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

            // 결과에서 라벨 이름만 추출하여 콜백으로 전달
            val resultLabels: List<String> = result.classificationResults()
                .flatMap { cr: ClassificationResult ->
                    cr.classifications()
                        .flatMap { cls: Classifications ->
                            cls.categories().map { c: Category ->
                                val dn = c.displayName()
                                dn.ifEmpty { c.categoryName() }
                            }
                        }
                }
            onResult(resultLabels)

        }, 0, classificationInterval, TimeUnit.MILLISECONDS)
    }

    override fun stopRecording() {
        if (this::audioRecord.isInitialized) {
            audioRecord.stop()
        }
    }
}
