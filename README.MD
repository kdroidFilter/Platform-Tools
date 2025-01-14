# PlatformTools

**PlatformTools** is a Kotlin Multiplatform library designed to provide platform-specific utilities and tools for managing operating systems, application installation, and release fetching seamlessly across various platforms. The library is modular and divided into three main components: core, appmanager, and releasefetcher.

---

## 🌐 Core Module

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
implementation("io.github.kdroidfilter:platformtools.core:0.2.0")
```

## 🔧 AppManager Module (Jvm & Android only)

The **AppManager** module of the KMP Platform Tools library provides utilities to manage application-level information and configurations across multiple platforms. Below, we will detail the functionality and demonstrate usage of the provided features.

### 🔮 Overview

This module is designed to simplify retrieving and managing app-related metadata, such as package information and other common requirements. It supports platform-specific implementations while offering a unified API for shared code.

### 💡 Interface: `AppInstaller`

The `AppInstaller` interface provides platform-specific functionality for managing application installation and uninstallation. For each platform, the required file format is as follows:

- **Android**: APK file
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

Uninstalls an application using the provided package name or removes the current application. Currently, uninstallation functionality is only supported on Android.

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

### 🎮 Android-Specific Function: `installAppSilently`

The `AppManager` module provides an additional function for Android devices running in Device Owner mode:

#### 🔄 `installAppSilently`

Silently installs an application from the specified file without requiring user interaction. This is particularly useful for enterprise environments or Device Owner mode.

```kotlin
suspend fun installAppSilently(
    appFile: File,
    onResult: (success: Boolean, message: String?) -> Unit
)
```

## 📃 Additional Functions (Jvm & Android)

The `AppManager` module also provides the following utility functions for JVM and Android platforms:

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

This library is available on Maven Central. To include it in your project, add the following dependency to your `build.gradle.kts` or `build.gradle` file:

```kotlin
implementation("io.github.kdroidfilter:platformtools.appmanager:0.2.0")
```

---

## 📥 Release Fetcher

### 🔧 Overview

This module simplifies application downloads for the target platform and streamlines new release management.

### 📦 `Downloader` Class

The `Downloader` class is a core component of the **Release Fetcher** module, designed to download application files from a given URL while tracking the download progress.

### Main Functionality
- **File Downloading:**
    - The primary method, `downloadApp`, accepts a URL and downloads the associated file.
    - It provides real-time updates on the download progress through a callback `onProgress`, reporting a percentage (from 0.0 to 100.0) and the local file when available.
    - In case of an error, the progress percentage is set to `-1.0`.

### 🔧 Technical Highlights
- **Error Handling:** Comprehensive handling of network or file errors with detailed error logging.
- **Optimized Downloading:** Uses a configurable buffer for efficient file writing.
- **Coroutine Compatibility:** The method is `suspend`, making it ideal for use with Kotlin Coroutines.

---

### 🔧 Usage Example

Here is how you can use the `Downloader` class:

```kotlin
fun main() = runBlocking {
    val downloader = Downloader()
    val url = "https://example.com/app-release.apk"

    val success = downloader.downloadApp(url) { progress, file ->
        when {
            progress == -1.0 -> println("Error during download.")
            progress == 100.0 -> println("Download complete: ${file?.absolutePath}")
            else -> println("Progress: ${"%.2f".format(progress)}%")
        }
    }

    if (success) {
        println("Download succeeded!")
    } else {
        println("Download failed.")
    }
}
```

### ⚖️ Configuration

The `ReleaseFetcherConfig` object allows modification of configuration settings for the downloader:

- **Default Configuration:**
    - `downloaderBufferSize`: `2 * 1024 * 1024` (2 MB)
    - `clientTimeOut`: `HttpTimeoutConfig.INFINITE_TIMEOUT_MS`

- **Example:** Modifying the default configuration:

```kotlin
ReleaseFetcherConfig.downloaderBufferSize = 4 * 1024 * 1024 // Set buffer size to 4 MB
ReleaseFetcherConfig.clientTimeOut = 60_000 // Set client timeout to 60 seconds
```

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

This library is available on Maven Central. To include it in your project, add the following dependency to your `build.gradle.kts` or `build.gradle` file:

```kotlin
implementation("io.github.kdroidfilter:platformtools.releasefetcher:0.2.0")
```

---

## ⚖️ Permission Handler Module

The **Permission Handler** module provides a simple way to check and request permissions on Android. It simplifies the process, requiring only that you declare the necessary permissions in the Android manifest file.

### 🔧 Main Features

- **Permission Checking:**
    - Functions like `hasInstallPermission()` allow you to verify if a specific permission is granted.

- **Permission Requesting:**
    - Functions like `requestInstallPermission(onGranted, onDenied)` allow you to request permissions, with callbacks for granted or denied states.

- **Manifest Requirements:**
    - Each function's documentation specifies the permissions required in the manifest file.

### 🔧 Available Permissions and Functions

1. **Install Permission:**
- **Check:** `hasInstallPermission()`
- **Request:** `requestInstallPermission(onGranted, onDenied)`
- **Manifest Permission:**
  ```xml
  <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
  ```

2. **Notification Permission:**
- **Check:** `hasNotificationPermission()`
- **Request:** `requestNotificationPermission(onGranted, onDenied)`
- **Manifest Permission:**
  ```xml
  <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
  ```

3. **Location Permissions:**
- **Check:** `hasLocationPermission()`
- **Request:** `requestLocationPermission(onGranted, onDenied)`
- **Manifest Permissions:**
  ```xml
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  ```

4. **Background Location Permission:**
- **Check:** `hasBackgroundLocationPermission()`
- **Request:** `requestBackgroundLocationPermission(onGranted, onDenied)` (requires location permissions to be granted first)
- **Manifest Permission:**
  ```xml
  <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
  ```

5. **Bluetooth Permissions:**
- **Check:** `hasBluetoothPermission()`
- **Request:** `requestBluetoothPermission(onGranted, onDenied)`
- **Manifest Permissions:**
  ```xml
  <uses-permission android:name="android.permission.BLUETOOTH" />
  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
  <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" android:required="false" />
  ```

6. **Camera Permission:**
- **Check:** `hasCameraPermission()`
- **Request:** `requestCameraPermission(onGranted, onDenied)`
- **Manifest Requirements:**
  ```xml
  <uses-feature android:name="android.hardware.camera" android:required="false" />
  <uses-permission android:name="android.permission.CAMERA" />
  ```

7. **Audio Recording Permission:**
- **Check:** `hasRecordAudioPermission()`
- **Request:** `requestRecordAudioPermission(onGranted, onDenied)`
- **Manifest Permission:**
  ```xml
  <uses-permission android:name="android.permission.RECORD_AUDIO" />
  ```

8. **Overlay Permission:**
- **Check:** `hasOverlayPermission()`
- **Request:** `requestOverlayPermission(onGranted, onDenied)`
- **Manifest Permission:**
  ```xml
  <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
  ```

9. **Contacts Permissions:**
- **Read Contacts:**
    - **Check:** `hasReadContactsPermission()`
    - **Request:** `requestReadContactsPermission(onGranted, onDenied)`
    - **Manifest Permission:**
      ```xml
      <uses-permission android:name="android.permission.READ_CONTACTS" />
      ```
- **Write Contacts:**
    - **Check:** `hasWriteContactsPermission()`
    - **Request:** `requestWriteContactsPermission(onGranted, onDenied)`
    - **Manifest Permission:**
      ```xml
      <uses-permission android:name="android.permission.WRITE_CONTACTS" />
      ```

10. **Read External Storage Permission (Android 13 and Above):**
- **Check:** `hasReadExternalStoragePermission(mediaTypes: Set<MediaType> = emptySet())`
- **Request:** `requestReadExternalStoragePermission(mediaTypes: Set<MediaType> = emptySet(), onGranted, onDenied)`
- **MediaType Enum:**
  ```kotlin
  enum class MediaType {
      IMAGES,
      VIDEO,
      AUDIO
  }
  ```
- **Manifest Permission:**
  ```xml
  <!-- Example permissions depending on media types -->
  <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
  <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
  <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
  ```

### 🔧 Usage Example

Here is how to use the module for requesting and handling permissions:

```kotlin
fun checkAndRequestInstallPermission() {
    if (hasInstallPermission()) {
        println("Install permission already granted.")
    } else {
        requestInstallPermission(
            onGranted = {
                println("Install permission granted.")
            },
            onDenied = {
                println("Install permission denied.")
            }
        )
    }
}

```

### 🔹 Special Notes for External Storage Permissions

- **Requests read external storage permission for the application.**
- **Behavior Based on Android Version:**
    - For Android versions below 13, it requests `READ_EXTERNAL_STORAGE`.
      ```xml
      <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
      ```
    - For Android 13 and above, it requests specific media permissions based on the provided media types.
      ```xml
      <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
      <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
      <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
      ```

### 🔧 Usage Example

Here is how you can use the module for requesting and handling External storage permissions:

```kotlin
fun checkAndRequestReadExternalStoragePermission() {
    val mediaTypes = setOf(MediaType.IMAGES, MediaType.VIDEO)
    if (hasReadExternalStoragePermission(mediaTypes)) {
        println("Read external storage permission already granted for selected media types.")
    } else {
        requestReadExternalStoragePermission(mediaTypes,
            onGranted = {
                println("Read external storage permission granted for selected media types.")
            },
            onDenied = {
                println("Read external storage permission denied.")
            }
        )
    }
}
```

### 🔧 Key Points

- Minimal setup: Add the required permissions in the manifest file.
- Comprehensive: Handles a wide range of Android permissions.
- Developer-friendly: Simple APIs with clear callbacks for permission handling.

This library is available on Maven Central. To include it in your project, add the following dependency to your `build.gradle.kts` or `build.gradle` file:

```kotlin
implementation("io.github.kdroidfilter:platformtools.permissionhandler:0.2.0")
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


