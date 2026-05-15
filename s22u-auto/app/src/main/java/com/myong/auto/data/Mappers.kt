package com.myong.auto.data

import com.myong.auto.data.db.ActionEntity
import com.myong.auto.data.db.RuleEntity
import com.myong.auto.data.db.RuleWithTriggerAndAction
import com.myong.auto.data.db.TriggerEntity
import com.myong.auto.domain.Action
import com.myong.auto.domain.ActionParams
import com.myong.auto.domain.Rule
import com.myong.auto.domain.Trigger
import com.myong.auto.domain.TriggerParams
import kotlinx.serialization.encodeToString

fun RuleWithTriggerAndAction.toDomain(): Rule? {
    val t = trigger ?: return null
    val a = action ?: return null
    return Rule(
        id = rule.id,
        name = rule.name,
        enabled = rule.enabled,
        createdAt = rule.createdAt,
        trigger = Trigger(appJson.decodeFromString<TriggerParams>(t.paramsJson)),
        action = Action(appJson.decodeFromString<ActionParams>(a.paramsJson))
    )
}

fun Rule.toRuleEntity(): RuleEntity =
    RuleEntity(id = id, name = name, enabled = enabled, createdAt = createdAt)

fun Trigger.toEntity(ruleId: Long = 0): TriggerEntity =
    TriggerEntity(
        ruleId = ruleId,
        type = type,
        paramsJson = appJson.encodeToString(params)
    )

fun Action.toEntity(ruleId: Long = 0): ActionEntity =
    ActionEntity(
        ruleId = ruleId,
        type = type,
        paramsJson = appJson.encodeToString(params)
    )
