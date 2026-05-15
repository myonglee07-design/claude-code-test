package com.myong.auto

import android.app.Application
import com.myong.auto.data.RuleRepository
import com.myong.auto.data.db.AppDatabase
import com.myong.auto.domain.RuleEngine
import com.myong.auto.service.NotificationHelper

class App : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var repository: RuleRepository
        private set
    lateinit var ruleEngine: RuleEngine
        private set

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        database = AppDatabase.get(this)
        repository = RuleRepository(database.ruleDao())
        ruleEngine = RuleEngine(this, repository)
    }
}
