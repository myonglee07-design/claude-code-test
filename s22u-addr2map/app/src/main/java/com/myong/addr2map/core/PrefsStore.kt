package com.myong.addr2map.core

import android.content.Context

object PrefsStore {

    private const val FILE = "addr2map_prefs"
    private const val KEY_DEFAULT_MAP = "default_map"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_ADDR_ONLY = "addr_only"
    private const val KEY_AUTO_DISMISS_SEC = "auto_dismiss_sec"
    private const val KEY_KAKAO_ON = "kakao_on"
    private const val KEY_RECON = "recon"
    private const val KEY_AUTO_A11Y = "auto_a11y"

    private fun sp(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun defaultTarget(context: Context): MapLauncher.Target {
        val name = sp(context).getString(KEY_DEFAULT_MAP, MapLauncher.Target.TMAP.name)
        return runCatching { MapLauncher.Target.valueOf(name!!) }
            .getOrDefault(MapLauncher.Target.TMAP)
    }

    fun setDefaultTarget(context: Context, target: MapLauncher.Target) {
        sp(context).edit().putString(KEY_DEFAULT_MAP, target.name).apply()
    }

    fun isEnabled(context: Context): Boolean =
        sp(context).getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, v: Boolean) {
        sp(context).edit().putBoolean(KEY_ENABLED, v).apply()
    }

    fun isAddrOnly(context: Context): Boolean =
        sp(context).getBoolean(KEY_ADDR_ONLY, true)

    fun setAddrOnly(context: Context, v: Boolean) {
        sp(context).edit().putBoolean(KEY_ADDR_ONLY, v).apply()
    }

    fun autoDismissSec(context: Context): Int =
        sp(context).getInt(KEY_AUTO_DISMISS_SEC, 6)

    fun setAutoDismissSec(context: Context, sec: Int) {
        sp(context).edit().putInt(KEY_AUTO_DISMISS_SEC, sec).apply()
    }

    fun isKakaoOn(context: Context): Boolean =
        sp(context).getBoolean(KEY_KAKAO_ON, true)

    fun setKakaoOn(context: Context, v: Boolean) {
        sp(context).edit().putBoolean(KEY_KAKAO_ON, v).apply()
    }

    fun isRecon(context: Context): Boolean =
        sp(context).getBoolean(KEY_RECON, false)

    fun setRecon(context: Context, v: Boolean) {
        sp(context).edit().putBoolean(KEY_RECON, v).apply()
    }

    fun isAutoA11y(context: Context): Boolean =
        sp(context).getBoolean(KEY_AUTO_A11Y, true)

    fun setAutoA11y(context: Context, v: Boolean) {
        sp(context).edit().putBoolean(KEY_AUTO_A11Y, v).apply()
    }
}
