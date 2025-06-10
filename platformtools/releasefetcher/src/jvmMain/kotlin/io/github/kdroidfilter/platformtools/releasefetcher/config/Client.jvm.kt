package io.github.kdroidfilter.platformtools.releasefetcher.config

import io.github.kdroidfilter.platformtools.releasefetcher.downloader.ReleaseFetcherConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout

actual val client: HttpClient = HttpClient(CIO) {
    followRedirects = true

    install(HttpTimeout) {
        requestTimeoutMillis = ReleaseFetcherConfig.clientTimeOut
    }
}

