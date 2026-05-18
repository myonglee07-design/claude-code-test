package com.myong.addr2map.core

import android.content.Context

object PrefsStore {

    private const val FILE = "addr2map_prefs"
    private const val KEY_DEFAULT_MAP = "default_map"

    fun defaultTarget(context: Context): MapLauncher.Target {
        val name = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getString(KEY_DEFAULT_MAP, MapLauncher.Target.TMAP.name)
        return runCatching { MapLauncher.Target.valueOf(name!!) }
            .getOrDefault(MapLauncher.Target.TMAP)
    }

    fun setDefaultTarget(context: Context, target: MapLauncher.Target) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DEFAULT_MAP, target.name)
            .apply()
    }
}
