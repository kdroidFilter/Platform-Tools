package io.github.kdroidfilter.platformtools.permissionhandler

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.kdroid.androidcontextprovider.ContextProvider
import io.github.kdroidfilter.platformtools.permissionhandler.manager.PermissionActivity
import io.github.kdroidfilter.platformtools.permissionhandler.manager.PermissionCallbackManager

/**
 * Checks if the app has the background location permission.
 *
 * @return true if the ACCESS_BACKGROUND_LOCATION permission is granted
 */
fun hasBackgroundLocationPermission(): Boolean {
    val context = ContextProvider.getContext()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    } else {
        // For versions < Q (29), ACCESS_BACKGROUND_LOCATION does not exist separately
        true
    }
}

/**
 * Requests the background location permission.
 * Before requesting ACCESS_BACKGROUND_LOCATION, ensure ACCESS_FINE_LOCATION is already granted,
 * use hasLocationPermission and requestLocationPermission()
 *
 * Note: Make sure to add the following permission in your AndroidManifest.xml:
 * <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
 *
 * @param onGranted Callback invoked when the permission is granted
 * @param onDenied Callback invoked when the permission is denied
 */
fun requestBackgroundLocationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    val context = ContextProvider.getContext()

    // Check if the permission is already granted
    if (hasBackgroundLocationPermission()) {
        onGranted()
        return
    }

    // Check if the OS version requires this permission
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        // ACCESS_BACKGROUND_LOCATION is not required below Q
        onGranted()
        return
    }

    val requestId = PermissionCallbackManager.registerCallbacks(onGranted, onDenied)

    try {
        val intent = Intent(context, PermissionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(PermissionActivity.EXTRA_REQUEST_ID, requestId)
            putExtra(PermissionActivity.EXTRA_REQUEST_TYPE, PermissionActivity.REQUEST_TYPE_BACKGROUND_LOCATION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("BackgroundLocationPerm", "Error launching PermissionActivity", e)
        PermissionCallbackManager.unregisterCallbacks(requestId)
        onDenied()
    }
}
