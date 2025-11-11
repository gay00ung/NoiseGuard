package net.lateinit.noiseguard.core.di

import net.lateinit.noiseguard.data.ml.NoiseClassifierApi
import net.lateinit.noiseguard.data.ml.IOSNoiseClassifier
import net.lateinit.noiseguard.domain.permission.PermissionHandler
import net.lateinit.noiseguard.domain.label.LabelLocalizer
import net.lateinit.noiseguard.domain.label.IOSLabelLocalizer
import net.lateinit.noiseguard.notification.LiveUpdateController
import net.lateinit.noiseguard.notification.NoopLiveUpdateController
import org.koin.dsl.module

/**
 * iOS 플랫폼 전용 의존성 모듈
 */
val iosPlatformModule = module {
    // 권한 처리
    single { PermissionHandler() }

    // iOS: 실제 분류기(Swift 러너와 연동) 바인딩
    single<NoiseClassifierApi> { IOSNoiseClassifier() }
    // Label localizer for KO CSV from bundle
    single<LabelLocalizer> { IOSLabelLocalizer() }

    // iOS는 알림 라이브 업데이트가 없으므로 No-op 제공
    single<LiveUpdateController> { NoopLiveUpdateController }
}
