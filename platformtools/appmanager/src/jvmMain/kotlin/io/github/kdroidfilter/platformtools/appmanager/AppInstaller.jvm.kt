package io.github.kdroidfilter.platformtools.appmanager

import io.github.kdroidfilter.platformtools.Platform
import io.github.kdroidfilter.platformtools.appmanager.WindowsPrivilegeHelper.installOnWindows
import io.github.kdroidfilter.platformtools.getPlatform
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}


actual fun getAppInstaller(): AppInstaller = DesktopInstaller()

/**
 * The `DesktopInstaller` class is responsible for handling the installation of applications
 * on desktop operating systems such as Windows, Linux, and macOS. This class implements the
 * `AppInstaller` interface and provides platform-specific installation logic for each supported
 * operating system.
 */
class DesktopInstaller : AppInstaller {

    /**
     * Determines whether the application can request permission to install packages
     * depending on the operating system being used.
     *
     * @return `true` if the operation is supported on the detected operating system
     * (Windows, Linux, or Mac); `false` otherwise for unknown or unsupported systems.
     */
    override suspend fun canRequestInstallPackages(): Boolean {
        logger.debug { "Checking if install packages request can be made." }
        return when (getPlatform()) {
            Platform.WINDOWS -> {
                logger.debug { "OS=Windows, returning true directly." }
                true
            }
            Platform.LINUX -> {
                logger.debug { "OS=Linux, returning true directly." }
                true
            }
            Platform.MAC -> {
                logger.debug { "OS=Mac, assuming elevation is not needed." }
                true
            }
            else -> {
                logger.debug { "Unknown OS, install permissions denied." }
                false
            }
        }
    }

    /**
     * Requests the necessary permission to install packages based on the detected operating system.
     *
     * The method identifies the current operating system and performs the necessary steps
     * or logs the appropriate handling mechanism for installing packages. The behavior
     * depends on the detected platform:
     *
     * - **Windows**: No elevation request is necessary, as package installation does not
     *   typically require additional privileges in the context of this application.
     * - **Linux**: Package installation is handled using `pkexec` for privilege elevation.
     * - **Mac**: Elevation handling for package installation is not implemented yet.
     * - **Other/Unknown OS**: The method logs the lack of support for package installation
     *   on unsupported or unidentified operating systems.
     *
     * This method does not provide feedback to the user or take any direct installation actions
     * but ensures appropriate logging and handling mechanisms for each supported platform.
     */
    override suspend fun requestInstallPackagesPermission() {
        logger.debug { "Requesting install packages permission." }
        when (getPlatform()) {
            Platform.WINDOWS -> {
                logger.debug { "OS=Windows, no elevation request needed." }
            }
            Platform.LINUX -> {
                logger.debug { "OS=Linux, elevation handled by pkexec." }
            }
            Platform.MAC -> {
                logger.debug { "OS=Mac, elevation not implemented." }
            }
            else -> {
                logger.debug { "Unsupported OS for elevation." }
            }
        }
    }

    /**
     * Installs a given application file based on the detected operating system.
     * The method supports multiple platforms (Windows, Linux, Mac) and uses platform-specific
     * installation mechanisms. If the operating system is unsupported, the installation fails with
     * an appropriate message.
     *
     * @param appFile The application file to be installed.
     * @param onResult A callback that provides the result of the installation.
     *                 The first parameter indicates success or failure (Boolean).
     *                 The second parameter contains an optional error message (String?).
     */
    override suspend fun installApp(appFile: File, onResult: (Boolean, String?) -> Unit) {
        logger.debug { "Starting installation for file: ${appFile.absolutePath}" }
        val osDetected = getPlatform()
        logger.debug { "Detected OS: $osDetected" }

        when (osDetected) {
            Platform.WINDOWS -> installOnWindows(appFile, onResult)
            Platform.LINUX -> installOnLinux(appFile, onResult)
            Platform.MAC -> installOnMac(appFile, onResult)
            else -> {
                val message = "Installation not supported for: ${getPlatform()}"
                logger.debug { message }
                onResult(false, message)
            }
        }
    }

    /**
     * Installs a .deb package on a Linux system using `pkexec` and `dpkg`.
     * This method attempts to elevate privileges if required and executes the installation command.
     *
     * @param installerFile The .deb file to be installed.
     * @param onResult A callback to handle the result of the installation.
     *                 The first parameter is a Boolean indicating success or failure.
     *                 The second parameter is an optional String containing an error message or output.
     */
    private fun installOnLinux(installerFile: File, onResult: (Boolean, String?) -> Unit) {
        logger.debug { "Starting installation for .deb package." }

        if (!installerFile.exists()) {
            val msg = "DEB file not found: ${installerFile.absolutePath}"
            logger.debug { msg }
            onResult(false, msg)
            return
        }

        logger.debug { "Executing dpkg via pkexec, which will prompt for a password if needed." }

        val command = listOf("pkexec", "dpkg", "-i", installerFile.absolutePath)
        logger.debug { "pkexec command: $command" }

        runCommand(command) { success, output ->
            logger.debug { "pkexec + dpkg result: success=$success, output=$output" }

            if (!success) {
                logger.debug { "dpkg via pkexec failed." }
                onResult(false, output)
            } else {
                logger.debug { "DEB package installation succeeded!" }
                onResult(true, output)
            }
        }
    }

    /**
     * Executes a system command using the provided list of command arguments
     * and returns the result asynchronously through a callback function.
     *
     * @param command A list of strings representing the command to execute and its arguments.
     * @param onResult A callback function to handle the result of the command execution.
     *                 The first parameter is a Boolean indicating success (true) or failure (false).
     *                 The second parameter is an optional String containing the output of the command
     *                 or an error message in case of failure.
     */
    private fun runCommand(command: List<String>, onResult: (Boolean, String?) -> Unit) {
        logger.debug { "Executing command: $command" }
        try {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            logger.debug { "Command completed (exitCode=$exitCode). Output: $output" }

            if (exitCode == 0) {
                onResult(true, "Success. Output: $output")
            } else {
                onResult(false, "Failure (code=$exitCode). Output: $output")
            }

        } catch (e: Exception) {
            logger.debug { "Exception in runCommand(): ${e.message}" }
            e.printStackTrace()
            onResult(false, "Exception during execution: ${e.message}")
        }
    }

    /**
     * Installs a package on macOS using an installer script with administrator privileges.
     *
     * @param installerFile The installer file to be executed, typically a `.pkg` file.
     * @param onResult A callback to handle the result of the operation. The first parameter
     *                 indicates success or failure as a Boolean. The second parameter provides
     *                 an optional error message as a String.
     */
    private fun installOnMac(installerFile: File, onResult: (Boolean, String?) -> Unit) {
        val script = """
        do shell script "installer -pkg ${installerFile.absolutePath} -target /" with administrator privileges
    """.trimIndent()

        try {
            val process = ProcessBuilder("osascript", "-e", script).start()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                onResult(true, null)
            } else {
                val errorMessage = process.errorStream.bufferedReader().readText()
                onResult(false, errorMessage)
            }
        } catch (e: Exception) {
            onResult(false, e.message)
        }
    }


    override suspend fun uninstallApp(packageName: String, onResult: (success: Boolean, message: String?) -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun uninstallApp(onResult: (success: Boolean, message: String?) -> Unit) {
        TODO("Not yet implemented")
    }
}
