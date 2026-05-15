package com.myong.auto.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.myong.auto.R

object NotificationHelper {

    private const val CHANNEL_ID = "rule_result"
    private const val CHANNEL_NAME = "룰 실행 결과"
    private const val CHANNEL_DESC = "자동화 규칙이 실행되었을 때 표시되는 알림"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = context.getSystemService(NotificationManager::class.java) ?: return
        if (mgr.getNotificationChannel(CHANNEL_ID) != null) return
        val ch = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = CHANNEL_DESC
            enableVibration(false)
            setSound(null, null)
            setShowBadge(false)
        }
        mgr.createNotificationChannel(ch)
    }

    fun notifyRuleResult(
        context: Context,
        ruleId: Long,
        ruleName: String,
        success: Boolean,
        detail: String? = null
    ) {
        ensureChannel(context)
        val icon = if (success) R.drawable.ic_check else R.drawable.ic_warning
        val colorRes = if (success) R.color.successColor else R.color.errorColor
        val title = (if (success) "✓ " else "⚠ ") + ruleName
        val text = detail ?: if (success) "실행 완료" else "실행 실패"

        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setColor(ContextCompat.getColor(context, colorRes))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(ruleId.toInt(), n)
        } catch (_: SecurityException) {
        }
    }
}
