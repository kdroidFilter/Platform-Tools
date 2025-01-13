package io.github.kdroidfilter.platformtools.permissionhandler.permission

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import io.github.kdroidfilter.platformtools.permissionhandler.hasInstallPermission
import io.github.kdroidfilter.platformtools.permissionhandler.hasOverlayPermission
import io.github.kdroidfilter.platformtools.permissionhandler.permission.PermissionCallbackManager

/**
 * Activity for handling runtime permission requests, specifically for notification permissions.
 *
 * This class is used internally to request permissions from the user. It implements the
 * necessary logic to handle the results of permission requests and trigger corresponding
 * callbacks. It operates in conjunction with the `PermissionCallbackManager` to manage
 * callbacks for granted or denied permissions.
 *
 * The activity finishes its execution as soon as the permission outcome is resolved
 * to reduce visibility and user interaction.
 *
 * Key functionalities:
 * - Requests the `POST_NOTIFICATIONS` permission on devices running Android 13 (API level 33)
 *   or higher.
 * - Handles permission results and invokes the appropriate callback functions.
 *
 * Usage of this class is designed to be internal to the application; it should not
 * be instantiated directly by developers.
 */
internal class PermissionActivity : Activity() {

    companion object {
        const val EXTRA_REQUEST_ID = "extra_request_id"
        const val EXTRA_REQUEST_TYPE = "extra_request_type"
        const val REQUEST_CODE_NOTIFICATIONS = 1001
        const val REQUEST_CODE_INSTALL = 1002
        const val REQUEST_CODE_OVERLAY = 1003

        const val REQUEST_TYPE_NOTIFICATION = "notification"
        const val REQUEST_TYPE_INSTALL = "install"
        const val REQUEST_TYPE_OVERLAY = "overlay"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestId = intent.getIntExtra(EXTRA_REQUEST_ID, -1)
        val requestType = intent.getStringExtra(EXTRA_REQUEST_TYPE)

        if (requestId == -1 || requestType == null) {
            Log.e("PermissionActivity", "Invalid requestId or requestType in intent")
            finish()
            return
        }

        when (requestType) {
            REQUEST_TYPE_NOTIFICATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        REQUEST_CODE_NOTIFICATIONS
                    )
                } else {
                    finish()
                }
            }

            REQUEST_TYPE_INSTALL -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName"))
                    startActivityForResult(intent, REQUEST_CODE_INSTALL)
                } else {
                    finish()
                }
            }

            REQUEST_TYPE_OVERLAY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivityForResult(intent, REQUEST_CODE_OVERLAY)
                } else {
                    finish()
                }
            }

            else -> {
                Log.e("PermissionActivity", "Unsupported request type: $requestType")
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_INSTALL -> {
                val requestId = intent.getIntExtra(EXTRA_REQUEST_ID, -1)
                if (requestId != -1) {
                    val callbacks = PermissionCallbackManager.getCallbacks(requestId)
                    if (hasInstallPermission()) {
                        callbacks?.first?.invoke()
                    } else {
                        callbacks?.second?.invoke()
                    }
                    PermissionCallbackManager.unregisterCallbacks(requestId)
                }
            }

            REQUEST_CODE_OVERLAY -> {
                val requestId = intent.getIntExtra(EXTRA_REQUEST_ID, -1)
                if (requestId != -1) {
                    val callbacks = PermissionCallbackManager.getCallbacks(requestId)
                    if (hasOverlayPermission()) {
                        callbacks?.first?.invoke()
                    } else {
                        callbacks?.second?.invoke()
                    }
                    PermissionCallbackManager.unregisterCallbacks(requestId)
                }
            }
        }
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            val requestId = intent.getIntExtra(EXTRA_REQUEST_ID, -1)
            if (requestId != -1) {
                val callbacks = PermissionCallbackManager.getCallbacks(requestId)
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callbacks?.first?.invoke()
                } else {
                    callbacks?.second?.invoke()
                }
                PermissionCallbackManager.unregisterCallbacks(requestId)
            }
        }
        finish()
    }
}
