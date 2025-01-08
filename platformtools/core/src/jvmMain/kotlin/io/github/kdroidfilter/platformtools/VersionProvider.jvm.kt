package io.github.kdroidfilter.platformtools

actual fun getAppVersion(): String {
    return System.getProperty("jpackage.app-version") ?: "0.1.0"
}