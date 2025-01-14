package io.github.kdroidfilter.platformtools.permissionhandler

actual fun hasInstallPermission(): Boolean = false
actual fun requestInstallPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
    onDenied()
}