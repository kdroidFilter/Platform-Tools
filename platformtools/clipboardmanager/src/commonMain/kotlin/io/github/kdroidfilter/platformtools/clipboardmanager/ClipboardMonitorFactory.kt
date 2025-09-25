package io.github.kdroidfilter.platformtools.clipboardmanager

expect object ClipboardMonitorFactory {
    fun create(listener: ClipboardListener): ClipboardMonitor
}
