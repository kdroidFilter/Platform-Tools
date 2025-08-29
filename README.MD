# PlatformTools

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kdroidfilter/platformtools.core?label=Maven%20Central)](https://central.sonatype.com/search?q=io.github.kdroidfilter)

**PlatformTools** is a Kotlin Multiplatform library designed to provide platform-specific utilities and tools for managing operating systems, application installation, and release fetching seamlessly across various platforms. The library is modular and divided into three main components: core, appmanager, and releasefetcher.

## 📑 Table of Contents
- [Core Module](#-core-module)
- [Dark Mode Detection Module](#-dark-mode-detection-module)
- [RTL Windows Module](#-rtl-windows-module-jvm-only)
- [AppManager Module](#-appmanager-module)
- [Release Fetcher](#-release-fetcher)
- [License](#-license)
- [Contributions](#-contributions)
- [Demo Application](#-demo-application)

---

## 🌐 Core Module

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kdroidfilter/platformtools.core?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.kdroidfilter/platformtools.core)

The **Core** module of the KMP Platform Tools library provides utilities for detecting the underlying operating system and platform, enabling platform-specific logic in Kotlin Multiplatform (KMP) projects. Below, we will detail the functionality and demonstrate usage of the provided features.

### ⚙️ Function: `getOperatingSystem`

The `getOperatingSystem` function determines the operating system on which the application is currently running. This function is expected to be implemented for each target platform in a Kotlin Multiplatform project.

#### ℹ️ Note

While this functionality may appear to contradict the principles of Kotlin Multiplatform by focusing on operating systems rather than platforms, it is important to note that `getOperatingSystem` returns the operating system itself and not the platform. For example, on JVM, JavaScript, or WASM targets, this function will return the operating system being used (e.g., `WINDOWS`, `MACOS`, `LINUX`) rather than distinguishing between different platforms or runtime environments.

```kotlin
fun getOperatingSystem(): OperatingSystem
```

### ⚙️ Function: `getPlatform`

The `getPlatform` function determines the specific platform or runtime environment on which the application is running. This is useful for small adjustments in shared code that depend on the platform.

```kotlin
fun getPlatform(): Platform
```

### 🔧 Example Usage

Here is an example of how to use `getOperatingSystem` and `getPlatform` with separate `when` blocks for each:

```kotlin
when (val os = getOperatingSystem()) {
    OperatingSystem.WINDOWS -> println("Logic for Windows OS")
    OperatingSystem.MACOS -> println("Logic for macOS")
    OperatingSystem.LINUX -> println("Logic for Linux OS")
    OperatingSystem.ANDROID -> println("Logic for Android OS")
    OperatingSystem.IOS -> println("Logic for iOS OS")
    OperatingSystem.UNKNOWN -> println("Logic for unknown OS")
}

when (val platform = getPlatform()) {
    Platform.ANDROID -> println("Logic for Android platform")
    Platform.JVM -> println("Logic for JVM platform")
    Platform.IOS_NATIVE -> println("Logic for iOS Native platform")
    Platform.JS -> println("Logic for JavaScript platform")
    Platform.WASM_JS -> println("Logic for WebAssembly JS platform")
    Platform.LINUX_NATIVE -> println("Logic for Linux Native platform")
    Platform.MAC_OS_NATIVE -> println("Logic for macOS Native platform")
    Platform.WINDOWS_NATIVE -> println("Logic for Windows Native platform")
}
```

### 🛠️ Android and JVM Only functions

#### `getCacheDir`

```kotlin
val cacheDir: File = getCacheDir()
```

#### `getAppVersion`

```kotlin
val version = getAppVersion()
println("App version: $version")
```

### 🛠️ Android only functions

#### `getAppVersion(packageName: String)`

Under Android, it's possible to take a package name as a parameter to get the version of another application.

```kotlin
val version = getAppVersion("com.sample.anotherApp")
println("App version: $version")
```

---

This library is available on Maven Central. To include this library in your project, add the following dependency to your `build.gradle.kts`:

```kotlin
implementation("io.github.kdroidfilter:platformtools.core:<version>")
```

## 🌙 Dark Mode Detection Module

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kdroidfilter/platformtools.darkmodedetector?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.kdroidfilter/platformtools.darkmodedetector)

The **Dark Mode Detection** module of PlatformTools enhances theme management by introducing a reactive function, `isSystemInDarkMode`, that accurately detects system-wide dark mode settings across all supported platforms.

### ⚙️ Function: `isSystemInDarkMode`

The `isSystemInDarkMode` function determines whether the system is currently in dark mode. Unlike `isSystemInDarkTheme`, which is not reactive on desktop platforms, `isSystemInDarkMode` provides real-time updates when the system's theme changes.

#### 🔍 How It Works

- On **desktop platforms (Windows, macOS, Linux)**, `isSystemInDarkMode` reacts to system theme changes. It uses native APIs on Windows/macOS, and Desktop Environment detection on Linux (GNOME/KDE are reactive).
- On **other platforms (Android, iOS, JVM, JavaScript, WebAssembly)**, `isSystemInDarkMode` falls back to `isSystemInDarkTheme`, ensuring compatibility across all targets.
- This makes `isSystemInDarkMode` a **fully cross-platform and reactive solution** for detecting dark mode preferences in a Kotlin Multiplatform project.

```kotlin
fun isSystemInDarkMode(): Boolean
```

### 🔧 Example Usage

Here is an example of using `isSystemInDarkMode` in a Jetpack Compose-based UI:

```kotlin
MaterialTheme(
    colorScheme = if (isSystemInDarkMode()) darkColorScheme() else lightColorScheme()
) {
    // Your UI content
}
```

This allows automatic adaptation of the UI theme based on the system’s dark mode setting, ensuring a seamless user experience.

### 🔗 Comparison with `isSystemInDarkTheme`

| Function | Reactive | Desktop Support | Other Platforms |
|----------|----------|----------------|----------------|
| `isSystemInDarkTheme` | ❌ No | ❌ Not reactive | ✅ Yes |
| `isSystemInDarkMode` | ✅ Yes | ✅ Fully supported (via JNA) | ✅ Yes (fallback to `isSystemInDarkTheme`) |

---

## 🎨 Adaptive Title Bar Support

On **Windows and macOS**, the application title bar does not automatically update based on dark mode settings. PlatformTools provides solutions for ensuring adaptive title bars.

### 🖥️ Windows Support

For Windows, use the following function to enable an adaptive title bar:

```kotlin
fun Window.setWindowsAdaptiveTitleBar(dark: Boolean = isSystemInDarkMode())
```

Example usage in a Compose for Desktop application:

```kotlin
Window(
    title = "sample",
    state = rememberWindowState(width = 800.dp, height = 600.dp),
    onCloseRequest = ::exitApplication,
) {
    window.minimumSize = Dimension(350, 600)
    window.setWindowsAdaptiveTitleBar()
    App()
}
```

### 🍏 macOS Support

On macOS, you can enable an adaptive title bar using the `setMacOsAdaptiveTitleBar` function. This function should be called before the application window is created:

#### Parameters of `setMacOsAdaptiveTitleBar`

The `setMacOsAdaptiveTitleBar` function takes a single parameter of type `MacOSTitleBarMode`:

```kotlin
fun setMacOsAdaptiveTitleBar(mode: MacOSTitleBarMode = MacOSTitleBarMode.AUTO)
```

The `MacOSTitleBarMode` enum has three possible values:

- **`MacOSTitleBarMode.AUTO`** (default): Uses the system setting for appearance. The title bar will automatically adapt to the system's dark/light mode preference.
- **`MacOSTitleBarMode.DARK`**: Forces dark mode for the title bar using `NSAppearanceNameDarkAqua`, regardless of the system setting.
- **`MacOSTitleBarMode.LIGHT`**: Forces light mode for the title bar using `NSAppearanceNameAqua`, regardless of the system setting.

#### Example Usage

```kotlin
fun main() {
    // Set macOS adaptive title bar before application starts
    setMacOsAdaptiveTitleBar() // Default is AUTO which uses system setting
    // You can also use DARK or LIGHT mode:
    // setMacOsAdaptiveTitleBar(MacOSTitleBarMode.DARK)
    // setMacOsAdaptiveTitleBar(MacOSTitleBarMode.LIGHT)

    application {
        // Your application code
    }
}
```

Alternatively, you can add the following configuration in the `gradle.build.kts` file, but using the function above gives you more control:

```kotlin
compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            macOS {
                jvmArgs(
                    "-Dapple.awt.application.appearance=system"
                )
            }
        }
    }
}
```

### 🐧 Linux Support

On Linux, the title bar updates correctly with system-wide dark mode settings, so no additional configuration is required.

#### Linux Desktop Environment and Dark Mode Detection

API changes (Linux/KDE) – Summary:
- isLinuxInDarkMode() is now reactive for GNOME and KDE. On KDE it uses the new isKdeInDarkMode().
- getKdeThemeState() is auto-reactive and updated in background; no manual monitoring required.
- rememberKdeDarkModeState() is public for Compose UIs.

The core module includes utilities to detect the running Linux Desktop Environment and whether a dark theme is active. This is used by the dark mode detector on JVM/Linux and can also be used directly if needed.

- Enum of supported environments:
  - `GNOME`, `KDE`, `XFCE`, `CINNAMON`, `MATE`, `UNKNOWN`.
- Functions (JVM/Linux):
  - `fun detectLinuxDesktopEnvironment(): LinuxDesktopEnvironment?`
    - Returns `null` if the OS is not Linux; otherwise detects the DE using `XDG_CURRENT_DESKTOP` and `DESKTOP_SESSION`.
  - `fun isLinuxDarkTheme(): Boolean?`
    - Returns `true`/`false` depending on whether a dark theme is detected for the current DE.
    - Returns `null` if not Linux or if detection fails/unknown.
  - `@Composable fun isLinuxInDarkMode(): Boolean`
    - Reactive variant for Compose. Uses GNOME/KDE live detectors when available; falls back to one-time checks for other DEs.

Detection strategy per desktop environment:
- KDE/Plasma: reads `kdeglobals` via `kreadconfig5 --file kdeglobals --group General --key ColorScheme`; falls back to `lookandfeeltool --current`.
- GNOME: checks `gsettings get org.gnome.desktop.interface gtk-theme`; if not conclusive, checks `gsettings get org.gnome.desktop.interface color-scheme` for `prefer-dark`.
- XFCE: reads theme name via `xfconf-query -c xsettings -p /Net/ThemeName`.
- Cinnamon: `gsettings get org.cinnamon.desktop.interface gtk-theme`.
- MATE: `gsettings get org.mate.interface gtk-theme`.

Example (JVM/Linux):
```kotlin
// import io.github.kdroidfilter.platformtools.detectLinuxDesktopEnvironment
// import io.github.kdroidfilter.platformtools.isLinuxDarkTheme

val de = detectLinuxDesktopEnvironment()
if (de == null) {
    println("Not running on Linux")
} else {
    println("DE: $de | Dark: ${isLinuxDarkTheme()}")
}
```

Note: These helpers execute system tools (`gsettings`, `xfconf-query`, `kreadconfig5`, `lookandfeeltool`) when available. If the required tool is missing for a given DE, the function may return `null`.

##### KDE theme details

Note: The KDE APIs below have been enhanced to be reactive out of the box. See the API changes summary just after this section.

In addition to the general dark theme detection, KDE users can get a more granular state out of the box:

- `fun isKdePanelDark(): Boolean?`
  - Detects whether the Plasma panel (taskbar) itself is dark. Useful for mixed configurations where windows are light but the panel uses a dark theme.
  - Strategy: reads Plasma/Plasmashell theme from `plasmarc`/`plasmashellrc` via `kreadconfig5`; falls back to `kdeglobals` `LookAndFeelPackage`.
- `data class KdeThemeState(val windowTheme: Boolean?, val panelTheme: Boolean?)`
  - Convenience data structure with helpers: `isMixed`, `isFullDark`, `isFullLight`.
- `fun getKdeThemeState(): KdeThemeState?`
  - Now auto-reactive: returns a cached state that is updated in the background using DBus signals; subsequent calls reflect the latest theme state. Returns `null` if not on KDE.
- `@Composable fun rememberKdeDarkModeState(): KdeThemeState?`
  - Compose-friendly helper that exposes the reactive KDE state. Internally manages monitoring lifecycle; recommended for UI.
- `@Composable internal fun isKdeInDarkMode(): Boolean`
  - Convenience boolean derived from KDE theme state. Used internally by `isLinuxInDarkMode()` when DE is KDE.

Example (JVM/KDE):
```kotlin
// val state = getKdeThemeState()
val state = getKdeThemeState()
if (state == null) {
    println("Not on KDE")
} else {
    println("Window dark: ${state.windowTheme} | Panel dark: ${state.panelTheme}")
    if (state.isMixed) println("Mixed theme detected")
}

// In Compose UI (recommended):
@Composable
fun KdeInfo() {
    val s = rememberKdeDarkModeState()
    Text("KDE window dark: ${s?.windowTheme}")
    Text("KDE panel dark: ${s?.panelTheme}")
}
```

---

## 📦 Installation

This library is available on Maven Central. To include this module in your project, add the following dependency to your `build.gradle.kts`:

```kotlin
implementation("io.github.kdroidfilter:platformtools.darkmodedetector:<version>")
```

---

With `isSystemInDarkMode`, your Kotlin Multiplatform projects can now dynamically react to dark mode changes on all supported platforms. 🚀

---

For detailed changes in the latest version, see: [RELEASE_NOTES_0.6.0.md](RELEASE_NOTES_0.6.0.md)


## 🔄 RTL Windows Module (JVM only)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kdroidfilter/platformtools.rtlwindows?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.kdroidfilter/platformtools.rtlwindows)

The **RTL Windows** module fixes a specific bug in Compose Desktop applications running on Windows in RTL mode, where window buttons (minimize, maximize, close) are incorrectly positioned. This module simply resolves this issue and does nothing if the application is not in RTL mode. It's essential for applications that need to support right-to-left languages like Arabic, Hebrew, or Persian.

### 🔧 Function: `setWindowsRtlLayout`

The `setWindowsRtlLayout` function applies RTL mirroring to a Window based on its component orientation:

```kotlin
fun Window.setWindowsRtlLayout()
```

This extension function:
- Checks if the operating system is Windows (returns early if not)
- Determines if RTL layout is needed based on the window's component orientation
- Applies the appropriate Windows-specific style flags to enable RTL layout
- Updates the window to reflect the new style

### 🔍 How It Works

- Uses JNA (Java Native Access) to interact with the Windows API
- Checks if the application is in RTL mode; if not, it does nothing
- When in RTL mode, applies the `WS_EX_LAYOUTRTL` and `WS_EX_RTLREADING` extended window styles to fix the window buttons positioning
- Only affects Windows OS; has no effect on other operating systems

### 🔧 Example Usage

Here is an example of using `setWindowsRtlLayout` in a Jetpack Compose for Desktop application:

```kotlin
Window(
    title = "RTL Example",
    state = rememberWindowState(width = 800.dp, height = 600.dp),
    onCloseRequest = ::exitApplication,
) {
    // Apply RTL layout if needed
    window.setWindowsRtlLayout()

    App()
}
```

### 📦 Installation

This library is available on Maven Central. To include this module in your project, add the following dependency to your `build.gradle.kts`:

```kotlin
implementation("io.github.kdroidfilter:platformtools.rtlwindows:<version>")
```

---

## 🔧 AppManager Module

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kdroidfilter/platformtools.appmanager?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.kdroidfilter/platformtools.appmanager)

The **AppManager** module of the KMP Platform Tools library provides utilities to manage application-level information and configurations across multiple platforms. Below, we will detail the functionality and demonstrate usage of the provided features.

> **Note for Android Users**: For Android app installation functionality, we recommend using [Ackpine](https://github.com/solrudev/Ackpine), which provides a comprehensive solution for managing app installations on Android.

### 🔮 Overview

This module is designed to simplify retrieving and managing app-related metadata, such as package information and other common requirements. It supports platform-specific implementations while offering a unified API for shared code.

### 💡 Interface: `AppInstaller`  (Jvm only)

The `AppInstaller` interface provides platform-specific functionality for managing application installation and uninstallation. For each platform, the required file format is as follows:

- **Windows**: MSI file
- **Linux**: DEB file
- **Mac**: PKG file
#### Key Functions

##### ⬇️ `installApp`

Installs an application from the specified file. This function requires appropriate permissions, such as the ability to install apps from unknown sources.

```kotlin
suspend fun installApp(appFile: File, onResult: (success: Boolean, message: String?) -> Unit)
```

**Parameters:**

- `appFile`: The file to be installed (e.g., APK, MSI, PKG, or DEB).
- `onResult`: A callback with the installation result. Provides:
    - `success`: Indicates whether the installation succeeded.
    - `message`: An optional message with additional context (e.g., error details).

##### ⬆️ `uninstallApp`

Uninstalls an application using the provided package name or removes the current application. **Note: This functionality is not yet implemented.**

```kotlin
suspend fun uninstallApp(packageName: String, onResult: (success: Boolean, message: String?) -> Unit)
```

```kotlin
suspend fun uninstallApp(onResult: (success: Boolean, message: String?) -> Unit)
```

**Parameters:**

- `packageName` (optional): The package name of the app to uninstall.
- `onResult`: A callback with the uninstallation result. Provides:
    - `success`: Indicates whether the uninstallation succeeded.
    - `message`: An optional message with additional context.

### 🎫 Factory Function: `getAppInstaller`

Retrieves the platform-specific implementation of the `AppInstaller` interface.

```kotlin
fun getAppInstaller(): AppInstaller
```

**Example Usage:**

```kotlin
suspend fun performAppInstallation(appFile: File) {
    val appInstaller = getAppInstaller()
    appInstaller.installApp(appFile) { success, message ->
        if (success) {
            println("App installed successfully.")
        } else {
            println("Failed to install app: $message")
        }
    }
}

suspend fun performAppUninstallation(packageName: String) {
    val appInstaller = getAppInstaller()
    appInstaller.uninstallApp(packageName) { success, message ->
        if (success) {
            println("App uninstalled successfully.")
        } else {
            println("Failed to uninstall app: $message")
        }
    }
}
```

---

### 🔧 Windows-Specific Configuration

The `AppManager` module provides additional configuration options for managing Windows application installations:

#### 🔒 Configuring Administrator Privileges

You can configure whether the application installation requires administrator privileges by modifying the `requireAdmin` property:

```kotlin
WindowsInstallerConfig.requireAdmin = false // Default is true
```

#### 🌐 Enabling Per-User Installation for Silent Updates

For enabling silent updates on Windows, it is recommended to configure `perUserInstall` as `true` in your build settings. Below is an example configuration for a Compose Desktop application:

```kotlin
compose.desktop {
  application {
    mainClass = "com.kdroid.sample.MainKt"
    nativeDistributions {
      targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Pkg)
      windows {
        perUserInstall = true
      }
    }
  }
}
```

---


## 📃 Additional Functions (Jvm and Android Only)

The `AppManager` module also provides the following utility functions for JVM platforms:

### 🔁 `restartApplication`

Restarts the current application.

```kotlin
fun restartApplication()
```

### 🔄 `hasAppVersionChanged`

Checks if the application version has changed since the last time it was opened. This is useful for detecting updates and performing related actions.

```kotlin
fun hasAppVersionChanged(): Boolean
```

### 🏠 `isFirstInstallation`

Checks if it is the first installation of the app. This function can be used to perform setup actions or show onboarding screens.

```kotlin
fun isFirstInstallation(): Boolean
```

**Example Usage:**

```kotlin
  if (hasAppVersionChanged()) {
      println("The application has been updated.")
      // Perform actions required after an update
  } else {
      println("No updates detected.")
  }

  if (isFirstInstallation()) {
    println("This is the first installation of the app.")
  // Perform any necessary setup actions here
  } else {
    println("The app has been installed before.")
  }
```

This library is available on Maven Central. To include it in your project, add the following dependency to your `build.gradle.kts` :

```kotlin
implementation("io.github.kdroidfilter:platformtools.appmanager:<version>")
```

---

## 📥 Release Fetcher

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kdroidfilter/platformtools.releasefetcher?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.kdroidfilter/platformtools.releasefetcher)

### 🔧 Overview

The Release Fetcher module provides utilities for interacting with GitHub releases API to fetch release information for your application. It allows you to retrieve the latest release details from a GitHub repository, which can be used to implement update checking functionality in your application. The module is designed to work across all Kotlin Multiplatform targets and uses Ktor for HTTP requests.


### 🔧 `GitHubReleaseFetcher` Class

The `GitHubReleaseFetcher` class is a key utility in the **Release Fetcher** module that interacts with the GitHub API to fetch release information, check for updates, and retrieve platform-specific download links.

### 🔧 Main Functionality

- **Fetch Latest Release:**
    - The `getLatestRelease` method uses the GitHub API to fetch the latest release for a given repository.
    - Handles HTTP responses and parses the response body into a `Release` object.

- **Check for Updates:**
    - The `checkForUpdate` method compares the latest release version with the current application version.
    - The comparison is made using the release name (`tag_name`) of the latest GitHub release and the application's version.
    - If a newer version is available, it triggers a callback with the new version and changelog.

- **Platform-Specific Download Links:**
    - The `getDownloadLinkForPlatform` method provides the appropriate download link based on the current operating system.
    - Supports Android (`.apk`), Windows (`.msi`), Linux (`.deb`), and macOS (`.dmg`).

### 🔧 Important Note

- The GitHub repository must contain at least one release.
- The release name (`tag_name`) in the repository is critical as it is compared to the application's current version.
- Ensure the GitHub repository is configured correctly to include meaningful release names and assets.

---

### 🔧 Usage Example

Here is how you can use the `GitHubReleaseFetcher` class:

```kotlin
fun main() = runBlocking {
    val fetcher = GitHubReleaseFetcher(owner = "ownerName", repo = "repoName")

    // Fetch the latest release
    val latestRelease = fetcher.getLatestRelease()
    if (latestRelease != null) {
        println("Latest version: ${latestRelease.tag_name}")
        println("Changelog: ${latestRelease.body}")

        // Get platform-specific download link
        val downloadLink = fetcher.getDownloadLinkForPlatform(latestRelease)
        if (downloadLink != null) {
            println("Download here: $downloadLink")
        } else {
            println("No suitable download link for the current platform.")
        }
    } else {
        println("Failed to fetch the latest release.")
    }

    // Check for updates
    fetcher.checkForUpdate { latestVersion, changelog ->
        println("Update available: $latestVersion")
        println("Changelog: $changelog")
    }
}
```

This library is available on Maven Central. To include it in your project, add the following dependency to your `build.gradle.kts`:

```kotlin
implementation("io.github.kdroidfilter:platformtools.releasefetcher:<version>")
```

### ⚠️ Important Note About Dependencies

The Release Fetcher module uses Ktor as a compileOnly dependency. This means you need to include Ktor dependencies in your project if you want to use this module. Add the following dependencies to your project:

```kotlin
// Required Ktor dependencies for Release Fetcher
implementation("io.ktor:ktor-client-core:<ktor-version>")
implementation("io.ktor:ktor-client-content-negotiation:<ktor-version>")
implementation("io.ktor:ktor-client-serialization:<ktor-version>")
implementation("io.ktor:ktor-client-logging:<ktor-version>")
implementation("io.ktor:ktor-client-cio:<ktor-version>")
// For WebAssembly JS target
implementation("io.ktor:ktor-client-js:<ktor-version>")
```

---

## 🛒 License

PlatformTools is licensed under the [MIT License](https://opensource.org/licenses/MIT). Feel free to use, modify, and distribute the library under the terms of the license.

---

## 👥 Contributions

Contributions are welcome! If you want to improve this library, please feel free to submit a pull request or open an issue.

---

## 📣 Demo Application

A demo is available in the `sample` module, showcasing the main features of all the modules included in this library.
Additionally, a demo application with an integrated updater using this library is available [here](https://github.com/kdroidFilter/AppwithAutoUpdater).
