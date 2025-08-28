package net.lateinit.noiseguard.domain.audio

/**
 * 오디오 녹음 권한 처리를 위한 인터페이스
 * 플랫폼별로 구현되어야 함
 */
interface AudioPermissionHandler {
    
    /**
     * 오디오 녹음 권한 요청
     * @return 권한이 부여되었으면 true, 거부되었으면 false
     */
    suspend fun requestPermission(): Boolean
    
    /**
     * 현재 오디오 녹음 권한 상태 확인
     * @return 권한이 있으면 true, 없으면 false
     */
    fun hasPermission(): Boolean
    
    /**
     * 권한이 영구적으로 거부되었는지 확인
     * @return 영구 거부 상태면 true
     */
    fun isPermanentlyDenied(): Boolean
    
    /**
     * 시스템 설정으로 이동하여 권한을 수동으로 활성화하도록 안내
     */
    fun openAppSettings()
}

/**
 * 권한 상태를 나타내는 열거형
 */
enum class PermissionStatus {
    GRANTED,      // 권한 허용됨
    DENIED,       // 권한 거부됨 (다시 요청 가능)
    PERMANENTLY_DENIED, // 권한 영구 거부됨 (설정에서만 변경 가능)
    NOT_DETERMINED // 아직 권한 요청하지 않음 (iOS)
}