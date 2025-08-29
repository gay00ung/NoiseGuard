package net.lateinit.noiseguard.core.di

import net.lateinit.noiseguard.domain.permission.PermissionHandler
import org.koin.dsl.module

/**
 * iOS 플랫폼 전용 의존성 모듈
 */
val iosPlatformModule = module {
    // 권한 처리
    single { PermissionHandler() }
}