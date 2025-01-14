package io.github.kdroidfilter.platformtools.permissionhandler

import android.content.Intent
import android.os.Build
import android.util.Log
import com.kdroid.androidcontextprovider.ContextProvider
import io.github.kdroidfilter.platformtools.permissionhandler.manager.PermissionActivity
import io.github.kdroidfilter.platformtools.permissionhandler.manager.PermissionCallbackManager

/**
 * Checks if the application has permission to install APK files.
 *
 * On devices running Android Oreo (API level 26) or higher, this method verifies
 * whether the app has been granted the `REQUEST_INSTALL_PACKAGES` permission. On lower
 * versions, it returns true by default as this permission is not required.
 *
 * @return true if the install permission is granted, or if the platform version is lower than API level 26.
 */
actual fun hasInstallPermission(): Boolean {
    val context = ContextProvider.getContext()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.packageManager.canRequestPackageInstalls()
    } else {
        true
    }
}


/**
 * Requests the install permission required to allow the app to install APK files from unknown sources.
 * This method checks if the required permission has already been granted. If granted, the `onGranted`
 * callback is invoked. If not, the method triggers a permission request workflow via a dedicated
 * `PermissionActivity`, and the appropriate callback (`onGranted` or `onDenied`) is invoked based
 * on the user's action.
 *
 * Note: Ensure to add <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
 * to your AndroidManifest.xml file. *
 * @param onGranted A callback function that is invoked when the install permission is granted.
 * @param onDenied A callback function that is invoked when the install permission is denied.
 */
actual fun requestInstallPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
) {
    val context = ContextProvider.getContext()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Check if the permission is already granted
        if (hasInstallPermission()) {
            onGranted()
        } else {
            val requestId = PermissionCallbackManager.registerCallbacks(onGranted, onDenied)

            // Create and launch PermissionActivity
            try {
                val intent = Intent(context, PermissionActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(PermissionActivity.EXTRA_REQUEST_ID, requestId)
                    putExtra(PermissionActivity.EXTRA_REQUEST_TYPE, PermissionActivity.REQUEST_TYPE_INSTALL)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("InstallPermission", "Error while launching PermissionActivity", e)
                PermissionCallbackManager.unregisterCallbacks(requestId)
                onDenied()
            }
        }
    } else {
        Log.d("InstallPermission", "Install permission not needed")
        onGranted()
    }
}

