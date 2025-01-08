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

private const val BUFFER_SIZE = 2621440 // 256KB buffer

class Downloader {

    suspend fun downloadApp(
        downloadUrl: String,
        onProgress: (percentage: Double, file: File?) -> Unit
    ): Boolean {
        logger.debug { "Starting download from URL: $downloadUrl" }

        val fileName = downloadUrl.substringAfterLast('/').substringBefore('?')
        val cacheDir = getCacheDir()
        val destinationFile = File(cacheDir, fileName)

        // Notify 0% immediately at the start
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
