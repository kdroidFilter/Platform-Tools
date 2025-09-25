// src/main/kotlin/io/github/kdroidfilter/platformtools/clipboardmanager/linux/X11Structs.kt
package io.github.kdroidfilter.platformtools.clipboardmanager.linux.x11

import com.sun.jna.*
import com.sun.jna.Structure.FieldOrder
import com.sun.jna.Union

@FieldOrder("type", "serial", "send_event", "display", "window",
    "selection", "time", "selection_time", "pad")
class XFixesSelectionNotifyEvent : Structure() {
    @JvmField var type: Int = 0
    @JvmField var serial: NativeLong = NativeLong(0)
    @JvmField var send_event: Int = 0
    @JvmField var display: Pointer? = null
    @JvmField var window: NativeLong = NativeLong(0)
    @JvmField var selection: NativeLong = NativeLong(0)
    @JvmField var time: NativeLong = NativeLong(0)
    @JvmField var selection_time: NativeLong = NativeLong(0)
    @JvmField var pad: ByteArray = ByteArray(92)
}

class XEvent : Union() {
    @JvmField var type: Int = 0
    @JvmField var xfixesSelection: XFixesSelectionNotifyEvent = XFixesSelectionNotifyEvent()
    @JvmField var pad: Array<NativeLong> = Array(24) { NativeLong(0) }
}
