package com.myong.auto.data

import com.myong.auto.data.db.RuleDao
import com.myong.auto.domain.Rule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RuleRepository(private val dao: RuleDao) {

    fun observeRules(): Flow<List<Rule>> =
        dao.observeRules().map { list -> list.mapNotNull { it.toDomain() } }

    suspend fun insertRule(rule: Rule): Long =
        dao.insertFullRule(
            rule.toRuleEntity(),
            rule.trigger.toEntity(),
            rule.action.toEntity()
        )

    suspend fun setEnabled(id: Long, enabled: Boolean) =
        dao.setEnabled(id, enabled)

    suspend fun deleteRule(id: Long) =
        dao.deleteRule(id)

    suspend fun getEnabledRules(): List<Rule> =
        dao.getEnabledRules().mapNotNull { it.toDomain() }
}
