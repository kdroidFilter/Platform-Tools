package io.github.kdroidfilter.platformtools

/**
 * Represents the various operating systems a Kotlin application can run on.
 *
 * This enumeration is typically used in multi-platform projects to determine
 * the target platform's operating system environment. The specific instance
 * can be retrieved using platform-specific implementations of the `getOperatingSystem`
 * function.
 *
 * Possible values:
 * - `WINDOWS`: Represents the Windows operating system.
 * - `MAC`: Represents the macOS operating system.
 * - `LINUX`: Represents Linux-based operating systems.
 * - `ANDROID`: Represents the Android operating system.
 * - `IOS`: Represents the iOS operating system.
 * - `JS`: Represents JavaScript environments.
 * - `WASMJS`: Represents WebAssembly environments executed via JavaScript.
 * - `UNKNOWN`: Represents an unrecognized or unsupported operating system.
 */
enum class OperatingSystem {
    WINDOWS, MAC, LINUX, ANDROID, IOS, JS, WASMJS, UNKNOWN
}

/**
 * Determines and returns the current operating system on which the code is running.
 *
 * @return An instance of [OperatingSystem] representing the detected operating system.
 */
expect fun getOperatingSystem(): OperatingSystem