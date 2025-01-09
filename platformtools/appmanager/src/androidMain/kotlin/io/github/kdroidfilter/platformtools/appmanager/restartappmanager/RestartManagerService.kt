/**
 * This file is inspired by the ProcessPhoenix project by Jake Wharton.
 * Source: https://github.com/JakeWharton/ProcessPhoenix/blob/trunk/process-phoenix/src/main/java/com/jakewharton/processphoenix/PhoenixService.java
 */

package io.github.kdroidfilter.platformtools.appmanager.restartappmanager

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.os.StrictMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class RestartManagerService : CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    suspend fun handleIntent(context: Context, intent: Intent?) {
        if (intent == null) {
            return
        }

        withContext(Dispatchers.IO) {
            val mainProcessPid = intent.getIntExtra(ProcessRestarter.KEY_MAIN_PROCESS_PID, -1)
            if (mainProcessPid != -1) {
                Process.killProcess(mainProcessPid) // Kill original main process
            }

            val nextIntent: Intent? = if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(ProcessRestarter.KEY_RESTART_INTENT, Intent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(ProcessRestarter.KEY_RESTART_INTENT)
            }

            if (nextIntent != null) {
                if (Build.VERSION.SDK_INT > 31) {
                    // Disable strict mode for out-of-process intents
                    StrictMode.setVmPolicy(
                        StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                            .permitUnsafeIntentLaunch()
                            .build()
                    )
                }

                if (Build.VERSION.SDK_INT >= 26) {
                    context.startForegroundService(nextIntent)
                } else {
                    context.startService(nextIntent)
                }
            }

            Runtime.getRuntime().exit(0) // Terminate process
        }
    }

    fun stop() {
        job.cancel()
    }
}



