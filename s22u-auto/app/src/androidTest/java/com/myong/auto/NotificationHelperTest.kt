package com.myong.auto

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.myong.auto.service.NotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationHelperTest {

    @get:Rule
    val perm: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    private val ctx: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val mgr: NotificationManager
        get() = ctx.getSystemService(NotificationManager::class.java)

    @Before
    fun clear() {
        mgr.cancelAll()
    }

    @Test
    fun ensureChannel_createsChannel() {
        NotificationHelper.ensureChannel(ctx)
        assertNotNull(mgr.getNotificationChannel("rule_result"))
    }

    @Test
    fun notify_showsOneActiveNotification() {
        NotificationHelper.notifyRuleResult(ctx, 42L, "테스트 룰", true, "완료")
        val mine = mgr.activeNotifications.filter { it.id == 42 }
        assertEquals(1, mine.size)
    }

    @Test
    fun sameRuleId_overwritesNotStacks() {
        NotificationHelper.notifyRuleResult(ctx, 7L, "룰", true, "1차")
        NotificationHelper.notifyRuleResult(ctx, 7L, "룰", false, "2차")
        val mine = mgr.activeNotifications.filter { it.id == 7 }
        assertEquals(1, mine.size)
    }
}
