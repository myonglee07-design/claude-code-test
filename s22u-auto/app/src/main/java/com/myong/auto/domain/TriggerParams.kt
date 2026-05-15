package com.myong.auto.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
enum class Repeat { Daily, Once }

@Serializable
enum class Transition { Enter, Exit, Both }

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class TriggerParams {

    @Serializable
    @SerialName("Time")
    data class Time(
        val hour: Int,
        val minute: Int,
        val repeat: Repeat,
        val onceDateTime: Long? = null
    ) : TriggerParams()

    @Serializable
    @SerialName("Geofence")
    data class Geofence(
        val lat: Double,
        val lon: Double,
        val radiusM: Int,
        val transition: Transition
    ) : TriggerParams()
}
