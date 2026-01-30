package io.github.kdroidfilter.platformtools

import java.text.SimpleDateFormat
import java.util.Date

var allowPlatformToolsLogging: Boolean = false
var platformToolsLoggingLevel: LoggingLevel = LoggingLevel.WARN

class LoggingLevel(
    val priority: Int,
) {
    companion object {
        val VERBOSE = LoggingLevel(0)
        val DEBUG = LoggingLevel(1)
        val INFO = LoggingLevel(2)
        val WARN = LoggingLevel(3)
        val ERROR = LoggingLevel(4)
    }
}

private const val COLOR_RED = "\u001b[31m"
private const val COLOR_AQUA = "\u001b[36m"
private const val COLOR_LIGHT_GRAY = "\u001b[37m"
private const val COLOR_ORANGE = "\u001b[38;2;255;165;0m"
private const val COLOR_RESET = "\u001b[0m"

private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

private fun getCurrentTimestamp(): String = dateFormat.format(Date())

fun debugln(tag: String? = null, message: () -> String) {
    if (allowPlatformToolsLogging && platformToolsLoggingLevel.priority <= LoggingLevel.DEBUG.priority) {
        val prefix = tag?.let { "[$it] " } ?: ""
        println("${getCurrentTimestamp()} $prefix${message()}")
    }
}

fun verboseln(tag: String? = null, message: () -> String) {
    if (allowPlatformToolsLogging && platformToolsLoggingLevel.priority <= LoggingLevel.VERBOSE.priority) {
        val prefix = tag?.let { "[$it] " } ?: ""
        println(COLOR_LIGHT_GRAY + getCurrentTimestamp() + " " + prefix + message() + COLOR_RESET)
    }
}

fun infoln(tag: String? = null, message: () -> String) {
    if (allowPlatformToolsLogging && platformToolsLoggingLevel.priority <= LoggingLevel.INFO.priority) {
        val prefix = tag?.let { "[$it] " } ?: ""
        println(COLOR_AQUA + getCurrentTimestamp() + " " + prefix + message() + COLOR_RESET)
    }
}

fun warnln(tag: String? = null, message: () -> String) {
    if (allowPlatformToolsLogging && platformToolsLoggingLevel.priority <= LoggingLevel.WARN.priority) {
        val prefix = tag?.let { "[$it] " } ?: ""
        println(COLOR_ORANGE + getCurrentTimestamp() + " " + prefix + message() + COLOR_RESET)
    }
}

fun errorln(tag: String? = null, throwable: Throwable? = null, message: () -> String) {
    if (allowPlatformToolsLogging && platformToolsLoggingLevel.priority <= LoggingLevel.ERROR.priority) {
        val prefix = tag?.let { "[$it] " } ?: ""
        println(COLOR_RED + getCurrentTimestamp() + " " + prefix + message() + COLOR_RESET)
        throwable?.printStackTrace()
    }
}
