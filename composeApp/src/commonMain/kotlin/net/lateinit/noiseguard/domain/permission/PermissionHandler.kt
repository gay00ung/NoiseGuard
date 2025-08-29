package net.lateinit.noiseguard.domain.permission

/**
 * 멀티플랫폼 권한 처리를 위한 인터페이스
 * 각 플랫폼별로 구현이 필요
 */
expect class PermissionHandler {
    /**
     * 오디오 녹음 권한이 있는지 확인
     */
    fun hasAudioPermission(): Boolean
    
    /**
     * 오디오 녹음 권한 요청
     * @return 권한 승인 여부
     */
    suspend fun requestAudioPermission(): Boolean
}