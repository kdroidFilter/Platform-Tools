package io.github.kdroidfilter.platformtools


/**
 * Represents a platform or operating system that the application can run on.
 *
 * This enum is used to identify the current platform or operating system
 * within the application, enabling platform-specific logic or optimizations.
 */
enum class Platform {
    WINDOWS, MAC, LINUX, ANDROID, IOS, JS, WASMJS, UNKNOWN
}


/**
 * Determines the current platform or operating system that the application is running on.
 *
 * The function is expected to return a value from the `Platform` enum, which represents
 * various known operating systems or platforms. This value can be used for enabling
 * platform-specific logic or configurations within the application.
 *
 * @return The current platform as a value of the `Platform` enum.
 */
expect fun getPlatform(): Platform