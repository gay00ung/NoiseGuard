package net.lateinit.noiseguard.domain.audio

import android.content.Context
import android.content.SharedPreferences
import net.lateinit.noiseguard.NoiseGuardApplication

actual object PlatformCalibrationStorage {
    private const val PREFS_NAME = "noiseguard_prefs"
    private const val KEY_OFFSET_DB = "calibration_offset_db"
    private const val KEY_BASELINE_DB = "baseline_db"
    private const val KEY_RELATIVE_MODE = "relative_mode"

    private fun prefs(): SharedPreferences {
        val ctx: Context = NoiseGuardApplication.appContext
            ?: throw IllegalStateException("Application context not initialized")
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual fun saveOffset(offsetDb: Float) {
        prefs().edit().putFloat(KEY_OFFSET_DB, offsetDb).apply()
    }

    actual fun loadOffsetOrNull(): Float? {
        val p = prefs()
        return if (p.contains(KEY_OFFSET_DB)) p.getFloat(KEY_OFFSET_DB, 0f) else null
    }

    actual fun saveBaseline(baselineDb: Float) {
        val e = prefs().edit()
        if (baselineDb.isNaN()) {
            e.remove(KEY_BASELINE_DB).apply()
        } else {
            e.putFloat(KEY_BASELINE_DB, baselineDb).apply()
        }
    }

    actual fun loadBaselineOrNull(): Float? {
        val p = prefs()
        return if (p.contains(KEY_BASELINE_DB)) p.getFloat(KEY_BASELINE_DB, 0f) else null
    }

    actual fun saveRelativeMode(enabled: Boolean) {
        prefs().edit().putBoolean(KEY_RELATIVE_MODE, enabled).apply()
    }

    actual fun loadRelativeModeOrNull(): Boolean? {
        val p = prefs()
        return if (p.contains(KEY_RELATIVE_MODE)) p.getBoolean(KEY_RELATIVE_MODE, false) else null
    }
}

