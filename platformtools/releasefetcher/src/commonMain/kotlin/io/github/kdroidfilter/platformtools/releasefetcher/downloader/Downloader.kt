package io.github.kdroidfilter.platformtools.releasefetcher.downloader

import io.github.kdroidfilter.platformtools.getCacheDir
import io.github.kdroidfilter.platformtools.releasefetcher.config.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class Downloader {
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