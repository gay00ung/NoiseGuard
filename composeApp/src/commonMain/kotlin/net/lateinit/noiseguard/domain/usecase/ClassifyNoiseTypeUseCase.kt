package net.lateinit.noiseguard.domain.usecase

import net.lateinit.noiseguard.domain.model.NoiseType
import net.lateinit.noiseguard.data.ml.ClassifiedLabel

class ClassifyNoiseTypeUseCase {
    // 점수 기반 매핑: 상위 라벨의 신뢰도를 활용해 안정적인 타입 결정
    private fun mapToNoiseType(labels: List<ClassifiedLabel>): NoiseType {
        if (labels.isEmpty()) return NoiseType.UNKNOWN

        // 1) Silence 빠른 처리
        val top = labels.maxByOrNull { it.score }!!
        if (top.name.equals("silence", ignoreCase = true) && top.score >= 0.15f) {
            return NoiseType.UNKNOWN
        }

        // 2) 후보 필터링: 낮은 스코어 제거 (절대 0.12 미만 컷)
        val filtered = labels.filter { it.score >= 0.12f }
        if (filtered.isEmpty()) return NoiseType.UNKNOWN

        // 3) 키워드 매핑 정의 (tap은 물과 혼동되어 제외)
        data class Rule(val type: NoiseType, val keywords: List<String>)

        val rules = listOf(
            Rule(NoiseType.DOOR, listOf("door", "slam", "knock", "doorbell")),
            Rule(NoiseType.TYPING, listOf("typing", "keyboard", "computer keyboard", "typewriter")),
            Rule(NoiseType.FOOTSTEP, listOf("footstep", "footsteps", "walk", "running", "step", "stomp")),
            Rule(NoiseType.HAMMERING, listOf("hammer", "hammering", "impact", "bang", "thump")),
            Rule(NoiseType.DRAGGING, listOf("scrape", "drag", "chair", "furniture")),
            Rule(NoiseType.TALKING, listOf("speech", "conversation", "chatter", "babble", "crowd", "hubbub")),
            Rule(NoiseType.MUSIC, listOf("music", "singing", "musical", "instrument", "guitar", "piano", "drum", "violin")),
            Rule(NoiseType.WATER, listOf("water", "faucet", "drip", "shower", "toilet", "flush", "sink")),
            Rule(NoiseType.VACUUM, listOf("vacuum", "hoover")),
            Rule(NoiseType.TRAFFIC, listOf("vehicle", "car", "bus", "truck", "motorcycle", "traffic", "roadway")),
            Rule(NoiseType.CONSTRUCTION, listOf("drill", "saw", "jackhammer", "construction", "power tool", "sanding")),
            Rule(NoiseType.PET, listOf("dog", "bark", "cat", "meow", "pet")),
            Rule(NoiseType.BABY, listOf("baby", "infant", "crying", "cry", "toddler")),
            Rule(NoiseType.TV, listOf("television", "tv", "broadcast")),
            Rule(NoiseType.ALARM, listOf("siren", "alarm", "buzzer", "beep")),
            Rule(NoiseType.APPLIANCE, listOf("blender", "hair dryer", "fan", "mechanical fan", "microwave", "dishwasher", "washing machine", "dryer", "air conditioner", "fridge", "refrigerator", "white noise")),
        )

        // 4) 타입별 점수 집계 (라벨 스코어 합)
        val typeScores = mutableMapOf<NoiseType, Float>()
        filtered.forEach { (name, score) ->
            val lower = name.lowercase()
            rules.forEach { r ->
                if (r.keywords.any { lower.contains(it) }) {
                    typeScores[r.type] = (typeScores[r.type] ?: 0f) + score
                }
            }
        }

        if (typeScores.isEmpty()) return NoiseType.UNKNOWN

        // 5) 최고 타입 선택 + 신뢰도 게이트
        val (bestType, bestScore) = typeScores.maxByOrNull { it.value }!!
        val secondScore = typeScores.filterKeys { it != bestType }.values.maxOrNull() ?: 0f

        // 절대 문턱: 0.25 이상, 상대 문턱: 0.15 이상이면서 1.8배 이상 우위
        val pass = (bestScore >= 0.25f) || (bestScore >= 0.15f && bestScore >= secondScore * 1.8f)
        return if (pass) bestType else NoiseType.UNKNOWN
    }

    // UseCase 실행: 점수 포함 라벨 리스트를 매핑 함수에 전달
    operator fun invoke(labels: List<ClassifiedLabel>): NoiseType = mapToNoiseType(labels)
}
