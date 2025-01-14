package io.github.kdroidfilter.platformtools.permissionhandler


/**
 * Checks if the app has notification permission.
 *
 * @return true if the app has notification permission, false otherwise.
 */
expect fun hasNotificationPermission(): Boolean

/**
 * Requests notification permission on the platform.
 *
 * @param onGranted Callback invoked when the permission is granted.
 * @param onDenied Callback invoked when the permission is denied.
 */
expect fun requestNotificationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
)