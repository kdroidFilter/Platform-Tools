package io.github.kdroidfilter.platformtools.clipboardmanager.linux

import io.github.kdroidfilter.platformtools.clipboardmanager.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class FallbackLinuxClipboardMonitor(listener: ClipboardListener) : LinuxClipboardMonitor(listener) {
    private var scheduler: ScheduledExecutorService? = null
    private var last: String? = null

    override fun start() {
        if (running.get()) return
        running.set(true)

        scheduler = Executors.newSingleThreadScheduledExecutor {
            Thread(it, "Linux-Fallback-ClipboardMonitor").apply { isDaemon = true }
        }.also {
            it.scheduleWithFixedDelay(::tick, 0, 200, TimeUnit.MILLISECONDS)
        }
    }

    override fun stop() {
        if (!running.get()) return
        running.set(false)
        scheduler?.shutdown()
        scheduler = null
    }

    override fun isRunning() = running.get()

    override fun getCurrentContent(): ClipboardContent = readAWTClipboard()

    private fun tick() {
        val c = getCurrentContent()
        if (c.text != last) {
            last = c.text
            listener.onClipboardChange(c)
        }
    }
}
