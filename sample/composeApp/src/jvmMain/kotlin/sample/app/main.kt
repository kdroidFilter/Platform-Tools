import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.kdroidfilter.platformtools.darkmodedetector.windows.setWindowsAdaptiveTitleBar
import io.github.kdroidfilter.platformtools.rtlwindows.setWindowsRtlLayout

import sample.app.App
import java.awt.Dimension

fun main() = application {
    Window(
        title = "sample",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        window.setWindowsAdaptiveTitleBar()
        window.setWindowsRtlLayout()
        App()
    }
}

