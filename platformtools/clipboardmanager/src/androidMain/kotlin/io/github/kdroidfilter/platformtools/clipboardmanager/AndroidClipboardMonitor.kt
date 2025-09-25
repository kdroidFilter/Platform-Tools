package io.github.kdroidfilter.platformtools.clipboardmanager

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android implementation of ClipboardMonitor using ClipboardManager callbacks.
 * - Debounced via main-thread Handler to coalesce rapid events.
 * - Builds a lightweight signature to avoid emitting duplicate consecutive events.
 * - Extracts: text, html (if provided), files (as content URIs), imageAvailable boolean.
 *
 * NOTE: On Android there is no standard RTF flavor; we leave rtf = null.
 * Files are exposed as content URIs (string). Resolving real paths is discouraged.
 */
internal class AndroidClipboardMonitor(
    private val context: Context,
    private val listener: ClipboardListener,
    private val debounceMillis: Long = 50L
) : ClipboardMonitor {

    private val running = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastSignature: String? = null

    private val cb by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private val callback = ClipboardManager.OnPrimaryClipChangedListener {
        // Debounce on main thread
        mainHandler.removeCallbacks(notifyRunnable)
        mainHandler.postDelayed(notifyRunnable, debounceMillis)
    }

    private val notifyRunnable = Runnable {
        if (!running.get()) return@Runnable
        val content = readClipboard()
        val sig = signatureOf(content)
        if (sig != lastSignature) {
            lastSignature = sig
            try {
                listener.onClipboardChange(content)
            } catch (_: Throwable) {
                // Listener exceptions must not stop monitoring
            }
        }
    }

    override fun start() {
        if (running.getAndSet(true)) return
        // Register listener and fire initial snapshot
        mainHandler.post {
            runCatching { cb.addPrimaryClipChangedListener(callback) }
            notifyRunnable.run()
        }
    }

    override fun stop() {
        if (!running.getAndSet(false)) return
        mainHandler.post {
            runCatching { cb.removePrimaryClipChangedListener(callback) }
        }
        mainHandler.removeCallbacksAndMessages(null)
        lastSignature = null
    }

    override fun isRunning(): Boolean = running.get()

    override fun getCurrentContent(): ClipboardContent = readClipboard()

    // ==== Internals ====

    private fun readClipboard(): ClipboardContent {
        val cm = cb
        val clip: ClipData? = runCatching { cm.primaryClip }.getOrNull()
        val desc: ClipDescription? = runCatching { cm.primaryClipDescription }.getOrNull()

        if (clip == null || clip.itemCount == 0) {
            return ClipboardContent(timestamp = System.currentTimeMillis())
        }

        var text: String? = null
        var html: String? = null
        var files: MutableList<String>? = null
        var imageAvailable = false

        for (i in 0 until clip.itemCount) {
            val item = clip.getItemAt(i)

            // Text (coerceToText covers plain text and simple formats)
            val coerced = item.coerceToText(context)
            if (!coerced.isNullOrEmpty()) {
                text = (text ?: "") + (if (text == null) "" else "\n") + coerced.toString()
            }

            // HTML (explicit htmlText if provided)
            val h = item.htmlText
            if (!h.isNullOrEmpty()) {
                html = (html ?: "") + (if (html == null) "" else "\n") + h
            }

            // URIs: could be images or files (documents/photos/etc.)
            val uri: Uri? = item.uri
            if (uri != null) {
                val cr = context.contentResolver
                val type = runCatching { cr.getType(uri) }.getOrNull()
                if ((type ?: "").startsWith("image/")) {
                    imageAvailable = true
                }
                // Treat any content URI as a "file-like" entry
                if (files == null) files = mutableListOf()
                files!!.add(uri.toString())
            }

            // Intents are ignored for now (non-file, non-text clipboard content)
        }

        // Also mark images via mime description if available (fallback)
        if (!imageAvailable && desc != null) {
            val mimes = (0 until desc.mimeTypeCount).map { desc.getMimeType(it) }
            imageAvailable = mimes.any { it.startsWith(ClipDescription.MIMETYPE_TEXT_URILIST) || it.startsWith("image/") }
        }

        return ClipboardContent(
            text = text,
            html = html,
            rtf = null, // Android has no canonical RTF clipboard flavor
            files = files,
            imageAvailable = imageAvailable,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun signatureOf(c: ClipboardContent): String {
        val sb = StringBuilder(128)
        fun add(name: String, v: String?) {
            if (v != null) {
                sb.append(name).append('#').append(v.length).append(';')
                sb.append(v.take(64)).append('|')
            } else sb.append(name).append('#').append("0;|")
        }
        add("t", c.text)
        add("h", c.html)
        add("r", c.rtf)
        sb.append("i#").append(if (c.imageAvailable) 1 else 0).append('|')
        val f = c.files
        if (f != null) {
            sb.append("f#").append(f.size).append('|')
            f.take(8).forEach { sb.append(it).append('|') }
        } else sb.append("f#0|")
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
