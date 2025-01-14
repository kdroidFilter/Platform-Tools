package io.github.kdroidfilter.platformtools.permissionhandler

actual fun hasInstallPermission(): Boolean = true

actual fun requestInstallPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
  onGranted
}