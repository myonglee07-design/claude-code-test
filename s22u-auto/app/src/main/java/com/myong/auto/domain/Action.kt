package com.myong.auto.domain

data class Action(val params: ActionParams) {
    val type: String
        get() = when (params) {
            is ActionParams.Wifi -> "WIFI"
            is ActionParams.RunSu -> "RUN_SU"
            is ActionParams.Notify -> "NOTIFY"
        }
}
