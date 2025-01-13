package io.github.kdroidfilter.platformtools.permissionhandler.notification

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log

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
        const val REQUEST_CODE_NOTIFICATIONS = 1001
    }

    /**
     * Initializes the PermissionActivity and handles the logic for requesting notification permissions.
     *
     * This method checks the device's API level to determine whether the `POST_NOTIFICATIONS`
     * permission should be requested. If the API level meets the requirements, it retrieves the
     * request ID from the intent and proceeds to request the necessary permission.
     * Finishes the activity prematurely in cases where:
     * - The device does not meet the API level requirement.
     * - The request ID from the incoming intent is invalid.
     *
     * @param savedInstanceState The state information of the activity if it is being re-initialized
     * after a previous shutdown.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val requestId = intent.getIntExtra(EXTRA_REQUEST_ID, -1)
            if (requestId == -1) {
                Log.e("PermissionActivity", "Invalid requestId in intent")
                finish()
                return
            }

            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_NOTIFICATIONS
            )
        } else {
            finish()
        }
    }

    /**
     * Handles the result of permission requests and triggers corresponding callbacks.
     *
     * This method is invoked after a runtime permission request is completed. It checks the
     * request code to ensure it matches the one for notification permissions. Based on the
     * outcome of the permission request, the method retrieves and executes the appropriate
     * callback from the `PermissionCallbackManager`.
     *
     * @param requestCode The integer request code originally supplied to the permission request.
     * @param permissions The array of requested permissions.
     * @param grantResults The array of integers representing the grant results for the requested
     * permissions. Each index in the array corresponds to the index of the permission in the
     * `permissions` array.
     */
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


