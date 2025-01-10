package io.github.kdroidfilter.platformtools.permissionhandler

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.kdroid.androidcontextprovider.ContextProvider

/**
 * Checks if the application has permission to install APK files.
 *
 * On devices running Android Oreo (API level 26) or higher, this method verifies
 * whether the app has been granted the `REQUEST_INSTALL_PACKAGES` permission. On lower
 * versions, it returns true by default as this permission is not required.
 *
 * @return true if the install permission is granted, or if the platform version is lower than API level 26.
 */
fun hasInstallPermission(): Boolean {
    val context = ContextProvider.getContext()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.packageManager.canRequestPackageInstalls()
    } else {
        true
    }
}

/**
 * Requests permission to install APK files. On Android Oreo (API 26) and above, this method
 * initiates an intent to the system settings screen where the user can grant the install permission.
 *
 * @param requestCode The request code used in the call to startActivityForResult to identify
 * the permission request. Defaults to 300 if not provided.
 */
fun requestInstallPermission(requestCode: Int = 300) {
    val context = ContextProvider.getContext()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
        if (context is android.app.Activity) {
            context.startActivityForResult(intent, requestCode)
        } else {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    } else {
        Log.d("InstallPermission", "Install permission not needed")
    }
}
