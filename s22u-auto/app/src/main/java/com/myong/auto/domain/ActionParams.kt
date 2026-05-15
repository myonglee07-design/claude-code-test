package com.myong.auto.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
enum class WifiState { On, Off }

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class ActionParams {

    @Serializable
    @SerialName("Wifi")
    data class Wifi(val state: WifiState) : ActionParams()

    @Serializable
    @SerialName("RunSu")
    data class RunSu(val command: String) : ActionParams()

    @Serializable
    @SerialName("Notify")
    data class Notify(val title: String, val message: String) : ActionParams()
}
