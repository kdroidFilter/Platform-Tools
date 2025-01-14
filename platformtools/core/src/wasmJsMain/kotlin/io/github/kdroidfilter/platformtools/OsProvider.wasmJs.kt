package io.github.kdroidfilter.platformtools

fun getUserAgent(): String =
    js("window.navigator.userAgent")

fun getOs(): String =
    js("window.navigator.platform")

actual fun getOperatingSystem(): OperatingSystem {
    val userAgent = getUserAgent()
    val platform = getOs()

    return when {
        userAgent.contains("Windows", ignoreCase = true) -> OperatingSystem.WINDOWS
        userAgent.contains("Macintosh", ignoreCase = true) || platform.contains("Mac", ignoreCase = true) -> OperatingSystem.MACOS
        userAgent.contains("Linux", ignoreCase = true) -> OperatingSystem.LINUX
        userAgent.contains("Android", ignoreCase = true) -> OperatingSystem.ANDROID
        userAgent.contains("iPhone", ignoreCase = true) ||
                userAgent.contains("iPad", ignoreCase = true) ||
                userAgent.contains("iPod", ignoreCase = true) -> OperatingSystem.IOS

        else -> OperatingSystem.UNKNOWN
    }
}