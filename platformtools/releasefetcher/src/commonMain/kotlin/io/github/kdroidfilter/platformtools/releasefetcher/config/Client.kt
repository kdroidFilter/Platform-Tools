package io.github.kdroidfilter.platformtools.releasefetcher.config

import io.github.kdroidfilter.platformtools.releasefetcher.downloader.ReleaseFetcherConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*

// Global Ktor client configuration
internal val client = HttpClient(CIO) {
    followRedirects = true

    install(HttpTimeout) {
        requestTimeoutMillis = ReleaseFetcherConfig.clientTimeOut
    }
}