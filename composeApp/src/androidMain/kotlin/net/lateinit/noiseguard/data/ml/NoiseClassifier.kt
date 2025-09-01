package net.lateinit.noiseguard.data.ml

import android.content.Context
import android.media.AudioRecord
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.task.core.BaseOptions
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NoiseClassifier(
    private val context: Context,
    private val modelPath: String = "yamnet.tflite",
    private val minScore: Float = 0.3f,
) : NoiseClassifierApi {
    private lateinit var classifier: AudioClassifier
    private lateinit var audioRecord: AudioRecord
    private lateinit var audioTensor: TensorAudio

    override fun initialize() {
        val baseOptions = BaseOptions.builder().setNumThreads(4).build()
        val options = AudioClassifier.AudioClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setMaxResults(3) // 상위 3개 결과만 받기
            .setScoreThreshold(minScore)
            .build()

        classifier = AudioClassifier.createFromFileAndOptions(context, modelPath, options)
    }

    override fun startRecordingAndClassifying(onResult: (List<String>) -> Unit) {
        // Task Library에서 제공하는 유틸리티로 AudioRecord와 TensorAudio 생성
        audioTensor = classifier.createInputTensorAudio()
        audioRecord = classifier.createAudioRecord()
        audioRecord.startRecording()

        // 별도 스레드에서 주기적으로 분류 실행 (예: 1초마다)
        val classificationInterval = (0.5 * 1000).toLong()
        val executor = Executors.newSingleThreadScheduledExecutor()

        executor.scheduleWithFixedDelay({
            // 녹음된 오디오를 텐서에 로드하고 추론 실행
            audioTensor.load(audioRecord)
            val output = classifier.classify(audioTensor)

            // 결과에서 라벨 이름만 추출하여 콜백으로 전달
            val resultLabels = output.flatMap { classifications ->
                classifications.categories.map { category ->
                    category.label
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
