package io.github.kdroidfilter.platformtools.clipboardmanager.linux

import io.github.kdroidfilter.platformtools.clipboardmanager.*
import io.github.kdroidfilter.platformtools.clipboardmanager.linux.wayland.WaylandClipboardMonitor
import io.github.kdroidfilter.platformtools.clipboardmanager.linux.x11.X11ClipboardMonitor
import io.github.kdroidfilter.platformtools.clipboardmanager.linux.x11.X11Ext
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

abstract class LinuxClipboardMonitor(protected val listener: ClipboardListener) : ClipboardMonitor {

    companion object {
        fun create(listener: ClipboardListener): LinuxClipboardMonitor {
            return when (detectWindowSystem()) {
                LinuxWindowSystem.X11    -> X11ClipboardMonitor(listener)
                LinuxWindowSystem.WAYLAND -> WaylandClipboardMonitor(listener)
                LinuxWindowSystem.UNKNOWN -> FallbackLinuxClipboardMonitor(listener)
            }
        }

        private fun detectWindowSystem(): LinuxWindowSystem {
            if (System.getenv("WAYLAND_DISPLAY") != null) return LinuxWindowSystem.WAYLAND
            when (System.getenv("XDG_SESSION_TYPE")) {
                "wayland" -> return LinuxWindowSystem.WAYLAND
                "x11"     -> return LinuxWindowSystem.X11
            }
            if (System.getenv("DISPLAY") != null) {
                return try {
                    val d = X11Ext.INSTANCE?.XOpenDisplay(null)
                    if (d != null) {
                        X11Ext.INSTANCE?.XCloseDisplay(d)
                        LinuxWindowSystem.X11
                    } else LinuxWindowSystem.UNKNOWN
                } catch (_: Throwable) {
                    LinuxWindowSystem.UNKNOWN
                }
            }
            return LinuxWindowSystem.UNKNOWN
        }
    }

    protected val running = AtomicBoolean(false)

    protected fun readClipboardViaCommand(cmd: Array<String>): String? {
        return runCatching {
            val p = ProcessBuilder(*cmd).redirectErrorStream(true).start()
            val ok = p.waitFor(1, TimeUnit.SECONDS) && p.exitValue() == 0
            if (ok) p.inputStream.bufferedReader().use { it.readText() }.ifEmpty { null } else null
        }.getOrNull()
    }

    protected fun readAWTClipboard(): ClipboardContent {
        return runCatching {
            val c = Toolkit.getDefaultToolkit().systemClipboard.getContents(null) ?: return ClipboardContent(timestamp = System.currentTimeMillis())
            var text: String? = null
            var files: List<String>? = null
            val imageAvailable = c.isDataFlavorSupported(DataFlavor.imageFlavor)

            if (c.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                text = c.getTransferData(DataFlavor.stringFlavor) as? String
            }
            if (c.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                @Suppress("UNCHECKED_CAST")
                val list = c.getTransferData(DataFlavor.javaFileListFlavor) as List<java.io.File>
                files = list.map { it.absolutePath }
            }
            ClipboardContent(text = text, files = files, imageAvailable = imageAvailable, timestamp = System.currentTimeMillis())
        }.getOrDefault(ClipboardContent(timestamp = System.currentTimeMillis()))
    }
}
