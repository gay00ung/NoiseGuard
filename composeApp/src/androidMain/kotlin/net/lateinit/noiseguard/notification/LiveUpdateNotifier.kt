package net.lateinit.noiseguard.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import net.lateinit.noiseguard.LIVE_CH_ID
import net.lateinit.noiseguard.MainActivity
import net.lateinit.noiseguard.R
import kotlin.math.roundToInt

/**
 * 실시간 소음 측정 알림을 관리하는 클래스
 *
 * 프로모션 라이브 업데이트 요건을 만족하도록 구성한다.
 */
class LiveUpdateNotifier(private val context: Context) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val notificationId = 1001
    private val contentIntent: PendingIntent by lazy {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private var totalSeconds: Long = 0

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun start(totalSeconds: Long) {
        this.totalSeconds = totalSeconds.coerceAtLeast(0)
        val remainingText = formatStatusText(this.totalSeconds, null)
        notificationManager.notify(
            notificationId,
            baseBuilder(remainingSeconds = this.totalSeconds)
                .setContentTitle("소음 측정 중")
                .setContentText(remainingText)
                .setProgress(100, 0, this.totalSeconds == 0L)
                .setStyle(NotificationCompat.BigTextStyle().bigText(remainingText))
                .build()
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun update(remainingSeconds: Long, currentDecibel: Float) {
        val safeRemaining = remainingSeconds.coerceAtLeast(0)
        val text = formatStatusText(safeRemaining, currentDecibel)
        val progress = progressFor(safeRemaining)
        notificationManager.notify(
            notificationId,
            baseBuilder(remainingSeconds = safeRemaining)
                .setContentTitle("소음 측정 중")
                .setContentText(text)
                .setProgress(100, progress, totalSeconds == 0L)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .build()
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun complete() {
        val text = "주변 소음 레벨 측정을 종료했어요."
        totalSeconds = 0
        notificationManager.notify(
            notificationId,
            baseBuilder()
                .setContentTitle("소음 측정 완료")
                .setContentText(text)
                .setOngoing(false)
                .setAutoCancel(true)
                .setProgress(0, 0, false)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .build()
        )
    }

    fun cancel() {
        totalSeconds = 0
        notificationManager.cancel(notificationId)
    }

    private fun baseBuilder(remainingSeconds: Long? = null): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, LIVE_CH_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(contentIntent)

        if (remainingSeconds != null) {
            val countdownMillis = remainingSeconds.coerceAtLeast(0) * 1000
            builder
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(System.currentTimeMillis() + countdownMillis + 1_000)
        }

        return builder
    }

    private fun formatStatusText(remainingSeconds: Long, currentDecibel: Float?): String {
        val countdown = formatCountdown(remainingSeconds)
        val safeDb = currentDecibel?.takeIf { it.isFinite() }
        return if (safeDb != null) {
            "$countdown · 현재 ${safeDb.toInt()} dB"
        } else {
            countdown
        }
    }

    private fun formatCountdown(seconds: Long): String {
        val minutes = seconds / 60
        val secs = (seconds % 60).toInt()
        return if (minutes > 0) {
            "${minutes}분 ${secs.toString().padStart(2, '0')}초 남음"
        } else {
            "${secs}초 남음"
        }
    }

    private fun progressFor(remainingSeconds: Long): Int {
        if (totalSeconds <= 0) return 0
        val elapsed = (totalSeconds - remainingSeconds).coerceAtLeast(0)
        return ((elapsed * 100f) / totalSeconds).roundToInt().coerceIn(0, 100)
    }
}
