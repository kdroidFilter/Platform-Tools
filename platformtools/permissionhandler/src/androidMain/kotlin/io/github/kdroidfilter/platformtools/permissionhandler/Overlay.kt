package io.github.kdroidfilter.platformtools.permissionhandler

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.kdroid.androidcontextprovider.ContextProvider
import io.github.kdroidfilter.platformtools.permissionhandler.manager.PermissionActivity
import io.github.kdroidfilter.platformtools.permissionhandler.manager.PermissionCallbackManager

/**
 * Checks if the application has permission to draw overlays on top of other apps.
 *
 * On devices running Android Marshmallow (API level 23) or higher, this method verifies
 * whether the app has been granted the `SYSTEM_ALERT_WINDOW` permission. On lower
 * versions, it returns true by default as this permission is not required.
 *
 * @return true if the overlay permission is granted, or if the platform version is lower than API level 23.
 */
fun hasOverlayPermission(): Boolean {
    val context = ContextProvider.getContext()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else {
        true
    }
}

/**
 * Requests overlay permission for the application. On Android M (API 23) and above, this method
 * initiates an intent to the system settings screen where the user can grant the overlay permission.
 *
 * Note: Ensure to add the following permission in the app's manifest file:
 * <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
 *
 * @param onGranted Callback executed when the permission is granted.
 * @param onDenied Callback executed when the permission is denied.
 */
fun requestOverlayPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
) {
    val context = ContextProvider.getContext()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Check if the permission is already granted
        if (hasOverlayPermission()) {
            onGranted()
        } else {
            val requestId = PermissionCallbackManager.registerCallbacks(onGranted, onDenied)

            // Create and launch PermissionActivity
            try {
                val intent = Intent(context, PermissionActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(PermissionActivity.EXTRA_REQUEST_ID, requestId)
                    putExtra(PermissionActivity.EXTRA_REQUEST_TYPE, PermissionActivity.REQUEST_TYPE_OVERLAY)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("OverlayPermission", "Error while launching PermissionActivity", e)
                PermissionCallbackManager.unregisterCallbacks(requestId)
                onDenied()
            }
        }
    } else {
        Log.d("OverlayPermission", "Overlay permission not needed")
        onGranted()
    }
}

