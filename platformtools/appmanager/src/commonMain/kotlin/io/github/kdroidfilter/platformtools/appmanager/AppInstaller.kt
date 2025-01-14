package io.github.kdroidfilter.platformtools.appmanager

import java.io.File

interface AppInstaller {

    /**
     * Installs an application from the specified file.
     *
     * This method handles the installation of an APK file. It requires permission to install
     * applications from unknown sources, which should be granted before invoking this method.
     * The installation result is communicated via the provided callback.
     *
     * @param appFile The APK file to be installed.
     * @param onResult A callback invoked with the installation result. The callback
     *        parameters are:
     *        - [success]: Indicates whether the installation succeeded.
     *        - [message]: An optional message providing additional context, typically in case of an error.
     */
    suspend fun installApp(appFile: File, onResult: (success: Boolean, message: String?) -> Unit)

    /**
     * Uninstalls an application based on the provided package name.
     *
     * @param packageName The package name of the application to be uninstalled.
     * @param onResult A callback invoked with the uninstallation result. The callback
     *        parameters are:
     *        - [success]: Indicates whether the uninstallation succeeded.
     *        - [message]: An optional message providing additional context, typically in case of an error.
     */
    suspend fun uninstallApp(packageName: String, onResult: (success: Boolean, message: String?) -> Unit)

    /**
     * Uninstalls the actual application from the device.
     *
     * @param onResult A callback invoked with the uninstallation result.
     * The callback parameters are:
     * - [success]: Indicates whether the uninstallation succeeded.
     * - [message]: An optional message providing additional context, typically in case of an error.
     */
    suspend fun uninstallApp(onResult: (success: Boolean, message: String?) -> Unit)
}


/**
 * Retrieves the instance of the `AppInstaller` interface for managing app installation tasks.
 *
 * The `AppInstaller` provides methods to check and request permissions for installing packages,
 * as well as functionality to install or uninstall applications.
 *
 * @return An instance of `AppInstaller` to handle app installation-related operations.
 */
expect fun getAppInstaller(): AppInstaller