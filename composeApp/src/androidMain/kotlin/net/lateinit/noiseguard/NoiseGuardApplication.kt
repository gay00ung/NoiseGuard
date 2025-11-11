package net.lateinit.noiseguard

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import net.lateinit.noiseguard.core.di.androidPlatformModule
import net.lateinit.noiseguard.core.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import net.lateinit.noiseguard.domain.audio.PlatformCalibrationStorage
import net.lateinit.noiseguard.domain.audio.DecibelCalculator

/**
 * Android Application 클래스
 * Koin DI 초기화를 담당
 */
const val LIVE_CH_ID = "live_update_channel"

class NoiseGuardApplication : Application() {
    companion object {
        @Volatile
        var appContext: Application? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("NoiseGuardApp", "<<<<< APPLICATION onCreate START >>>>>")
        appContext = this
        
        // Koin 초기화
        startKoin {
            // Android context 제공
            androidContext(this@NoiseGuardApplication)
            
            // 로거 설정 (디버그 모드에서만)
            androidLogger()
            
            // 모듈 로드
            modules(appModules() + androidPlatformModule)
        }

        // 저장된 사용자 보정 즉시 반영
        PlatformCalibrationStorage.loadOffsetOrNull()?.let { saved ->
            DecibelCalculator.setUserCalibrationOffset(saved)
        }

        createLiveUpdateChannel()
    }

    /**
     * 실시간 소음 업데이트 알림 채널 생성
     *
     */
    private fun createLiveUpdateChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                LIVE_CH_ID,
                "Live Noise Update",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Channel for live noise level updates" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
