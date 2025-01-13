package io.github.kdroidfilter.platformtools.permissionhandler


expect fun hasNotificationPermission(): Boolean

expect fun requestNotificationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
)