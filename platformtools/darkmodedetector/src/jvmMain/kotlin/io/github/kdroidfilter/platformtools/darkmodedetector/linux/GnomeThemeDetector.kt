package io.github.kdroidfilter.platformtools.darkmodedetector.linux

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Logger.Companion.setMinSeverity
import co.touchlab.kermit.Severity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

// Logger for GNOME
private val gnomeLogger = Logger.withTag("GnomeThemeDetector").apply { setMinSeverity(Severity.Warn) }

/**
 * GNOME specific theme detector using gsettings and monitoring.
 */
internal object GnomeThemeDetector {
    private const val MONITORING_CMD = "gsettings monitor org.gnome.desktop.interface"
    private val GET_CMD = arrayOf(
        "gsettings get org.gnome.desktop.interface gtk-theme",
        "gsettings get org.gnome.desktop.interface color-scheme"
    )

    val darkThemeRegex = ".*dark.*".toRegex(RegexOption.IGNORE_CASE)

    @Volatile
    private var detectorThread: Thread? = null
    private val listeners: MutableSet<Consumer<Boolean>> = ConcurrentHashMap.newKeySet()

    fun isDark(): Boolean {
        return try {
            val runtime = Runtime.getRuntime()
            for (cmd in GET_CMD) {
                val process = runtime.exec(cmd)
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    val line = reader.readLine()
                    gnomeLogger.d { "Command '$cmd' output: $line" }
                    if (line != null && isDarkTheme(line)) {
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            gnomeLogger.e(e) { "Couldn't detect GNOME theme" }
            false
        }
    }

    private fun startMonitoring() {
        if (detectorThread?.isAlive == true) return
        detectorThread = object : Thread("GTK Theme Detector Thread") {
            private var lastValue: Boolean = isDark()
            override fun run() {
                gnomeLogger.d { "Starting GTK theme monitoring thread" }
                val runtime = Runtime.getRuntime()
                val process = try {
                    runtime.exec(MONITORING_CMD)
                } catch (e: Exception) {
                    gnomeLogger.e(e) { "Couldn't start monitoring process" }
                    return
                }

                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    while (!isInterrupted) {
                        val line = reader.readLine() ?: break
                        if (!line.contains("gtk-theme", ignoreCase = true) &&
                            !line.contains("color-scheme", ignoreCase = true)
                        ) continue

                        gnomeLogger.d { "Monitoring output: $line" }
                        val currentIsDark = isDarkThemeFromLine(line) ?: isDark()
                        if (currentIsDark != lastValue) {
                            lastValue = currentIsDark
                            gnomeLogger.d { "Detected theme change => dark: $currentIsDark" }
                            for (listener in listeners) {
                                try { listener.accept(currentIsDark) } catch (ex: RuntimeException) {
                                    gnomeLogger.e(ex) { "Exception while notifying listener" }
                                }
                            }
                        }
                    }
                    gnomeLogger.d { "GTK theme monitoring thread ending" }
                    if (process.isAlive) {
                        process.destroy()
                        gnomeLogger.d { "Monitoring process destroyed" }
                    }
                }
            }
        }.apply { isDaemon = true; start() }
    }

    private fun isDarkThemeFromLine(line: String): Boolean? {
        val tokens = line.split("\\s+".toRegex())
        if (tokens.size < 2) return null
        val value = tokens[1].lowercase().replace("'", "")
        return if (value.isNotBlank()) isDarkTheme(value) else null
    }

    private fun isDarkTheme(text: String): Boolean = darkThemeRegex.matches(text)

    fun registerListener(listener: Consumer<Boolean>) {
        val wasEmpty = listeners.isEmpty()
        listeners.add(listener)
        if (wasEmpty) {
            startMonitoring()
        }
    }

    fun removeListener(listener: Consumer<Boolean>) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            detectorThread?.interrupt()
            detectorThread = null
        }
    }
}

internal fun detectGnomeDarkTheme(): Boolean? {
    return try {
        val p1 = Runtime.getRuntime().exec(arrayOf("gsettings", "get", "org.gnome.desktop.interface", "gtk-theme"))
        val theme = BufferedReader(InputStreamReader(p1.inputStream)).use { it.readLine()?.trim('\'', '"') }
        if (!theme.isNullOrBlank() && theme.contains("dark", ignoreCase = true)) return true
        val p2 = Runtime.getRuntime().exec(arrayOf("gsettings", "get", "org.gnome.desktop.interface", "color-scheme"))
        val scheme = BufferedReader(InputStreamReader(p2.inputStream)).use { it.readLine()?.trim('\'', '"') }
        when (scheme?.lowercase()) {
            "prefer-dark" -> true
            "default", "prefer-light" -> false
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

@Composable
internal fun isGnomeInDarkMode(): Boolean {
    val darkModeState = remember { mutableStateOf(GnomeThemeDetector.isDark()) }

    DisposableEffect(Unit) {
        gnomeLogger.d { "Registering GNOME dark mode listener in Compose" }
        val listener = Consumer<Boolean> { newValue ->
            gnomeLogger.d { "GNOME dark mode updated: $newValue" }
            darkModeState.value = newValue
        }
        GnomeThemeDetector.registerListener(listener)
        onDispose {
            gnomeLogger.d { "Removing GNOME dark mode listener in Compose" }
            GnomeThemeDetector.removeListener(listener)
        }
    }
    return darkModeState.value
}
