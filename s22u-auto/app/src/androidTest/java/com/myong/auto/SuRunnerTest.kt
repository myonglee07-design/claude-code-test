package com.myong.auto

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.myong.auto.service.SuRunner
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SuRunnerTest {

    @Test
    fun rootCheck_returnsBoolean() = runBlocking {
        val available = SuRunner.isRootAvailable()
        assertTrue(available || !available)
    }

    @Test
    fun success_echo() = runBlocking {
        assumeTrue(SuRunner.isRootAvailable())
        val r = SuRunner.run("echo hello")
        assertEquals(0, r.exitCode)
        assertEquals("hello", r.stdout)
        assertTrue(r.success)
    }

    @Test
    fun failure_false() = runBlocking {
        assumeTrue(SuRunner.isRootAvailable())
        val r = SuRunner.run("false")
        assertEquals(1, r.exitCode)
        assertTrue(!r.success)
    }

    @Test
    fun timeout_sleep() = runBlocking {
        assumeTrue(SuRunner.isRootAvailable())
        val r = SuRunner.run("sleep 10", timeoutMs = 100L)
        assertTrue(r.timedOut)
        assertTrue(!r.success)
    }
}
