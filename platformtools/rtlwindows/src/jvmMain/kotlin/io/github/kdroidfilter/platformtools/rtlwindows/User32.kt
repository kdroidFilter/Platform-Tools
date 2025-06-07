package io.github.kdroidfilter.platformtools.rtlwindows

import com.sun.jna.Native
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions

/* ---------- User32 bindings ---------- */
internal interface User32 : StdCallLibrary {
    companion object {
        val INSTANCE: User32 =
            Native.load("user32", User32::class.java, W32APIOptions.DEFAULT_OPTIONS)
    }

    fun GetWindowLongPtr(hWnd: WinDef.HWND, nIndex: Int): LONG_PTR
    fun SetWindowLongPtr(hWnd: WinDef.HWND, nIndex: Int, dwNewLong: LONG_PTR): LONG_PTR
    fun SetWindowPos(
        hWnd: WinDef.HWND,
        hWndInsertAfter: WinDef.HWND?,
        X: Int,
        Y: Int,
        cx: Int,
        cy: Int,
        uFlags: Int
    ): Boolean
}

 const val GWL_EXSTYLE = -20
 const val WS_EX_LAYOUTRTL = 0x0040_0000       // Mirror the entire window
 const val WS_EX_RTLREADING = 0x0000_2000      // RTL title-bar text
 const val SWP_NOMOVE = 0x0001
 const val SWP_NOSIZE = 0x0002
 const val SWP_NOZORDER = 0x0004
 const val SWP_FRAMECHANGED = 0x0020