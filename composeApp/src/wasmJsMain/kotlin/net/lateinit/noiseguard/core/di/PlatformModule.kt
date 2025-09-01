package net.lateinit.noiseguard.core.di

import net.lateinit.noiseguard.data.ml.NoiseClassifierApi
import net.lateinit.noiseguard.data.ml.NoopNoiseClassifier
import net.lateinit.noiseguard.domain.permission.PermissionHandler
import org.koin.dsl.module

/**
 * Web 플랫폼 전용 의존성 모듈
 */
val wasmJsPlatformModule = module {
    // 권한 처리 (Web은 더미 구현)
    single { PermissionHandler() }

    // Web: No-op 분류기 바인딩
    single<NoiseClassifierApi> { NoopNoiseClassifier() }
}
