package io.github.kdroidfilter.platformtools.releasefetcher.config

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*

// Global Ktor configuration
val client = HttpClient(CIO) {
    install(HttpRedirect) {
        checkHttpMethod = false
    }
    followRedirects = true

}