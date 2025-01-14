package io.github.kdroidfilter.platformtools.permissionhandler

actual fun hasNotificationPermission(): Boolean {
    return true
}

actual fun requestNotificationPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
    //No need to request Permission
    onGranted()
}