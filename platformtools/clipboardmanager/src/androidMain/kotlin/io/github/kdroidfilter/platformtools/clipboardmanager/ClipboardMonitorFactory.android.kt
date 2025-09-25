package io.github.kdroidfilter.platformtools.clipboardmanager

actual object ClipboardMonitorFactory {
    actual fun create(listener: ClipboardListener): ClipboardMonitor {
        // TODO: Implement Android ClipboardMonitor using ClipboardManager and a listener for primary clip changes
        throw NotImplementedError("Android ClipboardMonitor is not implemented yet. TODO: Implement using android.content.ClipboardManager")
    }
}
