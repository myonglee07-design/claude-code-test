package com.myong.auto.domain

data class Trigger(val params: TriggerParams) {
    val type: String
        get() = when (params) {
            is TriggerParams.Time -> "TIME"
            is TriggerParams.Geofence -> "GEOFENCE"
        }
}
