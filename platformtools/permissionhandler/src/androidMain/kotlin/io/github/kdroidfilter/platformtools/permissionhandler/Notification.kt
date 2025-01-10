package io.github.kdroidfilter.platformtools.permissionhandler

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.kdroid.androidcontextprovider.ContextProvider

/**
 * Checks if the application has permission to post notifications.
 *
 * On devices running Android 13 (API level 33) or higher, this method verifies
 * whether the app has been granted the `POST_NOTIFICATIONS` permission. On lower
 * versions, it returns true by default as this permission is not required.
 *
 * @return true if the notification permission is granted, or if the platform version is lower than API level 33.
 */
fun hasNotificationPermission(): Boolean {
    val context = ContextProvider.getContext()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.areNotificationsEnabled()
    } else {
        true
    }
}

/**
 * Requests notification permission for the application. On Android 13 (API 33) and above, this method
 * initiates an intent to the system settings screen where the user can grant the notification permission.
 *
 * @param requestCode The request code used in the call to startActivityForResult to identify
 * the permission request. Defaults to 200 if not provided.
 */
fun requestNotificationPermission(requestCode: Int = 200) {
    val context = ContextProvider.getContext()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        if (context is android.app.Activity) {
            context.startActivityForResult(intent, requestCode)
        } else {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    } else {
        Log.d("NotificationPermission", "Notification permission not needed")
    }
}
