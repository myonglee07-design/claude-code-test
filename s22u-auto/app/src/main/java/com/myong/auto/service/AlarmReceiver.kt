package com.myong.auto.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.myong.auto.App
import com.myong.auto.domain.Repeat
import com.myong.auto.domain.TriggerParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val ruleId = intent.getLongExtra(TimeTriggerScheduler.EXTRA_RULE_ID, -1L)
        if (ruleId < 0) return

        val pending = goAsync()
        val appCtx = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val app = appCtx as App
                val rule = app.repository.getRule(ruleId) ?: return@launch
                if (!rule.enabled) return@launch

                Log.d(TAG, "fire rule=${ruleId} ${rule.name}")

                // 액션 실행(ruleEngine.execute)은 10단계에서 연결. 지금은 발화 가시화만.
                NotificationHelper.notifyRuleResult(
                    appCtx, ruleId, rule.name, true,
                    "시간 트리거 발화 (액션 실행은 10단계)"
                )

                when ((rule.trigger.params as? TriggerParams.Time)?.repeat) {
                    Repeat.Daily -> TimeTriggerScheduler.schedule(appCtx, rule)
                    Repeat.Once -> app.repository.setEnabled(ruleId, false)
                    null -> {}
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}
