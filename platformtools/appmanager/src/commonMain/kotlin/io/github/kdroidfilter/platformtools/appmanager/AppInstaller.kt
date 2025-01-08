package io.github.kdroidfilter.platformtools.appmanager

import java.io.File

interface AppInstaller {
    suspend fun canRequestInstallPackages(): Boolean
    suspend fun requestInstallPackagesPermission()
    suspend fun installApp(appFile: File, onResult: (success: Boolean, message: String?) -> Unit)
    suspend fun uninstallApp(packageName: String, onResult: (success: Boolean, message: String?) -> Unit)
    suspend fun uninstallApp(onResult: (success: Boolean, message: String?) -> Unit)
}


expect fun getAppInstaller(): AppInstaller