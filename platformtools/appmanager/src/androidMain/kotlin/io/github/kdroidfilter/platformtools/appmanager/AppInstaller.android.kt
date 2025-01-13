package io.github.kdroidfilter.platformtools.appmanager

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.kdroid.androidcontextprovider.ContextProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual fun getAppInstaller(): AppInstaller = ApkInstallerAndroid()

class ApkInstallerAndroid : AppInstaller {
    private val context = ContextProvider.getContext()




    override suspend fun installApp(
        appFile: File,
        onResult: (success: Boolean, message: String?) -> Unit,
    ) {

        try {
            // Use a suspended coroutine to wait for the installation result
            suspendCancellableCoroutine { continuation ->
                val packageManager = context.packageManager

                // Extract the package name from the APK file
                val packageInfo = packageManager.getPackageArchiveInfo(appFile.absolutePath, 0)
                val targetPackageName = packageInfo?.packageName

                if (targetPackageName == null) {
                    onResult(false, "Failed to extract package name from APK file.")
                    continuation.resume(Unit)
                    return@suspendCancellableCoroutine
                }

                // Retrieve current package information for the target package before installation
                val currentPackageInfo = try {
                    packageManager.getPackageInfo(targetPackageName, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }

                // Retrieve the current versionCode or installation date
                val currentVersionCode = currentPackageInfo?.versionCode ?: -1
                val currentInstallTime = currentPackageInfo?.firstInstallTime ?: -1

                // Create the intent to launch the APK installation
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        // From Nougat (API 24) onwards, use FileProvider
                        val apkUri: Uri = FileProvider.getUriForFile(
                            context, "${context.packageName}.fileprovider", appFile
                        )
                        setDataAndType(apkUri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } else {
                        // Before Nougat, use Uri.fromFile
                        setDataAndType(Uri.fromFile(appFile), "application/vnd.android.package-archive")
                    }
                }

                try {
                    // Launch the installation intent
                    context.startActivity(installIntent)

                    // Wait for a short moment to allow the installation to start
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Check if the app has been installed or updated
                        val updatedPackageInfo = try {
                            packageManager.getPackageInfo(targetPackageName, 0)
                        } catch (e: PackageManager.NameNotFoundException) {
                            null
                        }

                        if (updatedPackageInfo != null) {
                            // Compare the versionCode or installation date
                            val isUpdated = when {
                                // If the versionCode has increased, it's a successful update
                                updatedPackageInfo.versionCode > currentVersionCode -> true
                                // If the installation date has changed, it's a successful update
                                updatedPackageInfo.firstInstallTime > currentInstallTime -> true
                                // Otherwise, the installation/update failed
                                else -> false
                            }

                            if (isUpdated) {
                                onResult(true, "Installation/update successful.")
                            } else {
                                onResult(false, "The application was not updated.")
                            }
                        } else {
                            onResult(false, "The application was not installed.")
                        }

                        // Resume the coroutine
                        continuation.resume(Unit)
                    }, 5000) // Wait 5 seconds before checking

                } catch (e: Exception) {
                    // Handle errors during the installation intent launch
                    onResult(false, "Failed to start installation: ${e.message}")
                    continuation.resumeWithException(e)
                }

                // Handle coroutine cancellation
                continuation.invokeOnCancellation {
                    // Clean up if necessary
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false, "Installation failed: ${e.message}")
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
