package io.github.kdroidfilter.platformtools

actual fun getPlatform(): Platform {
    val osName = System.getProperty("os.name").lowercase()
    return when {
        osName.contains("win") -> Platform.WINDOWS
        osName.contains("mac") -> Platform.MAC
        osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> Platform.LINUX
        else -> Platform.UNKNOWN
    }
}