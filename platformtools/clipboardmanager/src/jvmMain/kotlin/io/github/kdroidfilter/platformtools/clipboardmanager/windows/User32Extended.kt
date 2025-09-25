package io.github.kdroidfilter.platformtools.clipboardmanager.windows

import com.sun.jna.Native
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.DWORD
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.win32.W32APIOptions

interface User32Extended : User32 {
    fun AddClipboardFormatListener(hWnd: WinDef.HWND): Boolean
    fun RemoveClipboardFormatListener(hWnd: WinDef.HWND): Boolean
    fun GetClipboardSequenceNumber(): DWORD

    companion object {
        // W32APIOptions.DEFAULT_OPTIONS sets Unicode + useLastError=true,
        // so Kernel32.GetLastError() will return the right code.
        val INSTANCE: User32Extended =
            Native.load("user32", User32Extended::class.java, W32APIOptions.DEFAULT_OPTIONS)
    }
}
