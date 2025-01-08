package io.github.kdroidfilter.platformtools

import com.kdroid.androidcontextprovider.ContextProvider

actual fun getAppVersion(): String {
    val context = ContextProvider.getContext()
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "Unknown"
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

fun getAppVersion(packageName: String): String {
    val context = ContextProvider.getContext()
    return try {
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName ?: "Unknown"
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}