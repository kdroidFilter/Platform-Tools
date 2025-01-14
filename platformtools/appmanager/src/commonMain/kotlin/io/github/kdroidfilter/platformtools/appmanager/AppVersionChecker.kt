package io.github.kdroidfilter.platformtools.appmanager

import io.github.kdroidfilter.platformtools.getAppVersion
import io.github.kdroidfilter.platformtools.getCacheDir
import java.io.File

private const val VERSION_FILE_NAME = "app_version.txt"

/**
 * Checks if the app version has changed since the last execution.
 * @return `true` if the version has changed (updated), `false` otherwise.
 */
fun hasAppVersionChanged(): Boolean {
    val currentVersion = getAppVersion()
    val versionFile = File(getCacheDir(), VERSION_FILE_NAME)

    val oldVersion: String? = if (versionFile.exists()) {
        versionFile.readText().trim()
    } else null

    // 1) If this is the first installation, we store the current version and return false
    if (oldVersion.isNullOrEmpty()) {
        versionFile.writeText(currentVersion)
        return false
    }

    // 2) Otherwise, we compare
    val hasChanged = oldVersion != currentVersion

    // 3) If it has changed, we update the file
    if (hasChanged) {
        versionFile.writeText(currentVersion)
    }

    return hasChanged
}


/**
 * Determines if the application is being installed for the first time.
 *
 * The method checks the presence of a dedicated version file in the cache directory.
 * If the file does not exist, it saves the current application version in the file
 * and returns `true`, indicating that this is the first installation. Otherwise, it
 * returns `false` to indicate that the application has been installed previously.
 *
 * @return `true` if this is the first installation of the application, `false` otherwise.
 */
fun isFirstInstallation(): Boolean {
    val versionFile = File(getCacheDir(), VERSION_FILE_NAME)

    // If the file does not exist, this is the first install
    if (!versionFile.exists()) {
        // Write the current version (or any content)
        versionFile.writeText(getAppVersion())
        return true
    }

    // Otherwise, not the first installation
    return false
}