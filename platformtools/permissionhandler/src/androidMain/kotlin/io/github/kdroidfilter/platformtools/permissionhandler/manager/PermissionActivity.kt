package io.github.kdroidfilter.platformtools.permissionhandler.manager

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import io.github.kdroidfilter.platformtools.permissionhandler.MediaType
import io.github.kdroidfilter.platformtools.permissionhandler.hasInstallPermission
import io.github.kdroidfilter.platformtools.permissionhandler.hasOverlayPermission

/**
 * Activity for handling runtime permission requests, specifically for
 * notification permissions.
 *
 * This class is used internally to request permissions from the user.
 * It implements the necessary logic to handle the results of permission
 * requests and trigger corresponding callbacks. It operates in conjunction
 * with the `PermissionCallbackManager` to manage callbacks for granted or
 * denied permissions.
 *
 * The activity finishes its execution as soon as the permission outcome is
 * resolved to reduce visibility and user interaction.
 *
 * Key functionalities:
 * - Requests the `POST_NOTIFICATIONS` permission on devices running
 *   Android 13 (API level 33) or higher.
 * - Handles permission results and invokes the appropriate callback
 *   functions.
 *
 * Usage of this class is designed to be internal to the application; it
 * should not be instantiated directly by developers.
 */
internal class PermissionActivity : Activity() {

    companion object {
        const val EXTRA_REQUEST_ID = "extra_request_id"
        const val EXTRA_REQUEST_TYPE = "extra_request_type"
        const val EXTRA_MEDIA_TYPES = "extra_media_types"

        const val REQUEST_CODE_NOTIFICATIONS = 1001
        const val REQUEST_TYPE_NOTIFICATION = "notification"

        const val REQUEST_CODE_INSTALL = 1002
        const val REQUEST_TYPE_INSTALL = "install"


        const val REQUEST_CODE_OVERLAY = 1003
        const val REQUEST_TYPE_OVERLAY = "overlay"

        const val REQUEST_CODE_LOCATION = 1004
        const val REQUEST_TYPE_LOCATION = "location"
        const val EXTRA_PRECISE_LOCATION = "extra_precise_location"

        const val REQUEST_CODE_BACKGROUND_LOCATION = 1005
        const val REQUEST_TYPE_BACKGROUND_LOCATION = "background_location"

        const val REQUEST_CODE_CAMERA = 1006
        const val REQUEST_TYPE_CAMERA = "camera"

        const val REQUEST_CODE_READ_CONTACTS = 1007
        const val REQUEST_TYPE_READ_CONTACTS = "read_contacts"

        const val REQUEST_CODE_WRITE_CONTACTS = 1008
        const val REQUEST_TYPE_WRITE_CONTACTS = "write_contacts"

        const val REQUEST_CODE_RECORD_AUDIO = 1009
        const val REQUEST_TYPE_RECORD_AUDIO = "record_audio"

        const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1010
        const val REQUEST_TYPE_READ_EXTERNAL_STORAGE = "read_external_storage"

        const val REQUEST_CODE_READ_MEDIA = 1011
        const val REQUEST_TYPE_READ_MEDIA = "read_media"

        const val REQUEST_CODE_BLUETOOTH = 1012
        const val REQUEST_TYPE_BLUETOOTH = "bluetooth"
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
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
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

            REQUEST_TYPE_LOCATION -> {
                val preciseLocation = intent.getBooleanExtra(EXTRA_PRECISE_LOCATION, true)
                val permission = if (preciseLocation) {
                    Manifest.permission.ACCESS_FINE_LOCATION
                } else {
                    Manifest.permission.ACCESS_COARSE_LOCATION
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(permission), REQUEST_CODE_LOCATION)
                }
            }

            REQUEST_TYPE_BACKGROUND_LOCATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        REQUEST_CODE_BACKGROUND_LOCATION
                    )
                } else {
                    // ACCESS_BACKGROUND_LOCATION is not required below Q
                    val callbacks = PermissionCallbackManager.getCallbacks(requestId)
                    callbacks?.first?.invoke()
                    PermissionCallbackManager.unregisterCallbacks(requestId)
                }
            }

            REQUEST_TYPE_CAMERA -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CODE_CAMERA
                    )
                } else {
                    finish()
                }
            }

            REQUEST_TYPE_READ_CONTACTS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        REQUEST_CODE_READ_CONTACTS
                    )
                } else {
                    finish()
                }
            }

            REQUEST_TYPE_WRITE_CONTACTS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_CONTACTS),
                        REQUEST_CODE_WRITE_CONTACTS
                    )
                } else {
                    finish()
                }
            }

            REQUEST_TYPE_RECORD_AUDIO -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        REQUEST_CODE_RECORD_AUDIO
                    )
                } else {
                    finish()
                }
            }

            REQUEST_TYPE_READ_EXTERNAL_STORAGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_CODE_READ_EXTERNAL_STORAGE
                    )
                } else {
                    finish()
                }
            }

            REQUEST_TYPE_READ_MEDIA -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val mediaTypes = intent.getStringArrayExtra(EXTRA_MEDIA_TYPES)?.mapNotNull { mediaTypeName ->
                        try {
                            MediaType.valueOf(mediaTypeName)
                        } catch (e: IllegalArgumentException) {
                            Log.e("PermissionActivity", "Invalid media type: $mediaTypeName", e)
                            null
                        }
                    } ?: emptyList()

                    if (mediaTypes.isEmpty()) {
                        Log.e("PermissionActivity", "No valid media types provided")
                        finish()
                        return
                    }

                    val permissionsToRequest = mediaTypes.map { mediaType ->
                        when (mediaType) {
                            MediaType.IMAGES -> Manifest.permission.READ_MEDIA_IMAGES
                            MediaType.VIDEO -> Manifest.permission.READ_MEDIA_VIDEO
                            MediaType.AUDIO -> Manifest.permission.READ_MEDIA_AUDIO
                        }
                    }.toTypedArray()

                    requestPermissions(permissionsToRequest, REQUEST_CODE_READ_MEDIA)
                } else {
                    finish()
                }
            }

            REQUEST_TYPE_BLUETOOTH -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
                    requestPermissions(
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                        REQUEST_CODE_BLUETOOTH
                    )
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
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val requestId = intent.getIntExtra(EXTRA_REQUEST_ID, -1)
        if (requestId == -1) {
            finish()
            return
        }

        when (requestCode) {
            REQUEST_CODE_NOTIFICATIONS,
            REQUEST_CODE_LOCATION,
            REQUEST_CODE_BACKGROUND_LOCATION,
            REQUEST_CODE_CAMERA,
            REQUEST_CODE_READ_CONTACTS,
            REQUEST_CODE_WRITE_CONTACTS,
            REQUEST_CODE_RECORD_AUDIO,
            REQUEST_CODE_READ_EXTERNAL_STORAGE,
            REQUEST_CODE_READ_MEDIA,
            REQUEST_CODE_BLUETOOTH
                -> {
                val callbacks = PermissionCallbackManager.getCallbacks(requestId)
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    callbacks?.first?.invoke()   // onGranted()
                } else {
                    callbacks?.second?.invoke()  // onDenied()
                }
                PermissionCallbackManager.unregisterCallbacks(requestId)
            }
        }


        finish()
    }
}
