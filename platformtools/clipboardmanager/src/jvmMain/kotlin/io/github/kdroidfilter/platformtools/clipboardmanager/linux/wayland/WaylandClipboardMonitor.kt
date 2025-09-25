package io.github.kdroidfilter.platformtools.clipboardmanager.linux.wayland

import io.github.kdroidfilter.platformtools.clipboardmanager.ClipboardContent
import io.github.kdroidfilter.platformtools.clipboardmanager.ClipboardListener
import io.github.kdroidfilter.platformtools.clipboardmanager.linux.LinuxClipboardMonitor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class WaylandClipboardMonitor(listener: ClipboardListener) : LinuxClipboardMonitor(listener) {
    private var scheduler: ScheduledExecutorService? = null
    private var last: String? = null

    override fun start() {
        if (running.get()) return
        running.set(true)

        scheduler = Executors.newSingleThreadScheduledExecutor {
            Thread(it, "Wayland-ClipboardMonitor").apply { isDaemon = true }
        }.also {
            it.scheduleWithFixedDelay(::check, 0, 150, TimeUnit.MILLISECONDS)
        }
    }

    override fun stop() {
        if (!running.get()) return
        running.set(false)
        scheduler?.shutdown()
        scheduler = null
    }

    override fun isRunning() = running.get()

    override fun getCurrentContent(): ClipboardContent {
        readClipboardViaCommand(arrayOf("wl-paste", "--no-newline"))?.let { text ->
            val mimeList = readClipboardViaCommand(arrayOf("wl-paste", "--list-types")).orEmpty()
            val hasImage = mimeList.lines().any { it.startsWith("image/") }
            return ClipboardContent(text = text, imageAvailable = hasImage, timestamp = System.currentTimeMillis())
        }
        return readAWTClipboard()
    }

    private fun check() {
        if (!running.get()) return
        val c = getCurrentContent()
        if (c.text != last) {
            last = c.text
            listener.onClipboardChange(c)
        }
    }
}