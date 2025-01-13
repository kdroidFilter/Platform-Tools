package io.github.kdroidfilter.platformtools.permissionhandler.manager

/**
 * Manages permission callbacks for asynchronous request handling.
 *
 * This object is responsible for storing and managing callbacks associated with permission
 * requests. Callbacks are registered with unique request IDs, allowing specific actions
 * to be executed depending on whether the permission request is granted or denied.
 */
internal object PermissionCallbackManager {
    private val callbackMap = mutableMapOf<Int, Pair<() -> Unit, () -> Unit>>()
    private var currentRequestId = 0

    @Synchronized
    fun registerCallbacks(onGranted: () -> Unit, onDenied: () -> Unit): Int {
        val requestId = currentRequestId++
        callbackMap[requestId] = Pair(onGranted, onDenied)
        return requestId
    }

    @Synchronized
    fun unregisterCallbacks(requestId: Int) {
        callbackMap.remove(requestId)
    }

    fun getCallbacks(requestId: Int): Pair<() -> Unit, () -> Unit>? {
        return callbackMap[requestId]
    }
}