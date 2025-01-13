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
 * Checks if the app has the required location permissions.
 *
 * @param preciseLocation true to check for ACCESS_FINE_LOCATION, false to check only ACCESS_COARSE_LOCATION
 * @return true if the required permissions are granted
 */
fun hasLocationPermission(preciseLocation: Boolean = true): Boolean {
    val context = ContextProvider.getContext()
    val permission = if (preciseLocation) {
        Manifest.permission.ACCESS_FINE_LOCATION
    } else {
        Manifest.permission.ACCESS_COARSE_LOCATION
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    } else {
        // For versions < M (23), permissions are granted at installation
        // if declared in the Manifest
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )

        packageInfo.requestedPermissions?.contains(permission) == true
    }
}

/**
 * Requests location permissions.
 *
 * This method first checks if the permissions are already granted. If yes, the onGranted
 * callback is invoked. Otherwise, it triggers a permission request via PermissionActivity,
 * and the appropriate callback (onGranted or onDenied) is invoked based on the user's action.
 *
 * Note: Ensure you add the following permissions in your AndroidManifest.xml:
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 *
 * @param preciseLocation true to request ACCESS_FINE_LOCATION, false for ACCESS_COARSE_LOCATION only
 * @param onGranted Callback invoked when the permission is granted
 * @param onDenied Callback invoked when the permission is denied
 */
fun requestLocationPermission(
    preciseLocation: Boolean = true,
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    val context = ContextProvider.getContext()

    // Check if the permission is already granted
    if (hasLocationPermission(preciseLocation)) {
        onGranted()
        return
    }

    val requestId = PermissionCallbackManager.registerCallbacks(onGranted, onDenied)

    try {
        val intent = Intent(context, PermissionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(PermissionActivity.EXTRA_REQUEST_ID, requestId)
            putExtra(PermissionActivity.EXTRA_REQUEST_TYPE, PermissionActivity.REQUEST_TYPE_LOCATION)
            putExtra(PermissionActivity.EXTRA_PRECISE_LOCATION, preciseLocation)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("LocationPermission", "Error launching PermissionActivity", e)
        PermissionCallbackManager.unregisterCallbacks(requestId)
        onDenied()
    }
}
