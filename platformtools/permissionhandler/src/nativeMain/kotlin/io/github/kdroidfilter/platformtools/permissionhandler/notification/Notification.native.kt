package io.github.kdroidfilter.platformtools.permissionhandler.notification

actual fun hasNotificationPermission(): Boolean {
    return false //TODO
}

actual fun requestNotificationPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
    TODO("Not yet implemented")
}