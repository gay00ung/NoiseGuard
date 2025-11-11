package net.lateinit.noiseguard.domain.audio

/**
 * AudioRecorder 인스턴스를 생성하는 팩토리
 * 플랫폼별로 구현됨
 */
expect object AudioRecorderFactory {
    fun createAudioRecorder(): AudioRecorder
}