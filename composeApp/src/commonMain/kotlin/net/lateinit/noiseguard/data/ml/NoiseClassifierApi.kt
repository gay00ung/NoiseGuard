package net.lateinit.noiseguard.data.ml

/**
 * 플랫폼 독립적인 소음 분류기 인터페이스.
 * Android에서는 TFLite 기반 구현을, 기타 플랫폼에서는 No-op 구현을 주입합니다.
 */
interface NoiseClassifierApi {
    fun initialize()
    fun startRecordingAndClassifying(onResult: (List<String>) -> Unit)
    fun stopRecording()
}

