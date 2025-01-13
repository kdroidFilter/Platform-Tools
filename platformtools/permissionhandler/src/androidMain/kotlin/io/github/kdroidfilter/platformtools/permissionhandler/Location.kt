package io.github.kdroidfilter.platformtools.permissionhandler

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.kdroid.androidcontextprovider.ContextProvider
import io.github.kdroidfilter.platformtools.permissionhandler.permission.PermissionActivity
import io.github.kdroidfilter.platformtools.permissionhandler.permission.PermissionCallbackManager

/**
 * Vérifie si l'application a les permissions de localisation nécessaires.
 *
 * @param preciseLocation true pour vérifier ACCESS_FINE_LOCATION, false pour vérifier seulement ACCESS_COARSE_LOCATION
 * @return true si les permissions requises sont accordées
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
        // Pour les versions < M (23), les permissions sont accordées lors de l'installation
        // si elles sont déclarées dans le Manifest
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )

        packageInfo.requestedPermissions?.contains(permission) == true
    }
}

/**
 * Demande les permissions de localisation.
 *
 * Cette méthode vérifie d'abord si les permissions ont déjà été accordées. Si oui, le callback onGranted
 * est appelé. Sinon, elle déclenche une demande de permission via PermissionActivity, et le callback
 * approprié (onGranted ou onDenied) est appelé selon l'action de l'utilisateur.
 *
 * Note: Assurez-vous d'ajouter les permissions suivantes dans votre AndroidManifest.xml :
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 *
 * @param preciseLocation true pour demander ACCESS_FINE_LOCATION, false pour ACCESS_COARSE_LOCATION uniquement
 * @param onGranted Callback appelé quand la permission est accordée
 * @param onDenied Callback appelé quand la permission est refusée
 */
fun requestLocationPermission(
    preciseLocation: Boolean = true,
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    val context = ContextProvider.getContext()

    // Vérifie si la permission est déjà accordée
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
        Log.e("LocationPermission", "Erreur lors du lancement de PermissionActivity", e)
        PermissionCallbackManager.unregisterCallbacks(requestId)
        onDenied()
    }
}