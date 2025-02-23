// Inspired by the code from the jSystemThemeDetector project:
// https://github.com/Dansoftowner/jSystemThemeDetector/blob/master/src/main/java/com/jthemedetecor/MacOSThemeDetector.java

package io.github.kdroidfilter.platformtools.darkmodedetector

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.jangassen.jfa.foundation.Foundation
import de.jangassen.jfa.foundation.ID
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.function.Consumer
import java.util.regex.Pattern

// Initialize logger using kotlin-logging
private val logger = KotlinLogging.logger {}

/**
 * MacOSThemeDetector registers an observer with NSDistributedNotificationCenter
 * to detect theme changes in macOS. It reads the system preference "AppleInterfaceStyle"
 * (which is "Dark" when in dark mode) from NSUserDefaults.
 */
internal object MacOSThemeDetector {

    // Set of listeners to notify when the theme changes (true = dark, false = light)
    private val listeners: MutableSet<Consumer<Boolean>> = ConcurrentHashMap.newKeySet()

    // Pattern to match if the style string contains "dark" (case insensitive)
    private val darkPattern: Pattern = Pattern.compile(".*dark.*", Pattern.CASE_INSENSITIVE)

    // Executor to run callbacks in a dedicated thread
    private val callbackExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "MacOS Theme Detector Thread").apply { isDaemon = true }
    }

    /**
     * Callback invoked by the Objective-C runtime when the system posts
     * the "AppleInterfaceThemeChangedNotification" notification.
     * The expected Objective-C signature is "v@" (void return, no parameters).
     */
    @JvmStatic
    private val themeChangedCallback = object : com.sun.jna.Callback {
        fun callback() {
            callbackExecutor.execute {
                val isDark = isDark()
                logger.debug { "Theme change detected. Dark mode: $isDark" }
                notifyListeners(isDark)
            }
        }
    }

    // Initialize the observer on startup.
    init {
        initObserver()
    }

    /**
     * Initializes the Objective-C observer.
     * This method creates a custom Objective-C class ("NSColorChangesObserver")
     * that extends NSObject, adds a method "handleAppleThemeChanged:" that calls our callback,
     * and registers the observer with NSDistributedNotificationCenter.
     */
    private fun initObserver() {
        logger.debug { "Initializing macOS theme observer" }
        val pool = Foundation.NSAutoreleasePool()
        try {
            val delegateClass: ID = Foundation.allocateObjcClassPair(
                Foundation.getObjcClass("NSObject"),
                "NSColorChangesObserver"
            )
            if (!ID.NIL.equals(delegateClass)) {
                val selector = Foundation.createSelector("handleAppleThemeChanged:")
                val added = Foundation.addMethod(delegateClass, selector, themeChangedCallback, "v@")
                if (!added) {
                    logger.error { "Failed to add observer method to NSColorChangesObserver" }
                }
                Foundation.registerObjcClassPair(delegateClass)
            }
            val delegateObj = Foundation.invoke("NSColorChangesObserver", "new")
            Foundation.invoke(
                Foundation.invoke("NSDistributedNotificationCenter", "defaultCenter"),
                "addObserver:selector:name:object:",
                delegateObj,
                Foundation.createSelector("handleAppleThemeChanged:"),
                Foundation.nsString("AppleInterfaceThemeChangedNotification"),
                ID.NIL
            )
            logger.debug { "Observer successfully registered" }
        } finally {
            pool.drain()
        }
    }

    /**
     * Reads the system theme by checking the "AppleInterfaceStyle" preference.
     * Returns true if the system is in dark mode, false otherwise.
     */
    fun isDark(): Boolean {
        val pool = Foundation.NSAutoreleasePool()
        return try {
            val userDefaults = Foundation.invoke("NSUserDefaults", "standardUserDefaults")
            val styleKey = Foundation.nsString("AppleInterfaceStyle")
            val result = Foundation.invoke(userDefaults, "objectForKey:", styleKey)
            val styleString = Foundation.toStringViaUTF8(result)
            darkPattern.matcher(styleString ?: "").matches()
        } catch (e: Exception) {
            logger.error(e) { "Error reading system theme" }
            false
        } finally {
            pool.drain()
        }
    }

    fun registerListener(listener: Consumer<Boolean>) {
        listeners.add(listener)
    }

    fun removeListener(listener: Consumer<Boolean>) {
        listeners.remove(listener)
    }

    private fun notifyListeners(isDark: Boolean) {
        listeners.forEach { it.accept(isDark) }
    }
}


/**
 * A helper composable function that returns the current macOS dark mode state,
 * updating automatically when the system theme changes.
 */
@Composable
internal fun isMacOsInDarkMode(): Boolean {
    val darkModeState = remember { mutableStateOf(MacOSThemeDetector.isDark()) }
    DisposableEffect(Unit) {
        logger.debug { "Registering macOS dark mode listener in Compose" }
        val listener = Consumer<Boolean> { newValue ->
            logger.debug { "Compose macOS dark mode updated: $newValue" }
            darkModeState.value = newValue
        }
        MacOSThemeDetector.registerListener(listener)
        onDispose {
            logger.debug { "Removing macOS dark mode listener in Compose" }
            MacOSThemeDetector.removeListener(listener)
        }
    }
    return darkModeState.value
}