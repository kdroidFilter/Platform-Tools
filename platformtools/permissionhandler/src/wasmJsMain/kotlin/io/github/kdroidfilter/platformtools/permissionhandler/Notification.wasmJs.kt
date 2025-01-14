package io.github.kdroidfilter.platformtools.permissionhandler

import io.github.kdroidfilter.platformtools.permissionhandler.NotificationPermission.entries
import kotlin.js.Promise


/**
 * Exposes Notification.permission in Kotlin/Wasm.
 */
@JsFun("() => Notification.permission")
private external fun getNotificationPermissionValue(): String

/**
 * Exposes Notification.requestPermission() in Kotlin/Wasm.
 */
@JsFun("() => Notification.requestPermission()")
private external fun requestNotificationPermissionJs(): Promise<JsAny>

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
actual fun hasNotificationPermission(): Boolean {
    // Call the external function instead of js("Notification.permission")
    val currentPermission = getNotificationPermissionValue()
    return currentPermission == NotificationPermission.GRANTED.value
}

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
    // We handle "default" as denied in the simpler function
    handleNotificationPermissionRequest(
        onGranted = onGranted,
        onDenied = onDenied,
        onDefault = onDenied
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
        val requestPermission = requestNotificationPermissionJs()
        requestPermission.then { permission ->
            // Safely cast JsAny to String
            val permString = permission as String

            when (NotificationPermission.fromValue(permString)) {
                NotificationPermission.GRANTED -> onGranted()
                NotificationPermission.DENIED -> onDenied()
                NotificationPermission.DEFAULT -> onDefault()
            }
            null // Explicitly return null to satisfy the lambda
        }
    } catch (e: Exception) {
        println("Unexpected error during permission request $e")
        onDenied()
    }
}

