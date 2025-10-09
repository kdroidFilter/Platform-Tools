package io.github.kdroidfilter.platformtools.releasefetcher.github

import io.github.kdroidfilter.platformtools.releasefetcher.config.client
import io.github.kdroidfilter.platformtools.releasefetcher.github.model.Release
import io.ktor.client.call.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class GitHubReleaseFetcher(
    private val owner: String,
    private val repo: String,
    private val httpClient: HttpClient = client,
) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches the latest release from the GitHub API using Ktor.
     */
    suspend fun getLatestRelease(): Release? {
        return try {
            val response: HttpResponse =
                httpClient.get("https://api.github.com/repos/$owner/$repo/releases/latest")

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


}
