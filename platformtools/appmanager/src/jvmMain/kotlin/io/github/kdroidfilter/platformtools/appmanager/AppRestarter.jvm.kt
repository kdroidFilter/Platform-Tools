package io.github.kdroidfilter.platformtools.appmanager

import java.io.File
import kotlin.system.exitProcess

object AppManager {
    val applicationExecutablePath: String by lazy {
        try {
            File(ProcessHandle.current().info().command().get()).absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to get application executable path")
        }
    }
}

actual fun restartApplication() {
    try {
        val processBuilder = ProcessBuilder(AppManager.applicationExecutablePath)
        processBuilder.start()
        exitProcess(0)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
