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
 * Checks if the application has permission to record audio.
 *
 * This method verifies whether the app has been granted the `RECORD_AUDIO` permission.
 *
 * @return true if the audio recording permission is granted, false otherwise.
 */
fun hasRecordAudioPermission(): Boolean {
    val context = ContextProvider.getContext()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    } else {
        // Permissions are automatically granted on devices below Android M
        true
    }
}

/**
 * Requests record audio permission for the application.
 *
 * This method initiates a permission request flow. If the permission is already granted,
 * it invokes the `onGranted` callback immediately. Otherwise, it starts the
 * `PermissionActivity` to request the permission from the user.
 *
 * Note: Ensure to add
 * <uses-permission android:name="android.permission.RECORD_AUDIO" />
 * in the app's manifest file.
 *
 * @param onGranted Callback to be invoked if the permission is granted.
 * @param onDenied Callback to be invoked if the permission is denied.
 */
fun requestRecordAudioPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
) {
    val context = ContextProvider.getContext()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)) {
            PackageManager.PERMISSION_GRANTED -> {
                onGranted()
            }
            else -> {
                val requestId = PermissionCallbackManager.registerCallbacks(onGranted, onDenied)

                try {
                    val intent = Intent(context, PermissionActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(PermissionActivity.EXTRA_REQUEST_ID, requestId)
                        putExtra(PermissionActivity.EXTRA_REQUEST_TYPE, PermissionActivity.REQUEST_TYPE_RECORD_AUDIO)
                    }

                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("AudioPermission", "Error while launching PermissionActivity", e)
                    e.printStackTrace()
                    PermissionCallbackManager.unregisterCallbacks(requestId)
                    onDenied()
                }
            }
        }
    } else {
        Log.d("AudioPermission", "No audio recording permission required for this Android version.")
        onGranted()
    }
}
