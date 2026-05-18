package com.myong.addr2map.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException

object SuRunner {

    private const val TAG = "SuRunner"
    private const val DEFAULT_TIMEOUT_MS = 5000L
    private const val ROOT_CHECK_TIMEOUT_MS = 2000L
    private const val LOG_CMD_MASK = 50

    data class Result(
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
        val timedOut: Boolean
    ) {
        val success: Boolean get() = exitCode == 0 && !timedOut
    }

    suspend fun run(command: String, timeoutMs: Long = DEFAULT_TIMEOUT_MS): Result =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "run: ${command.take(LOG_CMD_MASK)}…")
            var process: Process? = null
            try {
                val p = Runtime.getRuntime().exec("su")
                process = p
                p.outputStream.bufferedWriter().use { w ->
                    w.write(command)
                    w.write("\n")
                    w.write("exit\n")
                    w.flush()
                }
                val outDef = async { p.inputStream.bufferedReader().readText() }
                val errDef = async { p.errorStream.bufferedReader().readText() }
                val finished = withTimeoutOrNull(timeoutMs) {
                    runInterruptible { p.waitFor() }
                    true
                }
                if (finished == null) {
                    p.destroyForcibly()
                    Log.w(TAG, "timeout after ${timeoutMs}ms")
                    Result(-1, outDef.await().trimEnd(), errDef.await().trimEnd(), true)
                } else {
                    val r = Result(
                        p.exitValue(),
                        outDef.await().trimEnd(),
                        errDef.await().trimEnd(),
                        false
                    )
                    if (!r.success) Log.w(TAG, "fail exit=${r.exitCode} stderr=${r.stderr}")
                    r
                }
            } catch (e: IOException) {
                Log.w(TAG, "su not found: ${e.message}")
                Result(-1, "", e.message ?: "su not found", false)
            } catch (e: InterruptedException) {
                Result(-1, "", "interrupted", true)
            } finally {
                try {
                    process?.destroyForcibly()
                } catch (_: Exception) {
                }
            }
        }

    suspend fun isRootAvailable(): Boolean {
        val r = run("id", ROOT_CHECK_TIMEOUT_MS)
        return r.success && r.stdout.contains("uid=0")
    }
}
