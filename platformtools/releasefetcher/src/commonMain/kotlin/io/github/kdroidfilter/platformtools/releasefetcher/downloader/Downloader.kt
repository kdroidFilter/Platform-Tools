package io.github.kdroidfilter.platformtools.releasefetcher.downloader

import io.github.kdroidfilter.platformtools.getCacheDir
import io.github.kdroidfilter.platformtools.releasefetcher.config.client
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private val logger = KotlinLogging.logger {}

private const val BUFFER_SIZE = 2621440

/**
 * Downloader is responsible for handling file downloads from a given URL.
 * This class provides functionality to asynchronously download files, report
 * download progress, and handle errors that may occur during the download process.
 */
class Downloader {

    /**
     * Downloads an application file from the provided URL and tracks the download progress.
     *
     * @param downloadUrl The URL from which the application will be downloaded.
     * @param onProgress A callback function reporting download progress percentage and the downloaded file (if available).
     *                     The percentage is a `Double` ranging from 0.0 to 100.0, or -1.0 in case of errors.
     *                     The `file` parameter is the local file being downloaded, or `null` during download progress updates.
     * @return A `Boolean` indicating whether the download was successful.
     *         Returns `true` if the download completes successfully, otherwise `false`.
     */
    suspend fun downloadApp(
        downloadUrl: String,
        onProgress: (percentage: Double, file: File?) -> Unit
    ): Boolean {
        logger.debug { "Starting download from URL: $downloadUrl" }

        val fileName = downloadUrl.substringAfterLast('/').substringBefore('?')
        val cacheDir = getCacheDir()
        val destinationFile = File(cacheDir, fileName)

        onProgress(0.0, null)
        logger.debug { "Download initialized: 0%" }

        return try {
            val response = client.get(downloadUrl) {
                onDownload { bytesSentTotal, contentLength ->
                    val progress = if (contentLength != null && contentLength > 0) {
                        (bytesSentTotal * 100.0 / contentLength)
                    } else 0.0
                    logger.debug { "Progress: $bytesSentTotal / $contentLength bytes" }
                    onProgress(progress, null)
                }
            }

            if (response.status.isSuccess()) {
                val channel: ByteReadChannel = response.body()
                val contentLength = response.contentLength() ?: -1L
                logger.debug { "Content length: $contentLength bytes" }

                withContext(Dispatchers.IO) {
                    destinationFile.outputStream().buffered(BUFFER_SIZE).use { output ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        while (!channel.isClosedForRead) {
                            val bytesRead = channel.readAvailable(buffer)
                            if (bytesRead == -1) break
                            output.write(buffer, 0, bytesRead)
                        }
                    }
                }

                if (destinationFile.exists()) {
                    logger.debug { "Download completed. Size: ${destinationFile.length()} bytes" }
                    onProgress(100.0, destinationFile)
                    true
                } else {
                    logger.error { "Error: File not created" }
                    onProgress(-1.0, null)  // Keep -1.0 for errors
                    false
                }
            } else {
                logger.error { "Download failed: ${response.status}" }
                onProgress(-1.0, null)  // Keep -1.0 for errors
                false
            }
        } catch (e: Exception) {
            logger.error(e) { "Download error: ${e.message}" }
            onProgress(-1.0, null)  // Keep -1.0 for errors
            false
        }
    }
}
