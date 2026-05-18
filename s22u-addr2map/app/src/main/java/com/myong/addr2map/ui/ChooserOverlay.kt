package com.myong.addr2map.ui

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.myong.addr2map.core.MapLauncher

object ChooserOverlay {

    private const val AUTO_DISMISS_MS = 6000L

    private val main = Handler(Looper.getMainLooper())
    private var currentView: View? = null
    private var dismissRunnable: Runnable? = null

    fun show(context: Context, address: String) {
        val appCtx = context.applicationContext
        main.post {
            if (!Settings.canDrawOverlays(appCtx)) {
                NotificationHelper.showChooser(appCtx, address)
                return@post
            }
            remove()
            val wm = appCtx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val view = buildView(appCtx, address)
            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }
            runCatching { wm.addView(view, lp) }
                .onSuccess {
                    currentView = view
                    dismissRunnable = Runnable { remove() }.also {
                        main.postDelayed(it, AUTO_DISMISS_MS)
                    }
                }
                .onFailure { NotificationHelper.showChooser(appCtx, address) }
        }
    }

    private fun overlayType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

    private fun remove() {
        dismissRunnable?.let { main.removeCallbacks(it) }
        dismissRunnable = null
        val v = currentView ?: return
        currentView = null
        val wm = v.context.applicationContext
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        runCatching { wm.removeView(v) }
    }

    private fun buildView(context: Context, address: String): View {
        fun dp(v: Int) = (v * context.resources.displayMetrics.density).toInt()

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A2238"))
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }
        card.addView(TextView(context).apply {
            text = address
            setTextColor(Color.WHITE)
            textSize = 15f
            maxLines = 3
        })
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(16), 0, 0)
        }
        fun mapBtn(label: String, color: String, t: MapLauncher.Target) =
            Button(context).apply {
                text = label
                setTextColor(Color.parseColor("#0A0E1A"))
                setBackgroundColor(Color.parseColor(color))
                setOnClickListener {
                    remove()
                    MapLauncher.open(context, t, address)
                }
            }
        row.addView(mapBtn("티맵", "#1FAF6B", MapLauncher.Target.TMAP),
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .apply { marginEnd = dp(8) })
        row.addView(mapBtn("카카오", "#FEE500", MapLauncher.Target.KAKAO),
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        card.addView(row)
        card.addView(Button(context).apply {
            text = "닫기"
            setTextColor(Color.parseColor("#B0BEC5"))
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { remove() }
        })
        return card
    }
}
