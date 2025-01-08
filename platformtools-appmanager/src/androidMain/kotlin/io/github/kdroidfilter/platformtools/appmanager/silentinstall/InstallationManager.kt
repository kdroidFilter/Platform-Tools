package io.github.kdroidfilter.platformtools.appmanager.silentinstall

/**
 * Manages installation callbacks and notifies the result of installation processes.
 *
 * This singleton object is designed to handle app installation result callbacks.
 * It registers a callback function that can be invoked once an installation process
 * completes, and ensures the callback is reset post-invocation to avoid redundant calls.
 */
object InstallationManager {
    private var installCallback: ((Boolean, String?) -> Unit)? = null

    /**
     * Registers a callback for installation results.
     * @param callback Function to be called with the installation result.
     */
    fun setInstallCallback(callback: (Boolean, String?) -> Unit) {
        installCallback = callback
    }

    /**
     * Notifies the registered callback with the installation result.
     * @param success Indicates whether the installation was successful.
     * @param message Message associated with the result.
     */
    internal fun notifyInstallResult(success: Boolean, message: String?) {
        installCallback?.invoke(success, message)
        // Reset the callback after use to avoid multiple calls
        installCallback = null
    }
}
