package io.github.kdroidfilter.platformtools.permissionhandler

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.kdroid.androidcontextprovider.ContextProvider

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
 * @param requestCode The request code used in the call to startActivityForResult to identify
 * the permission request. Defaults to 100 if not provided.
 */
fun requestOverlayPermission(requestCode: Int = 100) {
    val context = ContextProvider.getContext()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        if (context is android.app.Activity) {
            context.startActivityForResult(intent, requestCode)
        } else {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    } else {
        Log.d("OverlayPermission", "Overlay permission not needed ")
    }
}
