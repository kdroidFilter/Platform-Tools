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
 * Determines whether the app has been granted permission to read contacts.
 *
 * On devices running Android M (API level 23) or higher, this method checks
 * if the `READ_CONTACTS` permission has been granted. On older versions, the
 * permission is automatically granted.
 *
 * @return true if the `READ_CONTACTS` permission is granted, or if the platform
 * version is lower than API level 23.
 */
fun hasReadContactsPermission(): Boolean {
    val context = ContextProvider.getContext()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED
    } else {
        true // On older versions of Android, permission is automatically granted.
    }
}

/**
 * Requests the `READ_CONTACTS` permission from the user. If the permission is already granted,
 * the `onGranted` callback is invoked. Otherwise, a request for the permission is initiated.
 * Note: Ensure to add the following permission in the app's manifest file:
 * <uses-permission android:name="android.permission.READ_CONTACTS" />
 *
 * @param onGranted A callback function executed when the `READ_CONTACTS` permission is granted.
 * @param onDenied A callback function executed when the `READ_CONTACTS` permission is denied.
 */
fun requestReadContactsPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
) {
    val context = ContextProvider.getContext()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            val requestId = PermissionCallbackManager.registerCallbacks(onGranted, onDenied)
            try {
                val intent = Intent(context, PermissionActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(PermissionActivity.EXTRA_REQUEST_ID, requestId)
                    putExtra(PermissionActivity.EXTRA_REQUEST_TYPE, PermissionActivity.REQUEST_TYPE_READ_CONTACTS)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("ReadContacts", "Error while launching PermissionActivity", e)
                PermissionCallbackManager.unregisterCallbacks(requestId)
                onDenied()
            }
        }
    } else {
        onGranted()
    }
}
