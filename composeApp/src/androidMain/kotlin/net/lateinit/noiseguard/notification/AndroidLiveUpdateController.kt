package net.lateinit.noiseguard.notification

/**
 * Android Live Update 알림과 HomeViewModel 사이를 이어주는 구현체.
 */
class AndroidLiveUpdateController(
    private val notifier: LiveUpdateNotifier
) : LiveUpdateController {

    override fun start(totalSeconds: Long) {
        notifier.start(totalSeconds)
    }

    override fun update(remainingSeconds: Long, currentDecibel: Float) {
        notifier.update(remainingSeconds, currentDecibel)
    }

    override fun complete() {
        notifier.complete()
    }

    override fun cancel() {
        notifier.cancel()
    }
}
