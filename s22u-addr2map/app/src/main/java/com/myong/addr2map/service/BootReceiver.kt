package com.myong.addr2map.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.myong.addr2map.core.PrefsStore
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val a = intent.action ?: return
        if (a != Intent.ACTION_BOOT_COMPLETED &&
            a != "android.intent.action.QUICKBOOT_POWERON" &&
            a != "com.htc.intent.action.QUICKBOOT_POWERON") return

        if (!PrefsStore.isAutoA11y(context)) return

        val pending = goAsync()
        val app = context.applicationContext
        Thread {
            try {
                Thread.sleep(BOOT_DELAY_MS)
                if (A11yAuto.isAccessibilityEnabled(app)) return@Thread
                val cmd = A11yAuto.buildEnableCmd(app)
                val r = runBlocking { SuRunner.run(cmd) }
                Log.d(TAG, "boot a11y auto-enable success=${r.success}")
            } catch (_: InterruptedException) {
            } finally {
                pending.finish()
            }
        }.start()
    }

    companion object {
        private const val TAG = "BootReceiver"
        private const val BOOT_DELAY_MS = 30_000L
    }
}
