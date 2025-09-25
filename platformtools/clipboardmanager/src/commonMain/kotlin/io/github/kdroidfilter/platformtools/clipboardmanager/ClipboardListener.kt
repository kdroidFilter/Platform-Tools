package io.github.kdroidfilter.platformtools.clipboardmanager

interface ClipboardListener {
    fun onClipboardChange(content: ClipboardContent)
}
