package com.myong.addr2map.service

import android.accessibilityservice.AccessibilityService
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.myong.addr2map.core.AddressDetector
import com.myong.addr2map.core.PrefsStore
import com.myong.addr2map.ui.ChooserOverlay

class AddrAccessibilityService : AccessibilityService() {

    private var lastText: String? = null
    private var lastAt: Long = 0L

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        runCatching { handleClip() }
            .onFailure { Log.w(TAG, "clip read failed: ${it.message}") }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        clipboard()?.addPrimaryClipChangedListener(clipListener)
        Log.d(TAG, "connected")
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        clipboard()?.removePrimaryClipChangedListener(clipListener)
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    private fun clipboard(): ClipboardManager? =
        getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    private fun handleClip() {
        if (!PrefsStore.isEnabled(this)) return
        val cm = clipboard() ?: return
        val clip = cm.primaryClip ?: return
        if (clip.itemCount == 0) return
        val text = clip.getItemAt(0).coerceToText(this)?.toString()?.trim().orEmpty()
        if (text.isEmpty()) return

        val now = System.currentTimeMillis()
        if (text == lastText && now - lastAt < DEDUPE_MS) return
        lastText = text
        lastAt = now

        if (PrefsStore.isAddrOnly(this) && !AddressDetector.isLikelyAddress(text)) return
        Log.d(TAG, "clip handled")
        ChooserOverlay.show(applicationContext, text)
    }

    companion object {
        private const val TAG = "AddrA11y"
        private const val DEDUPE_MS = 5000L
    }
}
