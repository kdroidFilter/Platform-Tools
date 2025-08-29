package sample.app

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import io.github.kdroidfilter.platformtools.*
import io.github.kdroidfilter.platformtools.darkmodedetector.linux.isLinuxInDarkMode
import io.github.kdroidfilter.platformtools.darkmodedetector.linux.rememberKdeDarkModeState

@Composable
internal actual fun LinuxInfoSection() {
    // Only relevant on Linux
    if (getOperatingSystem() != OperatingSystem.LINUX) return

    val de: LinuxDesktopEnvironment? = detectLinuxDesktopEnvironment()
    val dark = isLinuxInDarkMode()

    Column {
        Text("Linux Desktop Environment: ${de ?: "Unknown/Not detected"}", style = MaterialTheme.typography.bodyLarge)
        Text("Linux Dark Theme: $dark", style = MaterialTheme.typography.bodyLarge)

        if (de == LinuxDesktopEnvironment.KDE) {
            val s = rememberKdeDarkModeState()
            if (s != null) {
                Text("KDE Window dark: ${s.windowTheme}", style = MaterialTheme.typography.bodyLarge)
                Text("KDE Panel dark: ${s.panelTheme}", style = MaterialTheme.typography.bodyLarge)
                if (s.isMixed) {
                    Text("KDE theme is mixed (window vs panel)", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Text("KDE theme state: Unknown", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
