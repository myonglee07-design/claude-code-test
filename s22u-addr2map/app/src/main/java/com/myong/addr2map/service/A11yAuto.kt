package com.myong.addr2map.service

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

object A11yAuto {

    fun isAccessibilityEnabled(context: Context): Boolean {
        val expected = ComponentName(context, AddrAccessibilityService::class.java)
            .flattenToString()
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    fun buildEnableCmd(context: Context): String {
        val comp = ComponentName(context, AddrAccessibilityService::class.java)
            .flattenToString()
        return "C=\"$comp\"; " +
            "CUR=\$(settings get secure enabled_accessibility_services); " +
            "if [ \"\$CUR\" = \"null\" ] || [ -z \"\$CUR\" ]; then NEW=\"\$C\"; " +
            "elif echo \"\$CUR\" | grep -q \"\$C\"; then NEW=\"\$CUR\"; " +
            "else NEW=\"\$CUR:\$C\"; fi; " +
            "settings put secure enabled_accessibility_services \"\$NEW\"; " +
            "settings put secure accessibility_enabled 1"
    }
}
