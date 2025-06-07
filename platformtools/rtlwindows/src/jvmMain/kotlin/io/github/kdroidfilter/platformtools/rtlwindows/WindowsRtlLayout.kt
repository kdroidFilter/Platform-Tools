package io.github.kdroidfilter.platformtools.rtlwindows

import com.sun.jna.Native
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR
import com.sun.jna.platform.win32.WinDef
import io.github.kdroidfilter.platformtools.OperatingSystem
import io.github.kdroidfilter.platformtools.getOperatingSystem
import java.awt.Window


/**
 * Apply or remove RTL mirroring on the given AWT/Compose [Window].
 *
 */
fun Window.setWindowsRtlLayout() {
    if (getOperatingSystem() != OperatingSystem.WINDOWS) return

    val isRtl = !this.componentOrientation.isLeftToRight
    // Obtain HWND from AWT component
    val hwnd = WinDef.HWND(Native.getComponentPointer(this))

    val current = User32.INSTANCE.GetWindowLongPtr(hwnd, GWL_EXSTYLE).toLong()
    val newStyle = if (isRtl) {
        current or WS_EX_LAYOUTRTL.toLong() or WS_EX_RTLREADING.toLong()
    } else {
        current and WS_EX_LAYOUTRTL.inv().toLong() and WS_EX_RTLREADING.inv().toLong()
    }

    if (newStyle != current) {
        User32.INSTANCE.SetWindowLongPtr(hwnd, GWL_EXSTYLE, LONG_PTR(newStyle))
        // Tell the window manager to re-evaluate styles
        User32.INSTANCE.SetWindowPos(
            hwnd, null, 0, 0, 0, 0,
            SWP_NOMOVE or SWP_NOSIZE or SWP_NOZORDER or SWP_FRAMECHANGED
        )
    }
}
