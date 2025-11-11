package net.lateinit.noiseguard.domain.audio

actual object PlatformCalibrationStorage {
    actual fun saveOffset(offsetDb: Float) { /* no-op for now */ }
    actual fun loadOffsetOrNull(): Float? = null
    actual fun saveBaseline(baselineDb: Float) { /* no-op for now */ }
    actual fun loadBaselineOrNull(): Float? = null
    actual fun saveRelativeMode(enabled: Boolean) { /* no-op for now */ }
    actual fun loadRelativeModeOrNull(): Boolean? = null
}

