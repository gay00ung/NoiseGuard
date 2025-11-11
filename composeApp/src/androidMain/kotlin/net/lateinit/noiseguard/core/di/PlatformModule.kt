package net.lateinit.noiseguard.core.di

import net.lateinit.noiseguard.data.ml.NoiseClassifier
import net.lateinit.noiseguard.data.ml.NoiseClassifierApi
import net.lateinit.noiseguard.domain.label.AndroidLabelLocalizer
import net.lateinit.noiseguard.domain.label.LabelLocalizer
import net.lateinit.noiseguard.domain.permission.PermissionHandler
import net.lateinit.noiseguard.notification.AndroidLiveUpdateController
import net.lateinit.noiseguard.notification.LiveUpdateController
import net.lateinit.noiseguard.notification.LiveUpdateNotifier
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android 플랫폼 전용 의존성 모듈
 */
val androidPlatformModule = module {
    // 권한 처리
    single { PermissionHandler(androidContext()) }

    // Android: 실제 분류기 바인딩
    single<NoiseClassifierApi> { NoiseClassifier(androidContext()) }

    // Label localizer (KO CSV)
    single<LabelLocalizer> { AndroidLabelLocalizer(androidContext()) }

    single { LiveUpdateNotifier(androidContext()) }
    single<LiveUpdateController> { AndroidLiveUpdateController(get()) }
}
