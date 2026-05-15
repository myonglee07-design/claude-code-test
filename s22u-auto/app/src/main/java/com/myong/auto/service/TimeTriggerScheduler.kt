package com.myong.auto.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.myong.auto.App
import com.myong.auto.domain.Repeat
import com.myong.auto.domain.Rule
import com.myong.auto.domain.TriggerParams
import java.time.LocalDateTime
import java.time.ZoneId

object TimeTriggerScheduler {

    const val EXTRA_RULE_ID = "ruleId"
    private const val TAG = "TimeTrigger"

    fun schedule(context: Context, rule: Rule) {
        val p = rule.trigger.params as? TriggerParams.Time ?: return
        val triggerAt = computeTriggerMillis(p)
        if (triggerAt == null) {
            Log.d(TAG, "rule=${rule.id} once in past, not scheduled")
            cancel(context, rule.id)
            return
        }
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val pi = pendingIntent(context, rule.id)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            Log.d(TAG, "rule=${rule.id} exact @${triggerAt}")
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            Log.w(TAG, "rule=${rule.id} inexact (no SCHEDULE_EXACT_ALARM)")
            NotificationHelper.notifyRuleResult(
                context, rule.id, rule.name, false,
                "정확한 알람 권한이 없어 시간이 부정확할 수 있습니다 (설정에서 허용)"
            )
        }
    }

    fun cancel(context: Context, ruleId: Long) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        am.cancel(pendingIntent(context, ruleId))
    }

    suspend fun scheduleAllEnabled(context: Context) {
        val app = context.applicationContext as App
        app.repository.getEnabledRules()
            .filter { it.trigger.params is TriggerParams.Time }
            .forEach { schedule(context, it) }
    }

    private fun pendingIntent(context: Context, ruleId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
            .putExtra(EXTRA_RULE_ID, ruleId)
        return PendingIntent.getBroadcast(
            context,
            ruleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun computeTriggerMillis(p: TriggerParams.Time): Long? = when (p.repeat) {
        Repeat.Daily -> {
            val now = LocalDateTime.now()
            var t = now.toLocalDate().atTime(p.hour, p.minute)
            if (!t.isAfter(now)) t = t.plusDays(1)
            t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        Repeat.Once -> {
            val dt = p.onceDateTime
            if (dt != null && dt > System.currentTimeMillis()) dt else null
        }
    }
}
