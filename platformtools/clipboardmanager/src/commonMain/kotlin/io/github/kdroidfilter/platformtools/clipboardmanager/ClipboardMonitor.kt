package io.github.kdroidfilter.platformtools.clipboardmanager

interface ClipboardMonitor {
    fun start()
    fun stop()
    fun isRunning(): Boolean
    fun getCurrentContent(): ClipboardContent
}
