package net.lateinit.noiseguard.domain.permission

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined
import kotlin.coroutines.resume

/**
 * iOS 플랫폼의 권한 처리 구현
 */
actual class PermissionHandler {
    
    actual fun hasAudioPermission(): Boolean {
        // iOS의 권한 상태 확인
        return when (AVAudioSession.sharedInstance().recordPermission) {
            AVAudioSessionRecordPermissionGranted -> true
            AVAudioSessionRecordPermissionDenied -> false
            AVAudioSessionRecordPermissionUndetermined -> false
            else -> false
        }
    }
    
    actual suspend fun requestAudioPermission(): Boolean {
        // 이미 권한이 있으면 true 반환
        if (hasAudioPermission()) {
            return true
        }
        
        // 권한 요청 (콜백 기반을 코루틴으로 변환)
        return suspendCancellableCoroutine { continuation ->
            AVAudioSession.sharedInstance().requestRecordPermission { granted ->
                continuation.resume(granted)
            }
        }
    }
}