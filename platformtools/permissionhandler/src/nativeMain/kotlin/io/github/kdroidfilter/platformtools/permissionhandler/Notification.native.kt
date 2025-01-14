package io.github.kdroidfilter.platformtools.permissionhandler

import platform.UserNotifications.*
import kotlin.concurrent.AtomicInt

/**
 * Checks if the app has permission to display notifications on iOS.
 *
 * This function uses `UNUserNotificationCenter` to retrieve the current notification settings and determines
 * if the authorization status is either `authorized` or `provisional`.
 *
 * Note: This function actively waits for the asynchronous callback to complete, which may cause a block
 * in the current thread. Use with caution in a production environment.
 *
 * @return `true` if notification permissions are granted or provisionally granted, `false` otherwise.
 */
actual fun hasNotificationPermission(): Boolean {
    var isAuthorized = false
    val semaphore = AtomicInt(0)

    UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
        isAuthorized = settings?.authorizationStatus == UNAuthorizationStatusAuthorized ||
                settings?.authorizationStatus == UNAuthorizationStatusProvisional
        semaphore.value = 1
    }

    // Wait for the callback to be executed (synchronization needed because iOS is asynchronous)
    while (semaphore.value == 0) {
        // Active loop to wait for the result
    }

    return isAuthorized
}


/**
 * Requests notification permission from the user on iOS.
 *
 * This function uses `UNUserNotificationCenter` to request authorization for notifications with options
 * for alerts, sounds, and badges.
 *
 * @param onGranted Callback to be executed if the user grants notification permissions.
 * @param onDenied Callback to be executed if the user denies notification permissions.
 *
 * Required Configuration in `Info.plist`:
 * Add the following keys to your `Info.plist` file to provide a clear description to the user when the app
 * requests notification permissions:
 *
 * ```xml
 * <key>NSLocationWhenInUseUsageDescription</key>
 * <string>We need access to notifications to keep you updated about important events.</string>
 * ```
 *
 * If you are using remote notifications, make sure to enable the "Push Notifications" capability
 * in your Xcode project.
 */
actual fun requestNotificationPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
    val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    val options: ULong = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge

    notificationCenter.requestAuthorizationWithOptions(options) { granted, error ->
        if (granted) {
            onGranted()
        } else {
            onDenied()
        }
    }
}
