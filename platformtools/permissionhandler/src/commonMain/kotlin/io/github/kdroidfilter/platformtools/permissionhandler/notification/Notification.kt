package io.github.kdroidfilter.platformtools.permissionhandler.notification


expect fun hasNotificationPermission(): Boolean

expect fun requestNotificationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
)