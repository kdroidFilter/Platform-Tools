// src/main/kotlin/io/github/kdroidfilter/platformtools/clipboardmanager/linux/X11Ext.kt
package io.github.kdroidfilter.platformtools.clipboardmanager.linux.x11

import com.sun.jna.*
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import com.sun.jna.platform.unix.X11
import com.sun.jna.ptr.NativeLongByReference

interface X11Ext : Library {
    fun XOpenDisplay(name: String?): Pointer?
    fun XCloseDisplay(display: Pointer): Int
    fun XDefaultScreen(display: Pointer): Int
    fun XRootWindow(display: Pointer, screen: Int): NativeLong
    fun XCreateSimpleWindow(display: Pointer, parent: NativeLong, x: Int, y: Int,
                            width: Int, height: Int, borderWidth: Int,
                            border: NativeLong, background: NativeLong): NativeLong
    fun XSelectInput(display: Pointer, window: NativeLong, mask: NativeLong)
    fun XNextEvent(display: Pointer, event: X11.XEvent): Int
    fun XPending(display: Pointer): Int
    fun XFlush(display: Pointer): Int
    fun XInternAtom(display: Pointer, name: String, onlyIfExists: Boolean): NativeLong
    fun XConvertSelection(display: Pointer, selection: NativeLong, target: NativeLong,
                          property: NativeLong, window: NativeLong, time: NativeLong)
    fun XGetWindowProperty(display: Pointer, window: NativeLong, property: NativeLong,
                           offset: NativeLong, length: NativeLong, delete: Boolean,
                           reqType: NativeLong, actualType: PointerByReference,
                           actualFormat: IntByReference, nItems: NativeLongByReference,
                           bytesAfter: NativeLongByReference, prop: PointerByReference): Int

    companion object {
        val INSTANCE: X11Ext? = runCatching {
            Native.load("X11", X11Ext::class.java) as X11Ext
        }.getOrNull()
    }
}
