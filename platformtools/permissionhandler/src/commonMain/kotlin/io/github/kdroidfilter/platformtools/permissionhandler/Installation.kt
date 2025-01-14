package io.github.kdroidfilter.platformtools.permissionhandler

expect fun hasInstallPermission(): Boolean

expect fun requestInstallPermission(onGranted: () -> Unit, onDenied: () -> Unit)

