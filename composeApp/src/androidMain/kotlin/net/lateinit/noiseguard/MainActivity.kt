package net.lateinit.noiseguard

import android.Manifest
import android.content.pm.*
import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.activity.result.contract.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.*
import androidx.core.content.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.lateinit.noiseguard.domain.permission.*
import kotlin.coroutines.*

class MainActivity : ComponentActivity(), PermissionRequester {

    private val _permissionState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    override val permissionState: StateFlow<Map<String, Boolean>> = _permissionState

    private var permissionCallback: ((Boolean) -> Unit)? = null
    private var pendingPermission: String? = null

    // 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionCallback?.invoke(isGranted)
        permissionCallback = null

        pendingPermission?.let { requested ->
            _permissionState.value = _permissionState.value + (requested to isGranted)
        }
        pendingPermission = null
    }

    override suspend fun requestPermission(permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return suspendCancellableCoroutine { continuation ->
            permissionCallback = { granted -> continuation.resume(granted) }
            pendingPermission = permission
            requestPermissionLauncher.launch(permission)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // PermissionRequester 등록
        PermissionRequesterHolder.requester = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lifecycleScope.launch {
                requestPermission(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            NoiseGuardApp()
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
