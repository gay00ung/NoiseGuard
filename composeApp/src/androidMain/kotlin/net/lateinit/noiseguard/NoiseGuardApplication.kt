package net.lateinit.noiseguard

import android.app.Application
import net.lateinit.noiseguard.core.di.androidPlatformModule
import net.lateinit.noiseguard.core.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Android Application 클래스
 * Koin DI 초기화를 담당
 */
class NoiseGuardApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Koin 초기화
        startKoin {
            // Android context 제공
            androidContext(this@NoiseGuardApplication)
            
            // 로거 설정 (디버그 모드에서만)
            androidLogger()
            
            // 모듈 로드
            modules(appModules() + androidPlatformModule)
        }
    }
}