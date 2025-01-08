package io.github.kdroidfilter.platformtools.appmanager

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.kdroid.androidcontextprovider.ContextProvider
import io.github.kdroidfilter.platformtools.appmanager.silentinstall.InstallationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.coroutines.resume

actual fun getAppInstaller(): AppInstaller = ApkInstallerAndroid()

class ApkInstallerAndroid : AppInstaller {
    private val context = ContextProvider.getContext()

    override suspend fun canRequestInstallPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true // Before Android O, this permission was not required
        }
    }

    override suspend fun requestInstallPackagesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            suspendCancellableCoroutine<Unit> { cont ->
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // If the intent fails, ignore the exception to avoid blocking the coroutine
                    e.printStackTrace()
                } finally {
                    // Resume in any case
                    cont.resume(Unit)
                }
            }
        }
    }

    override suspend fun installApp(
        appFile: File,
        onResult: (success: Boolean, message: String?) -> Unit,
    ) {
        // 1) Check if installation from unknown sources is allowed
        if (!canRequestInstallPackages()) {
            // 2) Request permission if necessary
            requestInstallPackagesPermission()
        }

        // 3) Recheck after the request: if still not allowed, exit
        if (!canRequestInstallPackages()) {
            onResult(false, "Installation from unknown sources is not allowed.")
            return
        }

        // 4) If permission is granted, proceed with the installation
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                // Open the APK installer in a new task
                flags = Intent.FLAG_ACTIVITY_NEW_TASK

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // From Nougat (API 24), a FileProvider must be used
                    // and permissions must be granted to read the URI
                    val apkUri: Uri = FileProvider.getUriForFile(
                        context, "${context.packageName}.fileprovider", appFile
                    )
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    // Before Nougat, Uri.fromFile can still be used
                    setDataAndType(Uri.fromFile(appFile), "application/vnd.android.package-archive")
                }
            }

            // Launch the intent
            context.startActivity(intent)

            // Inform the caller that the installation has started
            onResult(true, "Installation started.")
        } catch (e: Exception) {
            // In case of an unexpected error
            e.printStackTrace()
            onResult(false, "Failed to start installation: ${e.message}")
        }
    }


    /**
     * Installs an APK file silently without user interaction.
     *
     * This method is intended for use by device owner apps as it leverages APIs
     * that require the app to be a device owner. It performs checks for device ownership,
     * file validity, and handles the silent installation process using the Android
     * `PackageInstaller` API.
     *
     * @param appFile The APK file to be installed. The file must be readable and exist in the file system.
     * @param onResult A callback function that is triggered with the result of the installation process.
     *        The callback provides a boolean indicating success or failure and an optional message detailing the result.
     */
    suspend fun installAppSilently(
        appFile: File,
        onResult: (success: Boolean, message: String?) -> Unit,
    ) {

        fun isDeviceOwner(): Boolean {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            return devicePolicyManager.isDeviceOwnerApp(context.packageName)
        }

        // 1) Check if the app is Device Owner
        if (!isDeviceOwner()) {
            onResult(false, "Slient Install is allowed only for Device Owner App.")
            return
        }

        // 2) Check if the APK file is valid
        if (!appFile.exists() || !appFile.canRead()) {
            onResult(false, "APK file not found or unreadable: ${appFile.absolutePath}")
            return
        }

        // 3) If everything is valid, use PackageInstaller
        try {
            InstallationManager.setInstallCallback(onResult)

            val context = ContextProvider.getContext()
            val packageInstaller = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)

            withContext(Dispatchers.IO) {
                FileInputStream(appFile).use { inputStream ->
                    session.openWrite("package", 0, appFile.length()).use { outputStream ->
                        val buffer = ByteArray(65536)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } >= 0) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        session.fsync(outputStream)
                    }
                }
            }

            // Launch silent installation via a broadcast
            val intent = Intent("${context.packageName}.APP_INSTALL_STATUS")
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val intentSender = pendingIntent.intentSender

            session.commit(intentSender)
            session.close()

            // If we reach here, the installation is in progress.
            onResult(true, "Silent installation started.")
        } catch (e: IOException) {
            Log.e("InstallApp", "I/O Error: ${e.message}", e)
            onResult(false, "I/O Error during installation: ${e.message}")
        } catch (e: SecurityException) {
            Log.e("InstallApp", "Insufficient permissions: ${e.message}", e)
            onResult(false, "Insufficient permissions: ${e.message}")
        } catch (e: Exception) {
            Log.e("InstallApp", "Unexpected error: ${e.message}", e)
            onResult(false, "Unexpected error: ${e.message}")
        }
    }

    /**
     * Method to uninstall an app by package name.
     */
    override suspend fun uninstallApp(packageName: String, onResult: (success: Boolean, message: String?) -> Unit) {
        try {
            val packageURI: Uri = Uri.parse("package:$packageName")
            val uninstallIntent = Intent(Intent.ACTION_DELETE, packageURI)
            context.startActivity(uninstallIntent)
            onResult(true, "Uninstall process started for package: $packageName.")
        } catch (e: Exception) {
            Log.e("UninstallApp", "Error during uninstallation: ${e.message}", e)
            onResult(false, "Error during uninstallation: ${e.message}")
        }
    }

    /**
     * Method to uninstall the current app.
     */
    override suspend fun uninstallApp(onResult: (success: Boolean, message: String?) -> Unit) {
        try {
            val packageURI: Uri = Uri.parse("package:" + context.packageName)
            val uninstallIntent = Intent(Intent.ACTION_DELETE, packageURI)
            context.startActivity(uninstallIntent)
            onResult(true, "Uninstall process started for the current app.")
        } catch (e: Exception) {
            Log.e("UninstallApp", "Error during uninstallation: ${e.message}", e)
            onResult(false, "Error during uninstallation: ${e.message}")
        }
    }

}
