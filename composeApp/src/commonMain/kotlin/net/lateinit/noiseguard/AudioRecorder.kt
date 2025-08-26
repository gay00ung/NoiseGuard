package net.lateinit.noiseguard


expect class AudioRecorder {
    fun startRecording()
    fun stopRecording()
    fun getDecibelLevel(): Float
    fun isRecording(): Boolean
    fun getAverageDecibelLevel(): Float
    fun getPeakDecibelLevel(): Float
}

data class NoiseLevel(
    val current: Float,
    val average: Float,
    val peak: Float,
    val timeStamp: Long
)