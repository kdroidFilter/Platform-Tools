package io.github.kdroidfilter.platformtools.darkmodedetector

import androidx.compose.runtime.Composable
import io.github.kdroidfilter.platformtools.OperatingSystem
import io.github.kdroidfilter.platformtools.getOperatingSystem


/**
 * Composable function that returns whether the system is in dark mode.
 * It handles macOS, Windows, and Linux. For Windows and Linux, it returns false as a placeholder.
 */
@Composable
actual fun isSystemInDarkMode(): Boolean {
    return when (getOperatingSystem()) {
        OperatingSystem.MACOS -> isMacOsInDarkMode()
        OperatingSystem.WINDOWS -> {
            logger.debug { "Using Windows dark mode detection logic (placeholder)" }
            false
        }
        OperatingSystem.LINUX -> isLinuxOsInDarkMode()
        else -> false
    }
}