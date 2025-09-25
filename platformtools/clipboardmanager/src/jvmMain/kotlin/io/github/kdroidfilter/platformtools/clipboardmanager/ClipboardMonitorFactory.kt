package io.github.kdroidfilter.platformtools.clipboardmanager

import com.sun.jna.Platform
import io.github.kdroidfilter.platformtools.clipboardmanager.windows.WindowsClipboardMonitor

object ClipboardMonitorFactory {
    fun create(listener: ClipboardListener): ClipboardMonitor {
        return when {
            Platform.isWindows() -> WindowsClipboardMonitor(listener)
            Platform.isMac()     -> AwtOSClipboardMonitor(listener)
            Platform.isLinux()   -> AwtOSClipboardMonitor(listener)
            else -> error("Unsupported OS type: ${Platform.getOSType()}")
        }
    }
}
