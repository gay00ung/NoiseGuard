package net.lateinit.noiseguard

import kotlinx.browser.window

actual class AudioRecorder {
    actual fun startRecording() {
        window.alert("웹 브라우저에서는 녹음 기능을 사용할 수 없습니다.\n모바일 앱을 이용해주세요.")
    }

    actual fun stopRecording() {
        // 웹에서는 동작 없음
    }

    actual fun getDecibelLevel(): Float = 0f

    actual fun isRecording(): Boolean = false

    actual fun getAverageDecibelLevel(): Float = 0f

    actual fun getPeakDecibelLevel(): Float = 0f
}