package net.lateinit.noiseguard.domain.usecase

import net.lateinit.noiseguard.domain.model.NoiseType

class ClassifyNoiseTypeUseCase {
    // YAMNet 라벨을 앱의 NoiseType으로 매핑
    private fun mapToNoiseType(yamnetLabels: List<String>): NoiseType {
        if (yamnetLabels.isEmpty()) return NoiseType.UNKNOWN

        // 우선순위에 따라 매핑
        return when {
            yamnetLabels.any { it in listOf("Hammer", "Knock") } -> NoiseType.HAMMERING
            yamnetLabels.any { it in listOf("Footsteps", "Walk", "Running") } -> NoiseType.FOOTSTEP
            yamnetLabels.any { it in listOf("Speech", "Conversation", "Chatter") } -> NoiseType.TALKING
            yamnetLabels.any { it in listOf("Music", "Singing") } -> NoiseType.MUSIC
            yamnetLabels.any { it in listOf("Door", "Slam") } -> NoiseType.DOOR
            yamnetLabels.any { it in listOf("Water", "Faucet") } -> NoiseType.WATER
            yamnetLabels.any { it.contains("scrape", ignoreCase = true) || it.contains("drag", ignoreCase = true) } -> NoiseType.DRAGGING
            else -> NoiseType.UNKNOWN
        }
    }

    // UseCase 실행 시, Classifier를 통해 얻은 라벨 리스트를 매핑 함수에 전달
    operator fun invoke(yamnetLabels: List<String>): NoiseType {
        return mapToNoiseType(yamnetLabels)
    }
}