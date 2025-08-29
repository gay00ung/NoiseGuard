package net.lateinit.noiseguard.domain.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Android 플랫폼의 권한 처리 구현
 */
actual class PermissionHandler(private val context: Context) {
    
    actual fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    actual suspend fun requestAudioPermission(): Boolean {
        // 이미 권한이 있으면 true 반환
        if (hasAudioPermission()) {
            return true
        }
        
        // PermissionRequester를 통해 Activity에서 권한 요청
        val requester = PermissionRequesterHolder.requester
        if (requester != null) {
            return requester.requestPermission(Manifest.permission.RECORD_AUDIO)
        }
        
        // Requester가 없으면 현재 상태 반환
        println("⚠️ PermissionRequester not available")
        return hasAudioPermission()
    }
}