package io.github.kdroidfilter.platformtools.darkmodedetector.linux

import androidx.compose.runtime.Composable
import io.github.kdroidfilter.platformtools.LinuxDesktopEnvironment
import io.github.kdroidfilter.platformtools.detectLinuxDesktopEnvironment


@Composable
fun isLinuxInDarkMode(): Boolean {
    return when (detectLinuxDesktopEnvironment()) {
        LinuxDesktopEnvironment.KDE -> isKdeInDarkMode()
        LinuxDesktopEnvironment.GNOME -> isGnomeInDarkMode()
        LinuxDesktopEnvironment.XFCE -> detectXfceDarkTheme() ?: false
        LinuxDesktopEnvironment.CINNAMON -> detectCinnamonDarkTheme() ?: false
        LinuxDesktopEnvironment.MATE -> detectMateDarkTheme() ?: false
        else -> false
    }
}
