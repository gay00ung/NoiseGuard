package net.lateinit.noiseguard.notification

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import net.lateinit.noiseguard.LIVE_CH_ID
import net.lateinit.noiseguard.R

/**
 * 실시간 소음 측정 알림을 관리하는 클래스
 *
 * 프로모션 라이브 업데이트 요건을 만족하도록 구성한다.
 */
class LiveUpdateNotifier(private val context: Context) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val notificationId = 1001

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun start(progress: Int = 0) {
        val text = "주변 소음 레벨을 실시간으로 측정하고 있어요."
        notificationManager.notify(
            notificationId,
            baseBuilder()
                .setContentTitle("소음 측정 중")
                .setContentText(text)
                .setProgress(100, progress, progress == 0)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .build()
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun update(progress: Int, text: String) {
        notificationManager.notify(
            notificationId,
            baseBuilder()
                .setContentTitle("소음 측정 중")
                .setContentText(text)
                .setProgress(100, progress, false)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .build()
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun complete() {
        val text = "주변 소음 레벨 측정을 종료했어요."
        notificationManager.notify(
            notificationId,
            baseBuilder()
                .setContentTitle("소음 측정 종료")
                .setContentText(text)
                .setOngoing(false)
                .setRequestPromotedOngoing(false)
                .setProgress(0, 0, false)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .build()
        )
    }

    fun cancel() {
        notificationManager.cancel(notificationId)
    }

    private fun baseBuilder(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, LIVE_CH_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setRequestPromotedOngoing(true)
