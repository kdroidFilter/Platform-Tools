package io.github.kdroidfilter.platformtools.releasefetcher.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

actual val client = HttpClient(Js)