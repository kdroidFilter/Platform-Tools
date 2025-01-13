package io.github.kdroidfilter.platformtools.releasefetcher.downloader

import io.ktor.client.plugins.HttpTimeoutConfig

/**
 * Configuration object for managing buffer settings used during operations like file downloads.
 *
 * This object contains parameters that control the behavior of the buffer, such as its size. The buffer size
 * is critical for managing resource usage and optimizing performance during data transfers.
 */
object ReleaseFetcherConfig {
    var downloaderBufferSize: Int = 2 * 1024 * 1024
    var clientTimeOut: Long = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
}