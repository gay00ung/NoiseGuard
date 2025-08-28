package net.lateinit.noiseguard.domain.audio

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import net.lateinit.noiseguard.domain.audio.AudioPermissionHandler
import net.lateinit.noiseguard.domain.audio.PermissionStatus
import kotlin.coroutines.resume

/**
 * Android 플랫폼의 오디오 권한 처리 구현체
 * RECORD_AUDIO 권한을 관리하고 요청
 */
class AndroidAudioPermissionHandler(
    private val context: Context
) : AudioPermissionHandler {
    
    companion object {
        const val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
        const val PERMISSION_REQUEST_CODE = 1001
        
        // 권한 요청 결과를 저장하기 위한 콜백
        private var permissionCallback: ((Boolean) -> Unit)? = null
    }
    
    /**
     * 오디오 녹음 권한 요청
     * Activity 컨텍스트가 필요하므로 Activity에서 호출해야 함
     */
    override suspend fun requestPermission(): Boolean {
        // 이미 권한이 있는 경우
        if (hasPermission()) {
            return true
        }
        
        // Activity 컨텍스트 확인
        val activity = context as? Activity 
            ?: throw IllegalStateException("Permission request requires Activity context")
        
        return suspendCancellableCoroutine { continuation ->
            permissionCallback = { granted ->
                continuation.resume(granted)
                permissionCallback = null
            }
            
            // 권한 요청
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(RECORD_AUDIO_PERMISSION),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * 현재 오디오 녹음 권한 상태 확인
     */
    override fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            RECORD_AUDIO_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 권한이 영구적으로 거부되었는지 확인
     * "다시 묻지 않음"을 선택한 경우
     */
    override fun isPermanentlyDenied(): Boolean {
        val activity = context as? Activity ?: return false
        
        return !hasPermission() && 
               !ActivityCompat.shouldShowRequestPermissionRationale(
                   activity,
                   RECORD_AUDIO_PERMISSION
               )
    }
    
    /**
     * 앱 설정 화면으로 이동
     * 사용자가 직접 권한을 활성화할 수 있도록 함
     */
    override fun openAppSettings() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    /**
     * 권한 상태를 PermissionStatus enum으로 반환
     */
    fun getPermissionStatus(): PermissionStatus {
        return when {
            hasPermission() -> PermissionStatus.GRANTED
            isPermanentlyDenied() -> PermissionStatus.PERMANENTLY_DENIED
            else -> PermissionStatus.DENIED
        }
    }
    
    /**
     * Activity에서 onRequestPermissionsResult 호출 시 사용
     * MainActivity에서 이 메소드를 호출해야 함
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val granted = grantResults.isNotEmpty() && 
                          grantResults[0] == PackageManager.PERMISSION_GRANTED
            permissionCallback?.invoke(granted)
        }
    }
}