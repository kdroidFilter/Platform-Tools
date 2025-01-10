package io.github.kdroidfilter.platformtools



/**
 * Represents the operating systems a device or environment can run.
 *
 * This enum class is used to identify the underlying platform or operating system
 * being utilized by an application, and it is commonly utilized in platform-specific logic.
 *
 * Enum Constants:
 * - `WINDOWS`: Represents the Microsoft Windows operating system.
 * - `MACOS`: Represents the macOS operating system from Apple.
 * - `LINUX`: Represents Linux-based operating systems.
 * - `ANDROID`: Represents the Android operating system.
 * - `IOS`: Represents the iOS operating system from Apple.
 * - `UNKNOWN`: Represents an unrecognized or unsupported operating system.
 */
enum class OperatingSystem {
    WINDOWS, MACOS, LINUX, ANDROID, IOS, UNKNOWN
}



/**
 * Determines the operating system on which the application is currently running.
 *
 * @return An instance of [OperatingSystem] representing the current platform or operating system.
 */
expect fun getOperatingSystem(): OperatingSystem