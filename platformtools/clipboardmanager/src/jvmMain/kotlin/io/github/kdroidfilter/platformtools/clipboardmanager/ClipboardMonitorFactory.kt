package io.github.kdroidfilter.platformtools.clipboardmanager

import com.sun.jna.Platform
import io.github.kdroidfilter.platformtools.clipboardmanager.windows.WindowsClipboardMonitor

actual object ClipboardMonitorFactory {
    actual fun create(listener: ClipboardListener): ClipboardMonitor {
        return when {
            Platform.isWindows() -> WindowsClipboardMonitor(listener)
            Platform.isMac() || Platform.isLinux() -> AwtOSClipboardMonitor(listener)
            else -> error("Unsupported OS type: ${Platform.getOSType()}")
        }
    }
}
