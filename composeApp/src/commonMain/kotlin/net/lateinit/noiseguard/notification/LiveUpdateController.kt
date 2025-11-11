package net.lateinit.noiseguard.notification

/**
 * 플랫폼별 실시간 타이머/라이브 업데이트 노출을 추상화한 컨트롤러.
 * Android에서는 Live Update 알림으로, 다른 플랫폼에서는 No-op으로 처리한다.
 */
interface LiveUpdateController {
    /**
     * 자동 타이머가 시작되면 호출된다.
     *
     * @param totalSeconds 타이머 전체 길이(초)
     */
    fun start(totalSeconds: Long)

    /**
     * 타이머 남은 시간을 전달하며 주기적으로 호출된다.
     *
     * @param remainingSeconds 남은 시간(초)
     * @param currentDecibel 현재 측정 중인 dB 값
     */
    fun update(remainingSeconds: Long, currentDecibel: Float)

    /**
     * 타이머가 자연 종료(자동 중지) 되었을 때 호출된다.
     */
    fun complete()

    /**
     * 사용자가 녹음을 중단/일시정지하는 등 타이머가 취소될 때 호출된다.
     */
    fun cancel()
}

/**
 * 기본 No-op 구현 (Android 외 플랫폼에서 사용).
 */
object NoopLiveUpdateController : LiveUpdateController {
    override fun start(totalSeconds: Long) = Unit
    override fun update(remainingSeconds: Long, currentDecibel: Float) = Unit
    override fun complete() = Unit
    override fun cancel() = Unit
}
