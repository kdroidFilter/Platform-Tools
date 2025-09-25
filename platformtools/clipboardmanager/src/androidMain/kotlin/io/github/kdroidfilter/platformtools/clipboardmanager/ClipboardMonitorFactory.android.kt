package io.github.kdroidfilter.platformtools.clipboardmanager

import android.content.Context

/**
 * Android actual for ClipboardMonitorFactory.
 * You MUST call init(context) once (e.g., in Application.onCreate) before create(...).
 */
actual object ClipboardMonitorFactory {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual fun create(listener: ClipboardListener): ClipboardMonitor {
        check(::appContext.isInitialized) {
            "ClipboardMonitorFactory.init(context) must be called before create(listener)."
        }
        return AndroidClipboardMonitor(appContext, listener)
    }
}
