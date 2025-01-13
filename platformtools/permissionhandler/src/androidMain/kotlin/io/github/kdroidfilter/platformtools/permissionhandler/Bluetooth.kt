package io.github.kdroidfilter.platformtools.permissionhandler

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.kdroid.androidcontextprovider.ContextProvider
import io.github.kdroidfilter.platformtools.permissionhandler.manager.PermissionActivity
import io.github.kdroidfilter.platformtools.permissionhandler.manager.PermissionCallbackManager

/**
 * Checks if the application has permission to use Bluetooth.
 *
 * This method verifies whether the app has been granted the `BLUETOOTH_CONNECT` permission.
 * Required for Android 12 (API level 31) and above.
 *
 * @return true if the Bluetooth permission is granted, false otherwise.
 */
fun hasBluetoothPermission(): Boolean {
    val context = ContextProvider.getContext()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    } else {
        // No explicit Bluetooth permissions required for devices below Android 12
        true
    }
}

/**
 * Requests Bluetooth permission for the application.
 *
 * This method initiates a permission request flow. If the permission is already granted,
 * it invokes the `onGranted` callback immediately. Otherwise, it starts the
 * `PermissionActivity` to request the permission from the user.
 *
 * Note: Ensure to add the following permissions in the app's manifest file:
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 * <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" android:required="false"/>
 *
 * @param onGranted Callback to be invoked if the permission is granted.
 * @param onDenied Callback to be invoked if the permission is denied.
 */
fun requestBluetoothPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
) {
    val context = ContextProvider.getContext()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)) {
            PackageManager.PERMISSION_GRANTED -> {
                onGranted()
            }
            else -> {
                val requestId = PermissionCallbackManager.registerCallbacks(onGranted, onDenied)

                try {
                    val intent = Intent(context, PermissionActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(PermissionActivity.EXTRA_REQUEST_ID, requestId)
                        putExtra(PermissionActivity.EXTRA_REQUEST_TYPE, PermissionActivity.REQUEST_TYPE_BLUETOOTH)
                    }

                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("BluetoothPermission", "Error while launching PermissionActivity", e)
                    e.printStackTrace()
                    PermissionCallbackManager.unregisterCallbacks(requestId)
                    onDenied()
                }
            }
        }
    } else {
        Log.d("BluetoothPermission", "No Bluetooth permission required for this Android version.")
        onGranted()
    }
}
