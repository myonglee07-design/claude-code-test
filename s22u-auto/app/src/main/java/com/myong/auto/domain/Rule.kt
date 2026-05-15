package com.myong.auto.domain

data class Rule(
    val id: Long = 0,
    val name: String,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val trigger: Trigger,
    val action: Action
)
