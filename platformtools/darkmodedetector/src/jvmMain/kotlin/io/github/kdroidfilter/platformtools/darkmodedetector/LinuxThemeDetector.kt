package io.github.kdroidfilter.platformtools.darkmodedetector

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

//**
//* LinuxThemeDetector uses "gsettings monitor org.gnome.desktop.interface" to track
//* GTK theme changes in real-time. When "gtk-theme" or "color-scheme" lines are detected,
//* it checks if the new theme is dark and notifies registered listeners if there's a change.
//
internal object LinuxThemeDetector {
    // Commands for monitoring and retrieving the current GTK theme
    private const val MONITORING_CMD = "gsettings monitor org.gnome.desktop.interface"
    private val GET_CMD = arrayOf(
        "gsettings get org.gnome.desktop.interface gtk-theme",
        "gsettings get org.gnome.desktop.interface color-scheme"
    )

    // Regex to detect if a theme string is 'dark'
    val darkThemeRegex = ".*dark.*".toRegex(RegexOption.IGNORE_CASE)

    // A thread that runs the monitoring command
    @Volatile
    private var detectorThread: Thread? = null

    // A set of listeners to be notified of changes (true = dark, false = light)
    private val listeners: MutableSet<Consumer<Boolean>> = ConcurrentHashMap.newKeySet()

    /**
     * Attempts to read the current theme by running the GET_CMD commands.
     * If any of them matches "dark", we assume dark mode is on.
     */
    fun isDark(): Boolean {
        return try {
            val runtime = Runtime.getRuntime()
            for (cmd in GET_CMD) {
                val process = runtime.exec(cmd)
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    val line = reader.readLine()
                    logger.debug { "Command '$cmd' output: $line" }
                    if (line != null && isDarkTheme(line)) {
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            logger.error(e) { "Couldn't detect Linux OS theme" }
            false
        }
    }

    /**
     * Spawns a thread that executes "gsettings monitor org.gnome.desktop.interface"
     * and reads lines in real-time. When we detect changes to "gtk-theme" or
     * "color-scheme", we parse the line to see if it's dark.
     *
     * If the new state differs from the previous one, we notify listeners.
     */
    private fun startMonitoring() {
        if (detectorThread?.isAlive == true) return  // already started

        detectorThread = object : Thread("GTK Theme Detector Thread") {
            private var lastValue: Boolean = isDark()

            override fun run() {
                logger.debug { "Starting GTK theme monitoring thread" }
                val runtime = Runtime.getRuntime()
                val process = try {
                    runtime.exec(MONITORING_CMD)
                } catch (e: Exception) {
                    logger.error(e) { "Couldn't start monitoring process" }
                    return
                }

                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    while (!isInterrupted) {
                        val line = reader.readLine() ?: break
                        // Example line: gtk-theme: 'Adwaita-dark'
                        // or color-scheme: default / prefer-dark
                        if (!line.contains("gtk-theme", ignoreCase = true) &&
                            !line.contains("color-scheme", ignoreCase = true)
                        ) {
                            continue
                        }

                        logger.debug { "Monitoring output: $line" }
                        val currentIsDark = isDarkThemeFromLine(line)
                            ?: isDark() // fallback to a full check if we can't parse the line

                        if (currentIsDark != lastValue) {
                            lastValue = currentIsDark
                            logger.debug { "Detected theme change => dark: $currentIsDark" }
                            for (listener in listeners) {
                                try {
                                    listener.accept(currentIsDark)
                                } catch (ex: RuntimeException) {
                                    logger.error(ex) { "Exception while notifying listener" }
                                }
                            }
                        }
                    }
                    logger.debug { "GTK theme monitoring thread ending" }
                    if (process.isAlive) {
                        process.destroy()
                        logger.debug { "Monitoring process destroyed" }
                    }
                }
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    /**
     * Checks if a line from "gsettings monitor" indicates a dark theme.
     * For example, "gtk-theme: 'Adwaita-dark'" or "color-scheme: prefer-dark".
     *
     * If we cannot parse the line, returns null.
     */
    private fun isDarkThemeFromLine(line: String): Boolean? {
        // The line might look like:
        //  gtk-theme: 'Adwaita-dark'
        //  color-scheme: default
        val tokens = line.split("\\s+".toRegex())
        if (tokens.size < 2) {
            return null
        }
        val value = tokens[1].lowercase().replace("'", "")
        return if (value.isNotBlank()) {
            isDarkTheme(value)
        } else {
            null
        }
    }

    /**
     * True if the string matches ".*dark.*" (case-insensitive).
     */
    private fun isDarkTheme(text: String): Boolean {
        return darkThemeRegex.matches(text)
    }

    /**
     * Registers a listener to be notified of theme changes. If this is the first listener,
     * we start the monitoring thread.
     */
    fun registerListener(listener: Consumer<Boolean>) {
        val wasEmpty = listeners.isEmpty()
        listeners.add(listener)
        if (wasEmpty) {
            startMonitoring()
        }
    }

    /**
     * Removes a listener. If no listeners remain, we interrupt the monitoring thread.
     */
    fun removeListener(listener: Consumer<Boolean>) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            detectorThread?.interrupt()
            detectorThread = null
        }
    }
}

/**
 * Composable function that returns whether the system is in dark mode on Linux (GNOME/GTK).
 *
 * It uses [LinuxThemeDetector] to listen for changes via "gsettings monitor org.gnome.desktop.interface"
 * and automatically updates the Compose state when a new line indicates a different theme.
 */
@Composable
internal fun isLinuxInDarkMode(): Boolean {
    val darkModeState = remember { mutableStateOf(LinuxThemeDetector.isDark()) }

    DisposableEffect(Unit) {
        logger.debug { "Registering Linux dark mode listener in Compose" }
        val listener = Consumer<Boolean> { newValue ->
            logger.debug { "Linux dark mode updated: $newValue" }
            darkModeState.value = newValue
        }
        LinuxThemeDetector.registerListener(listener)
        onDispose {
            logger.debug { "Removing Linux dark mode listener in Compose" }
            LinuxThemeDetector.removeListener(listener)
        }
    }

    return darkModeState.value
}