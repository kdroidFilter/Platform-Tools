package io.github.kdroidfilter.platformtools.appmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.content.pm.PackageInstaller

/**
 * Receives and processes the result of an app installation via a broadcast.
 *
 * This class listens for broadcast intents that contain the result of a package
 * installation initiated through the Android PackageInstaller API. It extracts
 * the status and corresponding message, logs the information, and notifies
 * `InstallationManager` to invoke the corresponding callback.
 *
 * Inherits from `BroadcastReceiver` and overrides the `onReceive` method to handle
 * the incoming intents with installation results.
 */
class InstallResultReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        val success = (status == PackageInstaller.STATUS_SUCCESS)

        Log.i("InstallResultReceiver", "Installation completed. Success: $success, message: $message")

        // Notify the installation manager
        InstallationManager.notifyInstallResult(success, message)
    }
}
