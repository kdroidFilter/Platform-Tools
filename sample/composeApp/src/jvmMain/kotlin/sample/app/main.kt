import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.kdroidfilter.platformtools.darkmodedetector.mac.setMacOsAdaptiveTitleBar
import io.github.kdroidfilter.platformtools.darkmodedetector.windows.setWindowsAdaptiveTitleBar
import io.github.kdroidfilter.platformtools.rtlwindows.setWindowsRtlLayout
import sample.app.App
import java.awt.Dimension

fun main() {
    // Set macOS adaptive title bar before application starts
    setMacOsAdaptiveTitleBar() // Default is AUTO which uses system setting
    // You can also use DARK or LIGHT mode:
    // setMacOsAdaptiveTitleBar(MacOSTitleBarMode.DARK)
    // setMacOsAdaptiveTitleBar(MacOSTitleBarMode.LIGHT)

    application {
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
}
