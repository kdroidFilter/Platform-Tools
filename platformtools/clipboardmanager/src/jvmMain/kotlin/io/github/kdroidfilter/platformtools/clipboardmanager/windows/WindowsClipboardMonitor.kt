package io.github.kdroidfilter.platformtools.clipboardmanager.windows

import com.sun.jna.Pointer
import io.github.kdroidfilter.platformtools.clipboardmanager.*
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.WinUser.*
import com.sun.jna.platform.win32.WinDef.*
import com.sun.jna.ptr.PointerByReference

class WindowsClipboardMonitor(private val listener: ClipboardListener) : ClipboardMonitor {
    private val WM_CLIPBOARDUPDATE = 0x031D
    private val WM_QUIT = 0x0012
    private val WM_DESTROY = 0x0002

    // Keep a strong reference to the WindowProc to prevent GC.
    private val wndProc: WindowProc = WindowProc { h, msg, w, l -> handleMessage(h, msg, w, l) }

    private var hwnd: HWND? = null
    private var classRegistered = false
    private var thread: Thread? = null
    private val running = AtomicBoolean(false)
    private val started = CountDownLatch(1)
    private val startError = AtomicReference<Throwable?>(null)

    override fun start() {
        if (running.get()) return
        running.set(true)

        thread = Thread({
            try {
                createMessageWindow()
            } catch (e: Throwable) {
                startError.set(e)
            } finally {
                // Unblock the starter regardless of success/failure
                started.countDown()
            }

            try {
                if (startError.get() == null) {
                    loop()
                }
            } finally {
                cleanup()
            }
        }, "Windows-ClipboardMonitor").apply {
            isDaemon = false
            start()
        }

        // Wait until window creation succeeded or failed
        started.await()

        // Surface any startup failure to caller
        startError.get()?.let { err ->
            running.set(false)
            throw err
        }
    }

    override fun stop() {
        if (!running.get()) return
        running.set(false)
        hwnd?.let { User32.INSTANCE.PostMessage(it, WM_QUIT, WPARAM(0), LPARAM(0)) }
        thread?.join(5000)
    }

    override fun isRunning() = running.get()

    override fun getCurrentContent(): ClipboardContent = readClipboard()

    private fun lastErrorMsg(prefix: String): String {
        val code = Kernel32.INSTANCE.GetLastError()
        if (code == 0) return "$prefix (GetLastError=0)"

        val flags = WinBase.FORMAT_MESSAGE_FROM_SYSTEM or
                WinBase.FORMAT_MESSAGE_IGNORE_INSERTS or
                WinBase.FORMAT_MESSAGE_ALLOCATE_BUFFER

        val out = PointerByReference()
        val len = Kernel32.INSTANCE.FormatMessage(
            flags,
            null,
            code,
            0,
            out,
            0,
            null
        )
        if (len == 0) {
            return "$prefix (error $code: <failed to format message>)"
        }

        val ptr: Pointer = out.value
        val msg = try {
            ptr.getWideString(0).trim()
        } finally {
            Kernel32.INSTANCE.LocalFree(ptr)
        }
        return "$prefix (error $code: $msg)"
    }

    private fun createMessageWindow() {
        val hInstance = Kernel32.INSTANCE.GetModuleHandle(null)
        val className = "ClipboardMonitorWindow_${Integer.toHexString(System.identityHashCode(this))}"

        // Register once per instance
        if (!classRegistered) {
            val wndClass = WNDCLASSEX().apply {
                cbSize = size()
                lpfnWndProc = wndProc
                this.hInstance = hInstance
                lpszClassName = className
            }
            if (User32.INSTANCE.RegisterClassEx(wndClass).toInt() == 0) {
                throw IllegalStateException(lastErrorMsg("RegisterClassEx failed"))
            }
            classRegistered = true
        }

        // -------- Attempt #1: message-only parent (HWND_MESSAGE) --------
        hwnd = User32.INSTANCE.CreateWindowEx(
            0, className, "Clipboard Monitor", 0,
            0, 0, 0, 0,
            HWND_MESSAGE, null, hInstance, null
        )

        if (hwnd == null || !User32.INSTANCE.IsWindow(hwnd)) {
            val firstErr = lastErrorMsg("CreateWindowEx (HWND_MESSAGE) failed")

            // -------- Attempt #2: hidden top-level WS_POPUP, no parent --------
            val WS_POPUP = 0x80000000.toInt()
            hwnd = User32.INSTANCE.CreateWindowEx(
                0, className, "Clipboard Monitor", WS_POPUP,
                0, 0, 1, 1,
                null, null, hInstance, null
            )

            if (hwnd == null || !User32.INSTANCE.IsWindow(hwnd)) {
                throw IllegalStateException("$firstErr; then fallback also failed: ${lastErrorMsg("CreateWindowEx (WS_POPUP) failed")}")
            }
            // Keep it hidden; no ShowWindow call
        }

        // Register for clipboard notifications
        hwnd!!.let {
            if (!User32Extended.INSTANCE.AddClipboardFormatListener(it)) {
                throw IllegalStateException(lastErrorMsg("AddClipboardFormatListener failed"))
            }
        }
    }

    private fun handleMessage(hwnd: HWND, uMsg: Int, wParam: WPARAM, lParam: LPARAM): LRESULT {
        return when (uMsg) {
            WM_CLIPBOARDUPDATE -> {
                try { listener.onClipboardChange(readClipboard()) } catch (_: Throwable) {}
                LRESULT(0)
            }
            WM_DESTROY -> {
                User32.INSTANCE.PostQuitMessage(0)
                LRESULT(0)
            }
            else -> User32.INSTANCE.DefWindowProc(hwnd, uMsg, wParam, lParam)
        }
    }

    private fun loop() {
        val msg = MSG()
        while (running.get()) {
            val r = User32.INSTANCE.GetMessage(msg, null, 0, 0)
            when {
                r > 0 -> {
                    User32.INSTANCE.TranslateMessage(msg)
                    User32.INSTANCE.DispatchMessage(msg)
                }
                r == 0 -> break // WM_QUIT
                else -> break   // error
            }
        }
    }

    private fun cleanup() {
        hwnd?.let {
            runCatching { User32Extended.INSTANCE.RemoveClipboardFormatListener(it) }
            runCatching { User32.INSTANCE.DestroyWindow(it) }
        }
        hwnd = null
        running.set(false)
        // Unregistering the class is optional; OS will clean up at process exit.
    }

    private fun readClipboard(): ClipboardContent {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val contents = clipboard.getContents(null) ?: return ClipboardContent(timestamp = System.currentTimeMillis())

        var text: String? = null
        var html: String? = null
        var rtf: String? = null
        var files: List<String>? = null
        val imageAvailable: Boolean = contents.isDataFlavorSupported(DataFlavor.imageFlavor)

        if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            text = contents.getTransferData(DataFlavor.stringFlavor) as? String
        }

        runCatching {
            val htmlFlavor = DataFlavor("text/html;class=java.lang.String")
            if (contents.isDataFlavorSupported(htmlFlavor)) {
                html = contents.getTransferData(htmlFlavor) as? String
            }
        }

        runCatching {
            val rtfFlavor = DataFlavor("text/rtf;class=java.lang.String")
            if (contents.isDataFlavorSupported(rtfFlavor)) {
                rtf = contents.getTransferData(rtfFlavor) as? String
            }
        }

        if (contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            @Suppress("UNCHECKED_CAST")
            val list = contents.getTransferData(DataFlavor.javaFileListFlavor) as List<java.io.File>
            files = list.map { it.absolutePath }
        }

        return ClipboardContent(text, html, rtf, files, imageAvailable, System.currentTimeMillis())
    }
}
