package io.github.kdroidfilter.platformtools.releasefetcher.github

import io.github.kdroidfilter.platformtools.Platform
import io.github.kdroidfilter.platformtools.getAppVersion
import io.github.kdroidfilter.platformtools.getPlatform
import io.github.kdroidfilter.platformtools.releasefetcher.config.client
import io.github.kdroidfilter.platformtools.releasefetcher.github.model.Release
import io.github.z4kn4fein.semver.toVersion
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GitHubReleaseFetcher(
    private val owner: String,
    private val repo: String,
) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches the latest release from the GitHub API using Ktor.
     */
    suspend fun getLatestRelease(): Release? = withContext(Dispatchers.IO) {
        try {
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
            val currentVersion = getAppVersion().toVersion(strict = false)
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
        val platformFileTypes = mapOf(
            Platform.ANDROID to ".apk",
            Platform.WINDOWS to ".msi",
            Platform.LINUX to ".deb",
            Platform.MAC to ".dmg"
        )

        val fileType = platformFileTypes[getPlatform()] ?: return null

        // Find the corresponding asset
        val asset = release.assets.firstOrNull { it.name.endsWith(fileType, ignoreCase = true) }
        return asset?.browser_download_url
    }

}
