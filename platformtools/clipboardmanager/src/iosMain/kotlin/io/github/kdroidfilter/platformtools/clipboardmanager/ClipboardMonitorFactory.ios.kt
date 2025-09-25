package io.github.kdroidfilter.platformtools.clipboardmanager

actual object ClipboardMonitorFactory {
    actual fun create(listener: ClipboardListener): ClipboardMonitor {
        // TODO: Implement iOS ClipboardMonitor using UIPasteboard and periodic polling or notifications
        throw NotImplementedError("iOS ClipboardMonitor is not implemented yet. TODO: Implement using UIPasteboard.generalPasteboard")
    }
}
