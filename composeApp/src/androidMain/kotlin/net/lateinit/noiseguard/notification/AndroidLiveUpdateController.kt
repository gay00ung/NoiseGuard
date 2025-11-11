package net.lateinit.noiseguard.notification

import android.Manifest
import androidx.annotation.RequiresPermission

/**
 * Android Live Update 알림과 HomeViewModel 사이를 이어주는 구현체.
 */
class AndroidLiveUpdateController(
    private val notifier: LiveUpdateNotifier
) : LiveUpdateController {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun start(totalSeconds: Long) {
        notifier.start(totalSeconds)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun update(remainingSeconds: Long, currentDecibel: Float) {
        notifier.update(remainingSeconds, currentDecibel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun complete() {
        notifier.complete()
    }

    override fun cancel() {
        notifier.cancel()
    }
}
