package io.github.kdroidfilter.platformtools.releasefetcher.github

import io.github.kdroidfilter.platformtools.OperatingSystem
import io.github.kdroidfilter.platformtools.getAppVersion
import io.github.kdroidfilter.platformtools.getCacheDir
import io.github.kdroidfilter.platformtools.getOperatingSystem
import io.github.kdroidfilter.platformtools.releasefetcher.github.model.Release

import io.github.z4kn4fein.semver.toVersion
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class GitHubReleaseFetcher(
    private val repoOwner: String,
    private val repo: String,
) {
    // Global Ktor configuration
    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches the latest release from the GitHub API using Ktor.
     */
    suspend fun getLatestRelease(): Release? = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.get("https://api.github.com/repos/$repoOwner/$repo/releases/latest") {
                timeout {
                    requestTimeoutMillis = 5_000
                    socketTimeoutMillis = 5_000
                }
            }

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
            OperatingSystem.ANDROID to ".apk",
            OperatingSystem.WINDOWS to ".msi",
            OperatingSystem.LINUX to ".deb",
            OperatingSystem.MAC to ".dmg"
        )

        val fileType = platformFileTypes[getOperatingSystem()] ?: return null

        // Find the corresponding asset
        val asset = release.assets.firstOrNull { it.name.endsWith(fileType, ignoreCase = true) }
        return asset?.browser_download_url
    }

    /**
     * Downloads the application and reports progress via [onProgress].
     *  - [percentage] = -1.0 if the total size is unknown
     *  - Otherwise, it’s the calculated percentage
     */
    suspend fun downloadApp(
        downloadUrl: String,
        onProgress: (percentage: Double, totalBytes: Long) -> Unit
    ): Boolean {
        val fileName = downloadUrl.substringAfterLast('/')
            .substringBefore('?') // In case the URL contains query parameters

        val cacheDir = getCacheDir()
        val destinationFile = File(cacheDir, fileName)

        return try {
            val response: HttpResponse = client.get(downloadUrl)

            if (response.status.isSuccess()) {
                val contentLength = response.contentLength() ?: -1L
                val channel: ByteReadChannel = response.body()

                withContext(Dispatchers.IO) {
                    destinationFile.outputStream().use { output ->
                        var bytesReceived: Long = 0
                        val buffer = ByteArray(8192)

                        while (!channel.isClosedForRead) {
                            val bytesRead = channel.readAvailable(buffer)
                            if (bytesRead == -1) break

                            // Write to the file
                            output.write(buffer, 0, bytesRead)
                            bytesReceived += bytesRead

                            if (contentLength > 0) {
                                val percentage = (bytesReceived * 100.0) / contentLength
                                onProgress(percentage, contentLength)
                            } else {
                                // Total size unknown
                                onProgress(-1.0, bytesReceived)
                            }
                        }
                    }
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
