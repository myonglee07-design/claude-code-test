package com.myong.auto.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {

    @Insert
    suspend fun insertRule(rule: RuleEntity): Long

    @Insert
    suspend fun insertTrigger(trigger: TriggerEntity): Long

    @Insert
    suspend fun insertAction(action: ActionEntity): Long

    @Transaction
    suspend fun insertFullRule(
        rule: RuleEntity,
        trigger: TriggerEntity,
        action: ActionEntity
    ): Long {
        val ruleId = insertRule(rule)
        insertTrigger(trigger.copy(ruleId = ruleId))
        insertAction(action.copy(ruleId = ruleId))
        return ruleId
    }

    @Query("UPDATE rules SET enabled = :enabled WHERE id = :ruleId")
    suspend fun setEnabled(ruleId: Long, enabled: Boolean)

    @Query("DELETE FROM rules WHERE id = :ruleId")
    suspend fun deleteRule(ruleId: Long)

    @Transaction
    @Query("SELECT * FROM rules ORDER BY createdAt DESC")
    fun observeRules(): Flow<List<RuleWithTriggerAndAction>>

    @Transaction
    @Query("SELECT * FROM rules WHERE id = :ruleId")
    suspend fun getRule(ruleId: Long): RuleWithTriggerAndAction?

    @Transaction
    @Query("SELECT * FROM rules WHERE enabled = 1")
    suspend fun getEnabledRules(): List<RuleWithTriggerAndAction>
}
