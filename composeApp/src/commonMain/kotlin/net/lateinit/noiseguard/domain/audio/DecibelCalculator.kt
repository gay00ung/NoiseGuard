package net.lateinit.noiseguard.domain.audio

import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 데시벨 계산을 위한 유틸리티 객체
 * 오디오 샘플 데이터를 데시벨 단위로 변환하는 기능 제공
 */
object DecibelCalculator {
    // 기준 오프셋(dB)과 사용자 보정 오프셋(dB)
    private var baseOffsetDb: Float = 90f
    private var userOffsetDb: Float = 0f

    fun setBaseOffset(offsetDb: Float) { baseOffsetDb = offsetDb }
    fun setUserCalibrationOffset(offsetDb: Float) { userOffsetDb = offsetDb }
    fun getUserCalibrationOffset(): Float = userOffsetDb
    
    /**
     * 오디오 샘플의 RMS(Root Mean Square) 값을 계산
     * @param samples Float 배열 형태의 오디오 샘플 (-1.0 ~ 1.0 범위)
     * @return RMS 값
     */
    fun calculateRMS(samples: FloatArray): Float {
        if (samples.isEmpty()) return 0f
        
        var sum = 0.0
        samples.forEach { sample ->
            sum += sample * sample
        }
        
        return sqrt(sum / samples.size).toFloat()
    }
    
    /**
     * 16비트 PCM 샘플의 RMS 값을 계산 (Android용)
     * @param samples Short 배열 형태의 오디오 샘플 (-32768 ~ 32767 범위)
     * @return RMS 값
     */
    fun calculateRMS(samples: ShortArray): Float {
        if (samples.isEmpty()) return 0f
        
        var sum = 0.0
        samples.forEach { sample ->
            val normalized = sample / 32768.0 // Short를 -1.0 ~ 1.0으로 정규화
            sum += normalized * normalized
        }
        
        return sqrt(sum / samples.size).toFloat()
    }
    
    /**
     * RMS 값을 데시벨로 변환
     * @param rms RMS 값 (0.0 ~ 1.0 범위)
     * @return 데시벨 값 (dB)
     */
    fun convertToDecibel(rms: Float): Float {
        if (rms <= 0) return AudioConstants.MIN_DECIBEL

        // dBFS 계산 후 보정 오프셋 적용
        val dbFs = 20f * log10(rms)
        val calibratedDb = dbFs + baseOffsetDb + userOffsetDb

        return max(AudioConstants.MIN_DECIBEL, min(AudioConstants.MAX_DECIBEL, calibratedDb))
    }
    
    /**
     * 16비트 PCM 샘플 배열을 직접 데시벨로 변환 (Android 전용)
     * @param samples Short 배열 형태의 오디오 샘플
     * @return 데시벨 값 (dB)
     */
    fun samplesArrayToDecibel(samples: ShortArray): Float {
        val rms = calculateRMS(samples)
        return convertToDecibel(rms)
    }
    
    /**
     * Float 샘플 배열을 직접 데시벨로 변환 (iOS 전용)
     * @param samples Float 배열 형태의 오디오 샘플
     * @return 데시벨 값 (dB)
     */
    fun samplesArrayToDecibel(samples: FloatArray): Float {
        val rms = calculateRMS(samples)
        return convertToDecibel(rms)
    }
    
    /**
     * 데시벨 값을 0~100 범위로 정규화 (UI 표시용)
     * @param db 데시벨 값
     * @return 0~100 범위의 정규화된 값
     */
    fun normalizeDecibel(db: Float): Float {
        val clampedDb = max(AudioConstants.MIN_DECIBEL, min(AudioConstants.MAX_DECIBEL, db))
        return ((clampedDb - AudioConstants.MIN_DECIBEL) / 
                (AudioConstants.MAX_DECIBEL - AudioConstants.MIN_DECIBEL)) * 100f
    }
    
    /**
     * 데시벨 레벨을 사용자 친화적인 설명으로 변환
     * @param db 데시벨 값
     * @return 소음 수준 분류
     */
    fun getNoiseLevelCategory(db: Float): NoiseLevelCategory {
        return when {
            db < 40 -> NoiseLevelCategory.QUIET
            db < 50 -> NoiseLevelCategory.MODERATE
            db < 60 -> NoiseLevelCategory.NORMAL
            db < 70 -> NoiseLevelCategory.LOUD
            db < 80 -> NoiseLevelCategory.VERY_LOUD
            db < 90 -> NoiseLevelCategory.DANGEROUS
            else -> NoiseLevelCategory.HARMFUL
        }
    }
    
    /**
     * 평균 데시벨 계산
     * @param decibelValues 데시벨 값들의 리스트
     * @return 평균 데시벨
     */
    fun calculateAverageDecibel(decibelValues: List<Float>): Float {
        if (decibelValues.isEmpty()) return AudioConstants.MIN_DECIBEL
        
        // 데시벨은 로그 스케일이므로 단순 평균이 아닌 에너지 평균 계산
        var sumEnergy = 0.0
        decibelValues.forEach { db ->
            val energy = 10.0.pow(db / 10.0)
            sumEnergy += energy
        }
        
        val avgEnergy = sumEnergy / decibelValues.size
        return (10 * log10(avgEnergy)).toFloat()
    }
}

/**
 * 오디오 관련 상수 정의
 */
object AudioConstants {
    const val SAMPLE_RATE = 44100 // 샘플링 레이트 (Hz)
    const val BUFFER_SIZE = 1024 // 기본 버퍼 크기
    const val MIN_DECIBEL = 0f // 최소 데시벨
    const val MAX_DECIBEL = 130f // 최대 데시벨 (통증 역치)
    const val REFERENCE_AMPLITUDE = 32768.0f // 16비트 오디오 최대값
    const val UPDATE_INTERVAL_MS = 1000L // UI 업데이트 주기 (밀리초)
}

/**
 * 소음 수준 분류 열거형
 */
enum class NoiseLevelCategory(val description: String, val koreanDesc: String) {
    QUIET("Quiet", "조용함"),
    MODERATE("Moderate", "보통"),
    NORMAL("Normal", "일상적"),
    LOUD("Loud", "시끄러움"),
    VERY_LOUD("Very Loud", "매우 시끄러움"),
    DANGEROUS("Dangerous", "위험 수준"),
    HARMFUL("Harmful", "청력 손상 위험")
}
