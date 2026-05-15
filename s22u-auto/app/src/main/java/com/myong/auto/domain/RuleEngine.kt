package com.myong.auto.domain

import android.content.Context
import android.util.Log
import com.myong.auto.data.RuleRepository

class RuleEngine(
    private val context: Context,
    private val repository: RuleRepository
) {
    suspend fun registerAll() {
        val rules = repository.getEnabledRules()
        Log.d(TAG, "registerAll: ${rules.size} enabled rule(s)")
        rules.forEach { register(it) }
    }

    suspend fun register(rule: Rule) {
        Log.d(TAG, "register: id=${rule.id} ${rule.trigger.type}")
        when (rule.trigger.params) {
            is TriggerParams.Time ->
                TODO("7단계 AlarmReceiver: AlarmManager.setExactAndAllowWhileIdle 등록")
            is TriggerParams.Geofence ->
                TODO("8단계 GeofenceReceiver: GeofencingClient.addGeofences 등록")
        }
    }

    suspend fun unregister(rule: Rule) {
        Log.d(TAG, "unregister: id=${rule.id} ${rule.trigger.type}")
        when (rule.trigger.params) {
            is TriggerParams.Time ->
                TODO("7단계: AlarmManager.cancel(PendingIntent)")
            is TriggerParams.Geofence ->
                TODO("8단계: GeofencingClient.removeGeofences")
        }
    }

    suspend fun execute(rule: Rule) {
        Log.d(TAG, "execute: id=${rule.id} ${rule.action.type}")
        when (rule.action.params) {
            is ActionParams.Wifi ->
                TODO("10단계: SuRunner svc wifi enable|disable")
            is ActionParams.RunSu ->
                TODO("10단계: SuRunner.runOneShot(command)")
            is ActionParams.Notify ->
                TODO("10단계: NotificationHelper 표시")
        }
    }

    companion object {
        private const val TAG = "RuleEngine"
    }
}
