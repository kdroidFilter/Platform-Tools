package io.github.kdroidfilter.platformtools

actual fun getOperatingSystem(): OperatingSystem {
    val userAgent = js("navigator.userAgent") as String
    val platform = js("navigator.platform") as String

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