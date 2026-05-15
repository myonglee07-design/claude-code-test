package com.myong.auto

import com.myong.auto.data.appJson
import com.myong.auto.domain.ActionParams
import com.myong.auto.domain.Repeat
import com.myong.auto.domain.Transition
import com.myong.auto.domain.TriggerParams
import com.myong.auto.domain.WifiState
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParamsSerializationTest {

    @Test
    fun time_roundTrip() {
        val src: TriggerParams = TriggerParams.Time(3, 0, Repeat.Daily)
        val json = appJson.encodeToString(src)
        assertTrue(json.contains("\"type\":\"Time\""))
        assertEquals(src, appJson.decodeFromString<TriggerParams>(json))
    }

    @Test
    fun geofence_both_roundTrip() {
        val src: TriggerParams =
            TriggerParams.Geofence(35.87, 128.6, 150, Transition.Both)
        val json = appJson.encodeToString(src)
        assertEquals(src, appJson.decodeFromString<TriggerParams>(json))
    }

    @Test
    fun wifi_on_serializes_expected() {
        val src: ActionParams = ActionParams.Wifi(WifiState.On)
        val json = appJson.encodeToString(src)
        assertTrue(json.contains("\"type\":\"Wifi\""))
        assertTrue(json.contains("\"state\":\"On\""))
        assertEquals(src, appJson.decodeFromString<ActionParams>(json))
    }

    @Test
    fun runSu_and_notify_roundTrip() {
        val su: ActionParams = ActionParams.RunSu("svc wifi enable && echo ok")
        assertEquals(su, appJson.decodeFromString<ActionParams>(appJson.encodeToString(su)))
        val noti: ActionParams = ActionParams.Notify("제목", "본문")
        assertEquals(noti, appJson.decodeFromString<ActionParams>(appJson.encodeToString(noti)))
    }
}
