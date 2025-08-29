package net.lateinit.noiseguard.core.di

import net.lateinit.noiseguard.domain.permission.PermissionHandler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android 플랫폼 전용 의존성 모듈
 */
val androidPlatformModule = module {
    // 권한 처리
    single { PermissionHandler(androidContext()) }
}