/**
 * This file is inspired by the ProcessPhoenix project by Jake Wharton.
 * Source: https://github.com/JakeWharton/ProcessPhoenix/blob/trunk/process-phoenix/src/main/java/com/jakewharton/processphoenix/PhoenixActivity.java
 */

package io.github.kdroidfilter.platformtools.appmanager.restartappmanager

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.os.StrictMode

class RestartManagerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kill original main process
        val mainProcessPid = intent.getIntExtra(ProcessRestarter.KEY_MAIN_PROCESS_PID, -1)
        if (mainProcessPid != -1) {
            Process.killProcess(mainProcessPid)
        }

        val intents = intent.getParcelableArrayListExtra<Intent>(ProcessRestarter.KEY_RESTART_INTENTS)?.toTypedArray()
        if (intents != null) {
            if (Build.VERSION.SDK_INT > 31) {
                // Disable strict mode complaining about out-of-process intents. Normally you save and restore
                // the original policy, but this process will die almost immediately after the offending call.
                StrictMode.setVmPolicy(
                    StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                        .permitUnsafeIntentLaunch()
                        .build()
                )
            }

            startActivities(intents)
        }

        finish()
        Runtime.getRuntime().exit(0) // Kill kill kill!
    }
}


