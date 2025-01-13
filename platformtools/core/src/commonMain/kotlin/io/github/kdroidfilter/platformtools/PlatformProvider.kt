package io.github.kdroidfilter.platformtools

/**
 * Represents the various platforms or environments where an application can run.
 *
 * This enum class is used to identify the target runtime platform, and it is
 * commonly utilized for platform-specific logic or feature implementation.
 *
 * Enum Constants:
 * - `ANDROID`: Represents the Android platform.
 * - `JVM`: Represents the Java Virtual Machine environment.
 * - `IOS_NATIVE`: Represents the iOS native platform.
 * - `JS`: Represents the JavaScript environment.
 * - `WASM_JS`: Represents the WebAssembly JavaScript platform.
 * - `LINUX_NATIVE`: Represents Linux native platforms.
 * - `MAC_OS_NATIVE`: Represents the macOS native platform.
 * - `WINDOWS_NATIVE`: Represents Windows native platforms.
 */
enum class Platform {
    ANDROID, JVM, IOS_NATIVE, JS, WASM_JS, LINUX_NATIVE, MAC_OS_NATIVE, WINDOWS_NATIVE
}

/**
 * Determines the platform on which the application is currently running.
 *
 * @return An instance of [Platform] representing the specific platform or environment, such as Android, JVM, Native, or JavaScript.
 */
expect fun getPlatform(): Platform