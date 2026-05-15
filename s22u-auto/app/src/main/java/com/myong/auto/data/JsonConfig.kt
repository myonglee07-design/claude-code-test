package com.myong.auto.data

import kotlinx.serialization.json.Json

val appJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    classDiscriminator = "type"
}
