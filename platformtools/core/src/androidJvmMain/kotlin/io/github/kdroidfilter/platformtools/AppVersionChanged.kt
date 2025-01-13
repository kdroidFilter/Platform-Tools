package io.github.kdroidfilter.platformtools

import java.io.File

private const val VERSION_FILE_NAME = "app_version.txt"

/**
 * Checks if the app version has changed since the last execution.
 * @return `true` if the version has changed (updated), `false` otherwise.
 */
fun hasAppVersionChanged(): Boolean {
    val currentVersion = getAppVersion()

    // Retrieve the cache directory from the expect function
    val cacheDir = getCacheDir()
    val versionFile = File(cacheDir, VERSION_FILE_NAME)

    // Read the last stored version (if it exists)
    val oldVersion: String? = if (versionFile.exists()) {
        versionFile.readText().trim()
    } else null

    // Compare versions
    val hasChanged = (oldVersion != currentVersion)

    // If the version has changed (or is not yet stored), update the file
    if (hasChanged) {
        versionFile.writeText(currentVersion)
    }

    return hasChanged
}
