package net.lateinit.noiseguard.domain.model

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.*
import net.lateinit.noiseguard.core.util.getCurrentTimeMillis
import net.lateinit.noiseguard.presentation.theme.Caution
import net.lateinit.noiseguard.presentation.theme.Danger
import net.lateinit.noiseguard.presentation.theme.Safe
import net.lateinit.noiseguard.presentation.theme.Warning

data class NoiseLevel(
    val current: Float,
    val average: Float,
    val peak: Float,
    val timestamp: Long = getCurrentTimeMillis()
)

data class NoiseRecord(
    val id: String,
    val timestamp: Long,
    val decibelLevel: Float,
    val averageDb: Float,
    val peakDb: Float,
    val duration: Long,
    val location: String? = null,
    val noiseType: NoiseType = NoiseType.UNKNOWN,
    val audioFilePath: String? = null,
    val note: String? = null
)

enum class NoiseType {
    FOOTSTEP, HAMMERING, DRAGGING, MUSIC, TALKING, DOOR, WATER, UNKNOWN
}

enum class NoiseStatus(val minDb: Float, val color: Color, val label: String) {
    SAFE(0f, Safe, "안전"),
    CAUTION(40f, Warning, "주의"),
    WARNING(50f, Caution, "경고"),
    DANGER(57f, Danger, "위험");

    companion object {
        fun fromDecibel(db: Float) = NoiseStatus.entries.toTypedArray().lastOrNull { db >= it.minDb } ?: SAFE
    }
}

data class DailyStats(
    val date: LocalDate,
    val avgDecibel: Float,
    val maxDecibel: Float,
    val violationCount: Int,
    val totalMeasurements: Int
)