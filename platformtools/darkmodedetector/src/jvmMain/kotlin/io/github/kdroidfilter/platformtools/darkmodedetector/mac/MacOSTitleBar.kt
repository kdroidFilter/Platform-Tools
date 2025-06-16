package io.github.kdroidfilter.platformtools.darkmodedetector.mac

import io.github.kdroidfilter.platformtools.OperatingSystem
import io.github.kdroidfilter.platformtools.getOperatingSystem

/**
 * Enum class representing the different appearance modes for macOS title bar.
 */
enum class MacOSTitleBarMode {
    /**
     * Uses the system setting for appearance
     */
    AUTO,

    /**
     * Forces dark mode using NSAppearanceNameDarkAqua
     */
    DARK,

    /**
     * Forces light mode using NSAppearanceNameAqua
     */
    LIGHT
}

/**
 * Sets the macOS adaptive title bar appearance.
 *
 * This function sets the system property that controls the appearance of the title bar
 * in macOS applications. It should be called before the application window is created.
 *
 * @param mode The appearance mode to use. Default is [MacOSTitleBarMode.AUTO].
 */
fun setMacOsAdaptiveTitleBar(mode: MacOSTitleBarMode = MacOSTitleBarMode.AUTO) {
    if (getOperatingSystem() != OperatingSystem.MACOS) {
        return
    }

    val appearanceValue = when (mode) {
        MacOSTitleBarMode.DARK -> "NSAppearanceNameDarkAqua"
        MacOSTitleBarMode.LIGHT -> "NSAppearanceNameAqua"
        MacOSTitleBarMode.AUTO -> "system"
    }

    System.setProperty("apple.awt.application.appearance", appearanceValue)
}

