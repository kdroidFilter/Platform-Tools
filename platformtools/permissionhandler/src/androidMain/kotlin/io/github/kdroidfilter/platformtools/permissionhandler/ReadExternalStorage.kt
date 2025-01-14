// ReadExternalStoragePermission.kt
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

enum class MediaType {
    IMAGES,
    VIDEO,
    AUDIO
}

/**
 * Checks if the application has permission to read external storage or specific media types.
 *
 * For Android versions below 13, it checks for `READ_EXTERNAL_STORAGE`.
 * For Android 13 and above, it checks for specific media permissions.
 *
 * @param mediaTypes The specific media types to check permissions for (only applicable for Android 13+).
 * @return true if all required permissions are granted, false otherwise.
 */
fun hasReadExternalStoragePermission(mediaTypes: Set<MediaType> = emptySet()): Boolean {
    val context = ContextProvider.getContext()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13 and above: Check specific media permissions
        mediaTypes.all { mediaType ->
            when (mediaType) {
                MediaType.IMAGES -> ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED

                MediaType.VIDEO -> ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED

                MediaType.AUDIO -> ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Below Android 13 but Android M and above: Check READ_EXTERNAL_STORAGE
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // Permissions are automatically granted on devices below Android M
        true
    }
}

/**
 * Requests read external storage permission for the application.
 *
 * For Android versions below 13, it requests `READ_EXTERNAL_STORAGE`.
 * For Android 13 and above, it requests specific media permissions based on the provided media types.
 *
 * Note: Ensure to add the necessary permissions in the app's manifest file:
 * - For Android <13:
 *   <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
 * - For Android 13+:
 *   <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
 *   <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
 *   <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
 *
 * @param mediaTypes The specific media types to request permissions for (only applicable for Android 13+).
 * @param onGranted Callback to be invoked if all requested permissions are granted.
 * @param onDenied Callback to be invoked if any of the requested permissions are denied.
 */
fun requestReadExternalStoragePermission(
    mediaTypes: Set<MediaType> = emptySet(),
    onGranted: () -> Unit,
    onDenied: () -> Unit,
) {
    val context = ContextProvider.getContext()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13 and above: Request specific media permissions
        if (mediaTypes.isEmpty()) {
            Log.e("ReadExternalStorage", "No media types specified for Android 13+")
            onDenied()
            return
        }

        val permissionsToRequest = mediaTypes.map { mediaType ->
            when (mediaType) {
                MediaType.IMAGES -> Manifest.permission.READ_MEDIA_IMAGES
                MediaType.VIDEO -> Manifest.permission.READ_MEDIA_VIDEO
                MediaType.AUDIO -> Manifest.permission.READ_MEDIA_AUDIO
            }
        }.toSet()

        val allGranted = permissionsToRequest.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            onGranted()
            return
        }

        val requestId = PermissionCallbackManager.registerCallbacks(onGranted, onDenied)

        try {
            val intent = Intent(context, PermissionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(PermissionActivity.EXTRA_REQUEST_ID, requestId)
                putExtra(PermissionActivity.EXTRA_REQUEST_TYPE, PermissionActivity.REQUEST_TYPE_READ_MEDIA)
                putExtra(PermissionActivity.EXTRA_MEDIA_TYPES, mediaTypes.map { it.name }.toTypedArray())
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("ReadExternalStorage", "Error while launching PermissionActivity", e)
            e.printStackTrace()
            PermissionCallbackManager.unregisterCallbacks(requestId)
            onDenied()
        }

    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Below Android 13 but Android M and above: Request READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onGranted()
            return
        }

        val requestId = PermissionCallbackManager.registerCallbacks(onGranted, onDenied)

        try {
            val intent = Intent(context, PermissionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(PermissionActivity.EXTRA_REQUEST_ID, requestId)
                putExtra(PermissionActivity.EXTRA_REQUEST_TYPE, PermissionActivity.REQUEST_TYPE_READ_EXTERNAL_STORAGE)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("ReadExternalStorage", "Error while launching PermissionActivity", e)
            e.printStackTrace()
            PermissionCallbackManager.unregisterCallbacks(requestId)
            onDenied()
        }

    } else {
        // Permissions are automatically granted on devices below Android M
        Log.d("ReadExternalStorage", "No read external storage permission required for this Android version.")
        onGranted()
    }
}
