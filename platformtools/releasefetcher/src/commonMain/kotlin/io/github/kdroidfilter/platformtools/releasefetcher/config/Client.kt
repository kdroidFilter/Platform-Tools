package io.github.kdroidfilter.platformtools.releasefetcher.config

import io.ktor.client.*
import io.ktor.client.engine.cio.*

// Global Ktor configuration
val client = HttpClient(CIO)