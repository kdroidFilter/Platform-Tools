package io.github.kdroidfilter.platformtools.releasefetcher.github

import io.github.kdroidfilter.platformtools.OperatingSystem
import io.github.kdroidfilter.platformtools.getOperatingSystem
import io.github.kdroidfilter.platformtools.releasefetcher.config.client
import io.github.kdroidfilter.platformtools.releasefetcher.github.model.Release
import io.github.kdroidfilter.platformtools.releasefetcher.github.getCurrentAppVersion
import io.github.z4kn4fein.semver.toVersion
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class GitHubReleaseFetcher(
    private val owner: String,
    private val repo: String,
) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches the latest release from the GitHub API using Ktor.
     */
    suspend fun getLatestRelease(): Release? {
        return try {
            val response: HttpResponse = client.get("https://api.github.com/repos/$owner/$repo/releases/latest")

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.body()
                json.decodeFromString<Release>(responseBody)
            } else {
                // Handle different response codes as needed
                null
            }
        } catch (e: Exception) {
            // Log or handle the error as necessary
            e.printStackTrace()
            null
        }
    }

    /**
     * Checks for an update. If an update is available, executes [onUpdateNeeded] with the new version and changelog.
     */
    suspend fun checkForUpdate(
        onUpdateNeeded: (latestVersion: String, changelog: String) -> Unit,
    ) {
        val latestRelease = getLatestRelease()
        if (latestRelease != null) {
            val currentVersion = getCurrentAppVersion().toVersion(strict = false)
            val latestVersion = latestRelease.tag_name.toVersion(strict = false)

            if (latestVersion > currentVersion) {
                onUpdateNeeded(latestVersion.toString(), latestRelease.body)
            }
        }
    }

    /**
     * Returns the download link suitable for the current platform.
     */
    fun getDownloadLinkForPlatform(release: Release): String? {
        val operatingSystemFileTypes = mapOf(
            OperatingSystem.ANDROID to ".apk",
            OperatingSystem.WINDOWS to ".msi",
            OperatingSystem.LINUX to ".deb",
            OperatingSystem.MACOS to ".dmg"
        )

        val fileType = operatingSystemFileTypes[getOperatingSystem()] ?: return null

        // Find the corresponding asset
        val asset = release.assets.firstOrNull { it.name.endsWith(fileType, ignoreCase = true) }
        return asset?.browser_download_url
    }

}
