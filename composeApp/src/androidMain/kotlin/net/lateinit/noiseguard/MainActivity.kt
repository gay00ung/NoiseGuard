package net.lateinit.noiseguard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import net.lateinit.noiseguard.domain.permission.PermissionRequester
import net.lateinit.noiseguard.domain.permission.PermissionRequesterHolder
import kotlin.coroutines.resume

class MainActivity : ComponentActivity(), PermissionRequester {
    
    private val _permissionState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    override val permissionState: StateFlow<Map<String, Boolean>> = _permissionState
    
    private var permissionCallback: ((Boolean) -> Unit)? = null
    
    // 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionCallback?.invoke(isGranted)
        permissionCallback = null
        
        // 권한 상태 업데이트
        _permissionState.value = _permissionState.value + 
            (Manifest.permission.RECORD_AUDIO to isGranted)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // PermissionRequester 등록
        PermissionRequesterHolder.requester = this
        
        setContent {
            NoiseGuardApp()
        }
    }
    
    override suspend fun requestPermission(permission: String): Boolean {
        // 이미 권한이 있는지 확인
        if (ContextCompat.checkSelfPermission(this, permission) 
            == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        
        // 코루틴으로 콜백 기다리기
        return suspendCancellableCoroutine { continuation ->
            permissionCallback = { granted ->
                continuation.resume(granted)
            }
            requestPermissionLauncher.launch(permission)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 메모리 누수 방지
        PermissionRequesterHolder.requester = null
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    NoiseGuardApp()
}
