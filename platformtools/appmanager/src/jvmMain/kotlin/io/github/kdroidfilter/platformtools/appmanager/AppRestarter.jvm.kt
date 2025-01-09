package io.github.kdroidfilter.platformtools.appmanager

import java.io.File
import kotlin.system.exitProcess

actual fun restartApplication() {
    try {
        val resolvedExecutable = File(ProcessHandle.current().info().command().get())
        val processBuilder = ProcessBuilder(resolvedExecutable.absolutePath)
        processBuilder.start()
        exitProcess(0)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
