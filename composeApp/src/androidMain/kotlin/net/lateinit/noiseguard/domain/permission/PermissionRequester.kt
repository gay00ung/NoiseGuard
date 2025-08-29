package net.lateinit.noiseguard.domain.permission

import kotlinx.coroutines.flow.StateFlow

/**
 * Activity에서 구현할 권한 요청 인터페이스
 */
interface PermissionRequester {
    /**
     * 권한 요청 수행
     */
    suspend fun requestPermission(permission: String): Boolean
    
    /**
     * 권한 상태 Flow
     */
    val permissionState: StateFlow<Map<String, Boolean>>
}

/**
 * PermissionRequester를 보관하는 싱글톤
 * Activity가 생성될 때 설정됨
 */
object PermissionRequesterHolder {
    var requester: PermissionRequester? = null
}