/**
 * This file is inspired by the ProcessPhoenix project by Jake Wharton.
 * Source: https://github.com/JakeWharton/ProcessPhoenix/blob/trunk/process-phoenix/src/main/java/com/jakewharton/processphoenix/ProcessPhoenix.java
 */

package io.github.kdroidfilter.platformtools.appmanager.restartappmanager

import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import io.github.kdroidfilter.platformtools.appmanager.restartappmanager.ProcessRestarter.triggerRebirth

/**
 * ProcessRestarter facilitates restarting your application process. This should only be used for
 * things like fundamental state changes in your debug builds (e.g., changing from staging to
 * production).
 *
 * Trigger process recreation by calling [triggerRebirth] with a [Context] instance.
 */
object ProcessRestarter {

    internal const val KEY_RESTART_INTENT = "process_restart_intent"
    internal const val KEY_RESTART_INTENTS = "process_restart_intents"
    internal const val KEY_MAIN_PROCESS_PID = "process_main_process_pid"

    /**
     * Call to restart the application process using the default activity as an intent.
     *
     * Behavior of the current process after invoking this method is undefined.
     */
    fun triggerRebirth(context: Context) {
        triggerRebirth(context, getRestartIntent(context))
    }

    /**
     * Call to restart the application process using the provided Activity Class.
     *
     * Behavior of the current process after invoking this method is undefined.
     */
    fun triggerRebirth(context: Context, targetClass: Class<out Activity>) {
        val nextIntent = Intent(context, targetClass)
        triggerRebirth(context, nextIntent)
    }

    /**
     * Call to restart the application process using the specified intents.
     *
     * Behavior of the current process after invoking this method is undefined.
     */
    fun triggerRebirth(context: Context, vararg nextIntents: Intent) {
        require(nextIntents.isNotEmpty()) { "Intents cannot be empty" }

        nextIntents[0].addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val intent = Intent(context, RestartManagerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putParcelableArrayListExtra(KEY_RESTART_INTENTS, ArrayList(nextIntents.toList()))
            putExtra(KEY_MAIN_PROCESS_PID, Process.myPid())
        }

        context.startActivity(intent)
    }

    /**
     * Call to restart the application process using the provided Service Class.
     *
     * Behavior of the current process after invoking this method is undefined.
     */
    fun triggerServiceRebirth(context: Context, targetClass: Class<out Service>) {
        val nextIntent = Intent(context, targetClass)
        triggerServiceRebirth(context, nextIntent)
    }

    /**
     * Call to restart the application process using the specified Service intent.
     *
     * Behavior of the current process after invoking this method is undefined.
     */
    fun triggerServiceRebirth(context: Context, nextIntent: Intent) {
        val intent = Intent(context, RestartManagerService::class.java).apply {
            putExtra(KEY_RESTART_INTENT, nextIntent)
            putExtra(KEY_MAIN_PROCESS_PID, Process.myPid())
        }

        context.startService(intent)
    }

    private fun getRestartIntent(context: Context): Intent {
        val packageName = context.packageName
        val packageManager = context.packageManager

        val defaultIntent = if (packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        ) {
            packageManager.getLeanbackLaunchIntentForPackage(packageName)
        } else {
            packageManager.getLaunchIntentForPackage(packageName)
        }

        return defaultIntent ?: throw IllegalStateException(
            "Unable to determine default activity for $packageName. " +
                    "Does an activity specify the DEFAULT category in its intent filter?"
        )
    }

    /**
     * Checks if the current process is a temporary Process.
     * This can be used to avoid initialization of unused resources or to prevent running code that
     * is not multi-process ready.
     *
     * @return true if the current process is a temporary Process
     */
    fun isTemporaryProcess(context: Context): Boolean {
        val currentPid = Process.myPid()
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = manager.runningAppProcesses

        return runningProcesses?.any { processInfo ->
            processInfo.pid == currentPid && processInfo.processName.endsWith(":phoenix")
        } ?: false
    }
}

