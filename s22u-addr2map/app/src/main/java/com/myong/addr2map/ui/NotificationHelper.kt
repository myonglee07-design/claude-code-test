package com.myong.addr2map.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.myong.addr2map.R
import com.myong.addr2map.core.MapLauncher

object NotificationHelper {

    private const val CHANNEL_ID = "addr_chooser"
    private const val CHANNEL_NAME = "주소 → 맵 선택"
    private const val CHANNEL_DESC = "주소 복사 시 티맵/카카오 선택 알림"
    const val NOTI_ID = 2001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = context.getSystemService(NotificationManager::class.java) ?: return
        if (mgr.getNotificationChannel(CHANNEL_ID) != null) return
        mgr.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = CHANNEL_DESC
                enableVibration(false)
                setSound(null, null)
            }
        )
    }

    fun showChooser(context: Context, address: String) {
        ensureChannel(context)
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("주소 → 맵으로 열기")
            .setContentText(address)
            .setStyle(NotificationCompat.BigTextStyle().bigText(address))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, "티맵", action(context, MapLauncher.Target.TMAP, address))
            .addAction(0, "카카오", action(context, MapLauncher.Target.KAKAO, address))
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTI_ID, n)
        } catch (_: SecurityException) {
        }
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTI_ID)
    }

    private fun action(
        context: Context,
        target: MapLauncher.Target,
        address: String
    ): PendingIntent {
        val intent = Intent(context, MapActionReceiver::class.java)
            .putExtra(MapActionReceiver.EXTRA_TARGET, target.name)
            .putExtra(MapActionReceiver.EXTRA_ADDRESS, address)
        return PendingIntent.getBroadcast(
            context,
            target.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
