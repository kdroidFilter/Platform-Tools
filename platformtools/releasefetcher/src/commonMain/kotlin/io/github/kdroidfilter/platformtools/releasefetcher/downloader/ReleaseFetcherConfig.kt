package io.github.kdroidfilter.platformtools.releasefetcher.downloader

import io.ktor.client.plugins.HttpTimeoutConfig

/**
 * Configuration object for managing settings used during operations.
 */
object ReleaseFetcherConfig {
    var clientTimeOut: Long = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
}
