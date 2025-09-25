package io.github.kdroidfilter.platformtools.clipboardmanager

data class ClipboardContent(
    val text: String? = null,
    val html: String? = null,
    val rtf: String? = null,
    val files: List<String>? = null,
    val imageAvailable: Boolean = false,
    val timestamp: Long
)
