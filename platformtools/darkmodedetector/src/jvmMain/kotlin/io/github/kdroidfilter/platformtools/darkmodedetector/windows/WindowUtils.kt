package io.github.kdroidfilter.platformtools.darkmodedetector.windows

import com.sun.jna.Native
import com.sun.jna.Platform
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.ptr.IntByReference
import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Window


object WindowUtils {
    fun setDarkTitleBar(window: Window, dark: Boolean = true) {
        try {
            if (!Platform.isWindows()) return
            // Get HWND from the AWT Window
            val hwnd = WinDef.HWND(Native.getComponentPointer(window))

            // Create a pointer to hold the boolean value
            val darkModeEnabled = IntByReference(if (dark) 1 else 0)

            // Set the window attribute
            DwmApi.INSTANCE.DwmSetWindowAttribute(
                hwnd,
                DwmApi.DWMWA_USE_IMMERSIVE_DARK_MODE,
                darkModeEnabled.pointer,
                4 // size of Int
            )
        } catch (e: Exception) {
            windowsLogger.debug { "Failed to set dark mode: ${e.message}" }
        }
    }
}