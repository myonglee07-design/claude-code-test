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
                    val ms = com.myong.addr2map.core.PrefsStore
                        .autoDismissSec(appCtx) * 1000L
                    dismissRunnable = Runnable { remove() }.also {
                        main.postDelayed(it, ms)
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
        fun mapBtn(t: MapLauncher.Target) =
            Button(context).apply {
                val isTmap = t == MapLauncher.Target.TMAP
                text = if (isTmap) "티맵" else "카카오"
                setTextColor(Color.parseColor("#0A0E1A"))
                setBackgroundColor(Color.parseColor(if (isTmap) "#1FAF6B" else "#FEE500"))
                setOnClickListener {
                    remove()
                    MapLauncher.open(context, t, address)
                }
            }
        val default = com.myong.addr2map.core.PrefsStore.defaultTarget(context)
        val order = if (default == MapLauncher.Target.KAKAO)
            listOf(MapLauncher.Target.KAKAO, MapLauncher.Target.TMAP)
        else
            listOf(MapLauncher.Target.TMAP, MapLauncher.Target.KAKAO)
        row.addView(
            mapBtn(order[0]),
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .apply { marginEnd = dp(8) }
        )
        row.addView(
            mapBtn(order[1]),
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
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
