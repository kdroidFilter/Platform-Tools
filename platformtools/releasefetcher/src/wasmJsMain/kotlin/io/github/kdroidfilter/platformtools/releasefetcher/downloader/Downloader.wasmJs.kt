package io.github.kdroidfilter.platformtools.releasefetcher.downloader

import io.github.kdroidfilter.platformtools.releasefetcher.config.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

actual class Downloader {
    actual suspend fun download(
        downloadUrl: String,
        onProgress: (percentage: Double, file: PlatformFile?) -> Unit
    ): Boolean {
        return try {
            onProgress(0.0, null)
            val response: HttpResponse = client.get(downloadUrl)
            if (response.status.isSuccess()) {
                val bytes: ByteArray = response.body()
                val name = downloadUrl.substringAfterLast('/')
                onProgress(100.0, PlatformFile(name, bytes))
                true
            } else {
                onProgress(-1.0, null)
                false
            }
        } catch (e: Exception) {
            onProgress(-1.0, null)
            false
        }
    }
}
