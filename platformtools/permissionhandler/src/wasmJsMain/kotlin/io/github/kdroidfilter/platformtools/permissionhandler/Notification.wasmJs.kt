package io.github.kdroidfilter.platformtools.permissionhandler

import io.github.kdroidfilter.platformtools.permissionhandler.NotificationPermission.entries
import kotlin.js.Promise

/**
 * Represents the different permission states for notifications.
 */
internal enum class NotificationPermission(val value: String) {
    GRANTED("granted"),
    DENIED("denied"),
    DEFAULT("default");

    companion object {
        fun fromValue(value: String): NotificationPermission =
            entries.firstOrNull { it.value == value } ?: DEFAULT
    }
}


/**
 * Checks if the application has permission to display notifications.
 *
 * @return true if the application has permission, false otherwise.
 */
actual fun hasNotificationPermission(): Boolean =
    (js("Notification.permission") as String) == NotificationPermission.GRANTED.value

/**
 * Requests permission to display notifications.
 *
 * @param onGranted Callback executed if the permission is granted
 * @param onDenied Callback executed if the permission is denied
 */
actual fun requestNotificationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    handleNotificationPermissionRequest(
        onGranted = onGranted,
        onDenied = onDenied,
        onDefault = onDenied  // By default, treat as denied
    )
}

/**
 * Extended version of the permission request handling the "default" case.
 *
 * @param onGranted Callback executed if the permission is granted
 * @param onDenied Callback executed if the permission is denied
 * @param onDefault Callback executed if the response is neither granted nor denied
 */
fun requestNotificationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
    onDefault: () -> Unit
) {
    handleNotificationPermissionRequest(onGranted, onDenied, onDefault)
}

/**
 * Internal function to handle the permission request.
 */
private fun handleNotificationPermissionRequest(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
    onDefault: () -> Unit
) {
    try {
        val requestPermission = js("Notification.requestPermission()") as Promise<String>
        requestPermission
            .then { permission ->
                when (NotificationPermission.fromValue(permission)) {
                    NotificationPermission.GRANTED -> onGranted()
                    NotificationPermission.DENIED -> onDenied()
                    NotificationPermission.DEFAULT -> onDefault()
                }
                null // Explicitly return null
            }
    } catch (e: Exception) {
        println("Unexpected error during permission request $e")
        onDenied()
    }
}
