package io.github.kdroidfilter.platformtools.appmanager

import com.sun.jna.platform.win32.*
import com.sun.jna.ptr.IntByReference
import java.io.File
import java.io.InputStreamReader

object WindowsPrivilegeHelper {

    /**
     * Checks if the process is already running with elevated privileges (admin).
     */
    fun isProcessElevated(): Boolean {
        val hToken = WinNT.HANDLEByReference()
        try {
            // Open the token of the current process
            val success = Advapi32.INSTANCE.OpenProcessToken(
                Kernel32.INSTANCE.GetCurrentProcess(),
                WinNT.TOKEN_QUERY,
                hToken
            )
            if (!success) return false

            val elevation = WinNT.TOKEN_ELEVATION()
            val size = IntByReference()
            val result = Advapi32.INSTANCE.GetTokenInformation(
                hToken.value,
                WinNT.TOKEN_INFORMATION_CLASS.TokenElevation,
                elevation,
                elevation.size(),
                size
            )
            if (!result) return false

            // If TokenIsElevated != 0, then the process is in admin mode.
            return elevation.TokenIsElevated != 0
        } finally {
            Kernel32.INSTANCE.CloseHandle(hToken.value)
        }
    }

    /**
     * Requests elevation (UAC) to install an MSI via msiexec.
     * Waits for the msiexec process to finish and returns the result via the callback.
     */
    private fun requestAdminPrivilegesForMsi(msiPath: String, onResult: (Boolean, String?) -> Unit) {
        val shellExecuteInfo = ShellAPI.SHELLEXECUTEINFO().apply {
            cbSize = size()
            lpVerb = "runas" // Requests elevation (UAC)
            lpFile = "msiexec" // Executable in the PATH
            lpParameters = "/i \"$msiPath\" /quiet /l*v \"${File(msiPath).parentFile?.absolutePath}\\installation_log.txt\""
            nShow = WinUser.SW_SHOWNORMAL
            fMask = Shell32.SEE_MASK_NOCLOSEPROCESS
        }

        val success = Shell32.INSTANCE.ShellExecuteEx(shellExecuteInfo)
        if (!success) {
            val errorCode = Kernel32.INSTANCE.GetLastError()
            onResult(false, "ShellExecuteEx failed for the MSI. Error code: $errorCode")
            return
        }

        val hProcess = shellExecuteInfo.hProcess
        if (hProcess == null) {
            onResult(false, "Null process handle after ShellExecuteEx.")
            return
        }

        try {
            // Wait for the msiexec process to finish
            val waitResult = Kernel32.INSTANCE.WaitForSingleObject(hProcess, WinBase.INFINITE)
            if (waitResult != WinBase.WAIT_OBJECT_0) {
                onResult(false, "Failed to wait for the msiexec process.")
                return
            }

            // Get the exit code of msiexec
            val exitCode = IntByReference()
            val getExitSuccess = Kernel32.INSTANCE.GetExitCodeProcess(hProcess, exitCode)
            if (!getExitSuccess) {
                val errorCode = Kernel32.INSTANCE.GetLastError()
                onResult(false, "GetExitCodeProcess failed. Error code: $errorCode")
                return
            }

            if (exitCode.value == 0) {
                onResult(true, null)
            } else {
                onResult(false, "MSI installation failed with exit code: ${exitCode.value}")
            }

        } finally {
            Kernel32.INSTANCE.CloseHandle(hProcess)
        }
    }

    /**
     * Installs an MSI file on Windows.
     * @param installerFile The MSI file to install.
     * @param onResult Callback (success: Boolean, errorMessage: String?).
     * @param requireAdmin Indicates whether installation must be done in admin mode.
     */
    fun installOnWindows(
        installerFile: File,
        onResult: (Boolean, String?) -> Unit,
        requireAdmin: Boolean = false
    ) {
        // 1. Check if admin privileges are explicitly required
        if (requireAdmin && !isProcessElevated()) {
            // If admin rights are required and we are not in admin mode,
            // request elevation and wait for the result.
            requestAdminPrivilegesForMsi(installerFile.absolutePath, onResult)
            return
        }

        // 2. Validate the file
        if (!installerFile.exists() || !installerFile.extension.equals("msi", ignoreCase = true)) {
            onResult(false, "File not found or incorrect extension (MSI expected).")
            return
        }

        // 3. Prepare the msiexec command
        val command = listOf(
            "msiexec",
            "/i", installerFile.absolutePath,
            "/quiet",
            "/l*v", "${installerFile.parentFile?.absolutePath}\\installation_log.txt"
        )

        try {
            // 4. Build the ProcessBuilder
            val processBuilder = ProcessBuilder(command).apply {
                redirectErrorStream(true)
            }

            // 5. Start the msiexec process
            val process = processBuilder.start()
            val exitCode = process.waitFor()

            // 6. Read the output stream
            val output = InputStreamReader(process.inputStream).readText().trim()

            // 7. Check the exit code
            if (exitCode == 0) {
                onResult(true, null)
            } else {
                val errorMessage = """
                    Installation failed (return code: $exitCode).
                    Output:
                    $output
                """.trimIndent()
                onResult(false, errorMessage)
            }
        } catch (e: Exception) {
            onResult(false, "Exception during installation: ${e.message}")
        }
    }
}
