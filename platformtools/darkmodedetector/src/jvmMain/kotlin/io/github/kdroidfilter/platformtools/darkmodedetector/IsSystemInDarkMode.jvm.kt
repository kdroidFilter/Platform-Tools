package io.github.kdroidfilter.platformtools.darkmodedetector

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import io.github.kdroidfilter.platformtools.OperatingSystem
import io.github.kdroidfilter.platformtools.darkmodedetector.linux.isLinuxInDarkMode
import io.github.kdroidfilter.platformtools.darkmodedetector.mac.isMacOsInDarkMode
import io.github.kdroidfilter.platformtools.darkmodedetector.windows.isWindowsInDarkMode
import io.github.kdroidfilter.platformtools.getOperatingSystem

/**
 * Composable function that returns whether the system is in dark mode.
 * It handles macOS, Windows, and Linux.
 */
@Composable
actual fun isSystemInDarkMode(): Boolean {
    val isInPreview = LocalInspectionMode.current
    if (isInPreview) {
        return isSystemInDarkTheme()
    }

    return when (getOperatingSystem()) {
        OperatingSystem.MACOS -> isMacOsInDarkMode()
        OperatingSystem.WINDOWS -> isWindowsInDarkMode()
        OperatingSystem.LINUX -> isLinuxInDarkMode()
        else -> false
    }
}
