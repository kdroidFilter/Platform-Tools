package io.github.kdroidfilter.platformtools.clipboardmanager.windows

import io.github.kdroidfilter.platformtools.clipboardmanager.*
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.WinUser.*
import com.sun.jna.platform.win32.WinDef.*

class WindowsClipboardMonitor(private val listener: ClipboardListener) : ClipboardMonitor {
    private val WM_CLIPBOARDUPDATE = 0x031D
    private val WM_QUIT = 0x0012
    private val WM_DESTROY = 0x0002

    private var hwnd: HWND? = null
    private var thread: Thread? = null
    private val running = AtomicBoolean(false)
    private val started = CountDownLatch(1)

    override fun start() {
        if (running.get()) return
        running.set(true)

        thread = Thread({
            try {
                createMessageWindow()
                started.countDown()
                loop()
            } finally {
                cleanup()
            }
        }, "Windows-ClipboardMonitor").apply {
            isDaemon = false
            start()
        }

        started.await()
    }

    override fun stop() {
        if (!running.get()) return
        running.set(false)
        hwnd?.let { User32.INSTANCE.PostMessage(it, WM_QUIT, WPARAM(0), LPARAM(0)) }
        thread?.join(5000)
    }

    override fun isRunning() = running.get()

    override fun getCurrentContent(): ClipboardContent = readClipboard()

    private fun createMessageWindow() {
        val hInstance = Kernel32.INSTANCE.GetModuleHandle(null)
        val className = "ClipboardMonitorWindow"

        val wndClass = WNDCLASSEX().apply {
            cbSize = size()
            lpfnWndProc = WindowProc { h, msg, w, l -> handleMessage(h, msg, w, l) }
            this.hInstance = hInstance
            lpszClassName = className
        }

        if (User32.INSTANCE.RegisterClassEx(wndClass).toInt() == 0) {
            error("RegisterClassEx failed")
        }

        hwnd = User32.INSTANCE.CreateWindowEx(
            0, className, "Clipboard Monitor", 0,
            0, 0, 0, 0,
            HWND_MESSAGE, null, hInstance, null
        ) ?: error("CreateWindowEx failed")

        hwnd?.let {
            if (!User32Extended.INSTANCE.AddClipboardFormatListener(it)) {
                error("AddClipboardFormatListener failed")
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
                r == 0 -> break
                else -> break
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
