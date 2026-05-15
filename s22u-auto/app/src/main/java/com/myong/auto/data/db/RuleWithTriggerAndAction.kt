package com.myong.auto.data.db

import androidx.room.Embedded
import androidx.room.Relation

data class RuleWithTriggerAndAction(
    @Embedded val rule: RuleEntity,
    @Relation(parentColumn = "id", entityColumn = "ruleId")
    val trigger: TriggerEntity?,
    @Relation(parentColumn = "id", entityColumn = "ruleId")
    val action: ActionEntity?
)
