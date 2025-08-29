package net.lateinit.noiseguard.domain.permission

/**
 * Web 플랫폼의 권한 처리 구현
 * 웹 브라우저는 getUserMedia API 사용시 자동으로 권한 요청
 */
actual class PermissionHandler {
    
    actual fun hasAudioPermission(): Boolean {
        // 웹에서는 실시간으로 확인 불가, 항상 요청 필요
        return false
    }
    
    actual suspend fun requestAudioPermission(): Boolean {
        // 웹에서는 실제 녹음 시작시 브라우저가 자동으로 권한 요청
        // 현재는 지원하지 않음
        return false
    }
}