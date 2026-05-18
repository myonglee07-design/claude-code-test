package com.myong.addr2map.core

import android.content.Context

object PrefsStore {

    private const val FILE = "addr2map_prefs"
    private const val KEY_DEFAULT_MAP = "default_map"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_ADDR_ONLY = "addr_only"
    private const val KEY_AUTO_DISMISS_SEC = "auto_dismiss_sec"

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
}
