// src/main/kotlin/io/github/kdroidfilter/platformtools/clipboardmanager/linux/X11ClipboardMonitor.kt
package io.github.kdroidfilter.platformtools.clipboardmanager.linux.x11

import io.github.kdroidfilter.platformtools.clipboardmanager.*
import com.sun.jna.NativeLong
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import io.github.kdroidfilter.platformtools.clipboardmanager.linux.LinuxClipboardMonitor
import com.sun.jna.platform.unix.X11

class X11ClipboardMonitor(listener: ClipboardListener) : LinuxClipboardMonitor(listener) {
    private var display: Pointer? = null
    private var window: NativeLong = NativeLong(0)
    private var thread: Thread? = null
    private val XFixesSelectionNotify = 35

    private var clipboardAtom: NativeLong = NativeLong(0)
    private var primaryAtom: NativeLong = NativeLong(0)

    override fun start() {
        if (running.get()) return
        running.set(true)

        thread = Thread({
            try {
                setup()
                if (display != null) loop() else fallback()
            } finally {
                cleanup()
            }
        }, "X11-ClipboardMonitor").apply {
            isDaemon = false
            start()
        }
    }

    override fun stop() {
        if (!running.get()) return
        running.set(false)
        thread?.interrupt()
        thread?.join(5000)
    }

    override fun isRunning() = running.get()

    override fun getCurrentContent(): ClipboardContent {
        readClipboardViaCommand(arrayOf("xclip", "-selection", "clipboard", "-o"))?.let {
            return ClipboardContent(text = it, timestamp = System.currentTimeMillis())
        }
        readClipboardViaCommand(arrayOf("xsel", "--clipboard", "--output"))?.let {
            return ClipboardContent(text = it, timestamp = System.currentTimeMillis())
        }
        return readAWTClipboard()
    }

    private fun setup() {
        if (X11Ext.INSTANCE == null || XFixes.INSTANCE == null) return
        display = X11Ext.INSTANCE.XOpenDisplay(null) ?: return

        val major = IntByReference(5)
        val minor = IntByReference(0)
        if (XFixes.INSTANCE.XFixesQueryVersion(display!!, major, minor) == 0) return

        val screen = X11Ext.INSTANCE.XDefaultScreen(display!!)
        val root  = X11Ext.INSTANCE.XRootWindow(display!!, screen)
        window = X11Ext.INSTANCE.XCreateSimpleWindow(display!!, root, 0, 0, 1, 1, 0, NativeLong(0), NativeLong(0))

        clipboardAtom = X11Ext.INSTANCE.XInternAtom(display!!, "CLIPBOARD", false)
        primaryAtom   = X11Ext.INSTANCE.XInternAtom(display!!, "PRIMARY", false)

        val mask = 1 // XFixesSetSelectionOwnerNotifyMask
        XFixes.INSTANCE.XFixesSelectSelectionInput(display!!, window, clipboardAtom, mask)
        XFixes.INSTANCE.XFixesSelectSelectionInput(display!!, window, primaryAtom, mask)
    }

    private fun loop() {
        val event = X11.XEvent()
        var lastText: String? = null

        while (running.get() && display != null) {
            if (X11Ext.INSTANCE!!.XPending(display!!) > 0) {
                X11Ext.INSTANCE!!.XNextEvent(display!!, event)
                if (event.type == XFixesSelectionNotify) {
                    val content = getCurrentContent()
                    if (content.text != lastText) {
                        lastText = content.text
                        listener.onClipboardChange(content)
                    }
                }
            } else {
                Thread.sleep(10)
            }
        }
    }

    private fun fallback() {
        var last: String? = null
        while (running.get()) {
            val c = getCurrentContent()
            if (c.text != last) {
                last = c.text
                listener.onClipboardChange(c)
            }
            Thread.sleep(200)
        }
    }

    private fun cleanup() {
        display?.let { runCatching { X11Ext.INSTANCE?.XCloseDisplay(it) } }
        display = null
        window = NativeLong(0)
    }
}
