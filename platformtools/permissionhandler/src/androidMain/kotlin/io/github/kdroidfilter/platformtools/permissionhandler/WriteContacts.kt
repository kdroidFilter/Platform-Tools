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
 * Checks if the application has permission to write to contacts (WRITE_CONTACTS).
 *
 * @return true if permission is granted, false otherwise.
 */
fun hasWriteContactsPermission(): Boolean {
    val context = ContextProvider.getContext()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED
    } else {
        // Sur les anciennes versions d'Android, la permission est automatiquement accordée
        true
    }
}

/**
 * Requests permission to write to contacts (WRITE_CONTACTS).
 *
 * If permission is already granted, immediately invoke the [onGranted] callback.
 * Otherwise, launch the `PermissionActivity` to request permission from the user.
 *
 * Note: Ensure to add the following permission in the app's manifest file:
 * <uses-permission android:name="android.permission.WRITE_CONTACTS" />
 *
 * @param onGranted Callback invoked if permission is granted.
 * @param onDenied Callback invoked if permission is denied.
 */
fun requestWriteContactsPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
) {
    val context = ContextProvider.getContext()

    // Si la version d'Android >= M (6.0), on vérifie et/ou demande la permission
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission déjà accordée
            onGranted()
        } else {
            // On enregistre les callbacks (onGranted, onDenied) pour y accéder depuis PermissionActivity
            val requestId = PermissionCallbackManager.registerCallbacks(onGranted, onDenied)
            try {
                // On lance la PermissionActivity, qui gère la demande de permission
                val intent = Intent(context, PermissionActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(PermissionActivity.EXTRA_REQUEST_ID, requestId)
                    putExtra(
                        PermissionActivity.EXTRA_REQUEST_TYPE,
                        PermissionActivity.REQUEST_TYPE_WRITE_CONTACTS
                    )
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // En cas de problème (ex. Activity non trouvée), on nettoie et on invoque onDenied()
                Log.e("WriteContacts", "Error while launching PermissionActivity", e)
                PermissionCallbackManager.unregisterCallbacks(requestId)
                onDenied()
            }
        }
    } else {
        // Sur les anciennes versions, aucune vérification n'est nécessaire
        onGranted()
    }
}
