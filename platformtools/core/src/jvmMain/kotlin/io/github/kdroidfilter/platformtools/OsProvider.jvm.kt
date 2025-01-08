package io.github.kdroidfilter.platformtools

actual fun getOperatingSystem(): OperatingSystem {
    val osName = System.getProperty("os.name").lowercase()
    return when {
        osName.contains("win") -> OperatingSystem.WINDOWS
        osName.contains("mac") -> OperatingSystem.MAC
        osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> OperatingSystem.LINUX
        else -> OperatingSystem.UNKNOWN
    }
}