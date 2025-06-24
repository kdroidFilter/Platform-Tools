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

    private val json = Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

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
     * Fetches a list of releases from the GitHub API using Ktor.
     * 
     * @param count The number of releases to fetch. Default is 10.
     * @return A list of Release objects or null if the request fails.
     */
    suspend fun getReleases(count: Int = 10): List<Release>? {
        return try {
            val response: HttpResponse = client.get("https://api.github.com/repos/$owner/$repo/releases") {
                parameter("per_page", count)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.body()
                json.decodeFromString<List<Release>>(responseBody)
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

}
