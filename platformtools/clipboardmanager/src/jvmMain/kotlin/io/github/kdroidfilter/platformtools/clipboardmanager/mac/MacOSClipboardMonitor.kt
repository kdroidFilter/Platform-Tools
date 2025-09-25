package io.github.kdroidfilter.platformtools.clipboardmanager.mac

import io.github.kdroidfilter.platformtools.clipboardmanager.* // ClipboardMonitor, ClipboardListener, ClipboardContent
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.security.MessageDigest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * macOS clipboard monitor using lightweight polling.
 * This mirrors WindowsClipboardMonitor's public API/behavior:
 *  - start()/stop()/isRunning()
 *  - listener.onClipboardChange(...) with ClipboardContent mirroring the Windows fields
 *  - getCurrentContent() returns a one-off snapshot
 *
 * Design notes:
 *  - macOS doesn't expose a stable, public event for pasteboard changes to the JVM.
 *  - We poll the system clipboard and coalesce duplicate payloads via a signature.
 *  - Interval defaults to 200 ms (feels "instant" without wasting CPU).
 */
class MacOSClipboardMonitor(
    private val listener: ClipboardListener,
    private val intervalMillis: Long = 200L
) : ClipboardMonitor {

    private val running = AtomicBoolean(false)
    private val started = CountDownLatch(1)
    private var scheduler: ScheduledExecutorService? = null
    private var lastSignature: String? = null

    override fun start() {
        if (running.get()) return
        running.set(true)

        scheduler = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "macOS-ClipboardMonitor").apply { isDaemon = false }
        }.also { exec ->
            // Fire a first read quickly, then repeat.
            exec.scheduleAtFixedRate(::tickSafe, 0L, intervalMillis.coerceAtLeast(50L), TimeUnit.MILLISECONDS)
        }

        started.countDown()
        started.await()
    }

    override fun stop() {
        if (!running.get()) return
        running.set(false)
        scheduler?.shutdownNow()
        scheduler = null
        lastSignature = null
    }

    override fun isRunning(): Boolean = running.get()

    override fun getCurrentContent(): ClipboardContent = readClipboard()

    // === Internals ===

    private fun tickSafe() {
        if (!running.get()) return
        try {
            val content = readClipboard()
            val sig = signatureOf(content)
            if (sig != lastSignature) {
                lastSignature = sig
                try {
                    listener.onClipboardChange(content)
                } catch (_: Throwable) {
                    // Listener exceptions must not break the monitor.
                }
            }
        } catch (_: Throwable) {
            // Swallow all to keep the loop resilient.
        }
    }

    private fun readClipboard(): ClipboardContent {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val contents: Transferable = clipboard.getContents(null)
            ?: return ClipboardContent(timestamp = System.currentTimeMillis())

        var text: String? = null
        var html: String? = null
        var rtf: String? = null
        var files: List<String>? = null
        val imageAvailable: Boolean = contents.isDataFlavorSupported(DataFlavor.imageFlavor)

        // Plain text
        if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            runCatching {
                text = contents.getTransferData(DataFlavor.stringFlavor) as? String
            }
        }

        // HTML (as String)
        runCatching {
            val htmlFlavor = DataFlavor("text/html;class=java.lang.String")
            if (contents.isDataFlavorSupported(htmlFlavor)) {
                html = contents.getTransferData(htmlFlavor) as? String
            }
        }

        // RTF (as String) – many apps provide RTF in this flavor on macOS
        runCatching {
            val rtfFlavor = DataFlavor("text/rtf;class=java.lang.String")
            if (contents.isDataFlavorSupported(rtfFlavor)) {
                rtf = contents.getTransferData(rtfFlavor) as? String
            }
        }

        // File list
        if (contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            runCatching {
                @Suppress("UNCHECKED_CAST")
                val list = contents.getTransferData(DataFlavor.javaFileListFlavor) as List<java.io.File>
                files = list.map { it.absolutePath }
            }
        }

        return ClipboardContent(
            text = text,
            html = html,
            rtf = rtf,
            files = files,
            imageAvailable = imageAvailable,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Build a robust signature to avoid emitting duplicate consecutive events.
     * We hash the shapes/lengths instead of full payloads to stay cheap.
     */
    private fun signatureOf(c: ClipboardContent): String {
        val sb = StringBuilder(128)
        fun add(name: String, v: String?) {
            if (v != null) {
                sb.append(name).append('#').append(v.length).append(';')
                // Include a small prefix to disambiguate same-length strings
                sb.append(v.take(64)).append('|')
            } else sb.append(name).append('#').append("0;|")
        }
        add("t", c.text)
        add("h", c.html)
        add("r", c.rtf)
        sb.append("i#").append(if (c.imageAvailable) 1 else 0).append('|')
        if (c.files != null) {
            sb.append("f#").append(c.files!!.size).append('|')
            c.files!!.take(8).forEach { sb.append(it).append('|') }
        } else sb.append("f#0|")

        // Cheap stable digest
        return sha1(sb.toString())
    }

    private fun sha1(s: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        val bytes = md.digest(s.toByteArray(Charsets.UTF_8))
        val hex = CharArray(bytes.size * 2)
        val hexChars = "0123456789abcdef".toCharArray()
        var i = 0
        for (b in bytes) {
            val v = b.toInt() and 0xFF
            hex[i++] = hexChars[v ushr 4]
            hex[i++] = hexChars[v and 0x0F]
        }
        return String(hex)
    }
}
