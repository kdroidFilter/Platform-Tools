// Inspired by the code from the jSystemThemeDetector project:
// https://github.com/Dansoftowner/jSystemThemeDetector/blob/master/src/main/java/com/jthemedetecor/WindowsThemeDetector.java

package io.github.kdroidfilter.platformtools.darkmodedetector

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.sun.jna.platform.win32.Advapi32
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinError
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinNT.KEY_READ
import com.sun.jna.platform.win32.WinReg
import com.sun.jna.platform.win32.WinReg.HKEY
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

// Initialize logger using kotlin-logging
private val logger = KotlinLogging.logger {}

/**
 * WindowsThemeDetector uses JNA to read the Windows registry value:
 *   HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize\AppsUseLightTheme
 *
 * If this value = 0 => Dark mode.
 * If this value = 1 => Light mode.
 *
 * The detector also monitors the registry for changes in real-time
 * by calling RegNotifyChangeKeyValue on a background thread.
 */
internal object WindowsThemeDetector {
    private const val REGISTRY_PATH = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize"
    private const val REGISTRY_VALUE = "AppsUseLightTheme"

    // A set of listeners to notify when the theme changes (true = dark, false = light).
    private val listeners: MutableSet<Consumer<Boolean>> = ConcurrentHashMap.newKeySet()

    @Volatile
    private var detectorThread: Thread? = null

    /**
     * Returns true if the system is in dark mode (i.e. registry value is 0),
     * or false if the system is in light mode (registry value is 1 or doesn't exist).
     */
    fun isDark(): Boolean {
        // Check if the registry value is 0 for "AppsUseLightTheme"
        return Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, REGISTRY_PATH, REGISTRY_VALUE) &&
                Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, REGISTRY_PATH, REGISTRY_VALUE) == 0
    }

    /**
     * Registers a listener. If it's the first listener, we start a background thread
     * to listen for changes in the registry key via RegNotifyChangeKeyValue.
     */
    fun registerListener(listener: Consumer<Boolean>) {
        synchronized(this) {
            listeners.add(listener)

            // If this is the first listener, or if a previous thread was interrupted,
            // start a new monitoring thread
            if (listeners.size == 1 || detectorThread?.isInterrupted == true) {
                startMonitoringThread()
            }
        }
    }

    /**
     * Removes a listener. If no listeners remain, we interrupt the monitoring thread.
     */
    fun removeListener(listener: Consumer<Boolean>) {
        synchronized(this) {
            listeners.remove(listener)
            if (listeners.isEmpty()) {
                detectorThread?.interrupt()
                detectorThread = null
            }
        }
    }

    /**
     * Creates and starts a background thread that monitors the registry
     * for changes to the theme key. When a change is detected, it reads the new theme
     * and notifies the listeners if there's a difference from the previous state.
     */
    private fun startMonitoringThread() {
        val thread = object : Thread("Windows Theme Detector Thread") {
            private var lastValue = isDark()

            override fun run() {
                logger.debug { "Windows theme monitor thread started" }

                // Open the registry key for reading
                val hKeyRef = WinReg.HKEYByReference()
                val openErr = Advapi32.INSTANCE.RegOpenKeyEx(
                    WinReg.HKEY_CURRENT_USER,
                    REGISTRY_PATH,
                    0,
                    KEY_READ,
                    hKeyRef
                )
                if (openErr != WinError.ERROR_SUCCESS) {
                    logger.error { "RegOpenKeyEx failed with code $openErr" }
                    return
                }
                val hKey: HKEY = hKeyRef.value

                try {
                    // Loop until the thread is interrupted
                    while (!isInterrupted) {
                        // Wait for registry changes
                        val notifyErr = Advapi32.INSTANCE.RegNotifyChangeKeyValue(
                            hKey,
                            false,
                            WinNT.REG_NOTIFY_CHANGE_LAST_SET,
                            null,
                            false
                        )
                        if (notifyErr != WinError.ERROR_SUCCESS) {
                            logger.error { "RegNotifyChangeKeyValue failed with code $notifyErr" }
                            return
                        }

                        val currentValue = isDark()
                        if (currentValue != lastValue) {
                            lastValue = currentValue
                            logger.debug { "Windows theme changed => dark: $currentValue" }
                            // Notify all listeners
                            val snapshot = listeners.toList()
                            for (l in snapshot) {
                                try {
                                    l.accept(currentValue)
                                } catch (e: RuntimeException) {
                                    logger.error(e) { "Error while notifying listener" }
                                }
                            }
                        }
                    }
                } finally {
                    // Close the registry key
                    logger.debug { "Detector thread closing registry key" }
                    Advapi32Util.registryCloseKey(hKey)
                }
            }
        }
        thread.isDaemon = true
        detectorThread = thread
        thread.start()
    }
}

/**
 * Composable function that returns whether Windows is currently in dark mode.
 *
 * It uses [WindowsThemeDetector] to read the registry value for AppsUseLightTheme.
 * It registers a listener to automatically update the Compose state if the registry changes.
 */
@Composable
internal fun isWindowsInDarkMode(): Boolean {
    // Compose state with initial value
    val darkModeState = remember { mutableStateOf(WindowsThemeDetector.isDark()) }

    DisposableEffect(Unit) {
        logger.debug { "Registering Windows dark mode listener in Compose" }
        val listener = Consumer<Boolean> { newValue ->
            logger.debug { "Windows dark mode updated: $newValue" }
            darkModeState.value = newValue
        }

        WindowsThemeDetector.registerListener(listener)

        onDispose {
            logger.debug { "Removing Windows dark mode listener in Compose" }
            WindowsThemeDetector.removeListener(listener)
        }
    }

    return darkModeState.value
}