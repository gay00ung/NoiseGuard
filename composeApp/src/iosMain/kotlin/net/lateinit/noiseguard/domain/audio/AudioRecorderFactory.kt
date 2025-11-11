package net.lateinit.noiseguard.domain.audio

/**
 * iOS 플랫폼의 AudioRecorder 팩토리 구현
 */
actual object AudioRecorderFactory {
    actual fun createAudioRecorder(): AudioRecorder {
        return AudioRecorder()
    }
}