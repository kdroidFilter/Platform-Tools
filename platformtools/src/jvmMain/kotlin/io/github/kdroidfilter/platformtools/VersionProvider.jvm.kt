package io.github.kdroidfilter.platformtools

actual fun getAppVersion(): String {
    return System.getProperty("jpackage.app-version")
}