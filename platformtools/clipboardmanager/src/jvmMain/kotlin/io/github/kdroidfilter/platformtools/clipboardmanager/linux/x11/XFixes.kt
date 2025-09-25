// src/main/kotlin/io/github/kdroidfilter/platformtools/clipboardmanager/linux/XFixes.kt
package io.github.kdroidfilter.platformtools.clipboardmanager.linux.x11

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.NativeLong

interface XFixes : Library {
    fun XFixesQueryVersion(display: Pointer, major: IntByReference, minor: IntByReference): Int
    fun XFixesSelectSelectionInput(display: Pointer, window: NativeLong, selection: NativeLong, mask: Int)

    companion object {
        val INSTANCE: XFixes? = runCatching {
            Native.load("Xfixes", XFixes::class.java) as XFixes
        }.getOrNull()
    }
}
