package io.github.kdroidfilter.platformtools.permissionhandler.notification

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.kdroid.androidcontextprovider.ContextProvider

/**
 * Checks if the application has permission to post notifications.
 *
 * On devices running Android 13 (API level 33) or higher, this method
 * verifies whether the app has been granted the `POST_NOTIFICATIONS`
 * permission. On lower versions, it returns true by default as this
 * permission is not required.
 *
 * @return true if the notification permission is granted, or if the
 *    platform version is lower than API level 33.
 */
actual fun hasNotificationPermission(): Boolean {
    val context = ContextProvider.getContext()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.areNotificationsEnabled()
    } else {
        true
    }
}

/**
 * Requests notification permission for the application. On Android 13
 * (API 33) and above, this method initiates an intent to the system
 * settings screen where the user can grant the notification permission.
 *
 * Note: Ensure to add <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
 * in the app's manifest file.
 *
 */
actual fun requestNotificationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
) {
    val context = ContextProvider.getContext()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Check if the permission is already granted
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
            PackageManager.PERMISSION_GRANTED -> {
                onGranted()
            }
            else -> {
                // Register the callbacks
                val requestId = PermissionCallbackManager.registerCallbacks(onGranted, onDenied)

                // Create and launch PermissionActivity
                try {
                    val intent = Intent(context, PermissionActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(PermissionActivity.EXTRA_REQUEST_ID, requestId)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("NotificationPermission", "Error while launching PermissionActivity", e)
                    PermissionCallbackManager.unregisterCallbacks(requestId)
                    onDenied()
                }
            }
        }
    } else {
        // No permission required before TIRAMISU
        Log.d("NotificationPermission", "No notification permission required for this Android version.")
        onGranted()
    }
}