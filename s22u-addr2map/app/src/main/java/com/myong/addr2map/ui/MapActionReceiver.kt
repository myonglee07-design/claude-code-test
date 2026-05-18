package com.myong.addr2map.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.myong.addr2map.core.MapLauncher

class MapActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val targetName = intent.getStringExtra(EXTRA_TARGET) ?: return
        val address = intent.getStringExtra(EXTRA_ADDRESS) ?: return
        val target = runCatching { MapLauncher.Target.valueOf(targetName) }.getOrNull() ?: return
        MapLauncher.open(context.applicationContext, target, address)
        NotificationHelper.cancel(context.applicationContext)
    }

    companion object {
        const val EXTRA_TARGET = "target"
        const val EXTRA_ADDRESS = "address"
    }
}
