package net.lateinit.noiseguard.domain.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 보정/기준값 및 표시 모드 관리
 */
object CalibrationConfig {
    private val _userOffsetDb = MutableStateFlow(0f)
    val userOffsetDb: StateFlow<Float> = _userOffsetDb

    private val _baselineDb = MutableStateFlow<Float?>(null)
    val baselineDb: StateFlow<Float?> = _baselineDb

    private val _relativeDisplay = MutableStateFlow(false)
    val relativeDisplay: StateFlow<Boolean> = _relativeDisplay

    init {
        PlatformCalibrationStorage.loadOffsetOrNull()?.let {
            _userOffsetDb.value = it
            DecibelCalculator.setUserCalibrationOffset(it)
        }
        _baselineDb.value = PlatformCalibrationStorage.loadBaselineOrNull()
        _relativeDisplay.value = PlatformCalibrationStorage.loadRelativeModeOrNull() ?: false
    }

    fun setUserOffset(offsetDb: Float) {
        val clamped = offsetDb.coerceIn(-40f, 40f)
        _userOffsetDb.value = clamped
        DecibelCalculator.setUserCalibrationOffset(clamped)
        PlatformCalibrationStorage.saveOffset(clamped)
    }

    fun setBaselineTo(db: Float?) {
        _baselineDb.value = db
        PlatformCalibrationStorage.saveBaseline(db ?: Float.NaN)
    }

    fun setRelativeDisplay(enabled: Boolean) {
        _relativeDisplay.value = enabled
        PlatformCalibrationStorage.saveRelativeMode(enabled)
    }
}

/**
 * 플랫폼별 보정/기준 저장소
 */
expect object PlatformCalibrationStorage {
    fun saveOffset(offsetDb: Float)
    fun loadOffsetOrNull(): Float?
    fun saveBaseline(baselineDb: Float)
    fun loadBaselineOrNull(): Float?
    fun saveRelativeMode(enabled: Boolean)
    fun loadRelativeModeOrNull(): Boolean?
}

