package net.lateinit.noiseguard.data.ml

/**
 * 비-Android 플랫폼용 No-op 분류기.
 */
class NoopNoiseClassifier : NoiseClassifierApi {
    override fun initialize() { /* no-op */ }
    override fun startRecordingAndClassifying(onResult: (List<ClassifiedLabel>) -> Unit) { /* no-op */ }
    override fun stopRecording() { /* no-op */ }
}

