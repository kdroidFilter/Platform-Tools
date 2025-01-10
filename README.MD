## PlatformTools

**PlatformTools** is a Kotlin Multiplatform library designed to provide platform-specific utilities and tools for managing operating systems, application installation, and release fetching seamlessly across various platforms. The library is modular and divided into three main components: core, appmanager, and releasefetcher.

---

## ✨ Features

### Core

- **Platform Detection**\
  Identify the current platform or operating system, such as Windows, Mac, Linux, Android, iOS, JS, or WASM.

### AppManager (JVM and Android only)

- **App Installation Management**\
  Check and request permissions for installing applications, install and uninstall apps, and manage application packages seamlessly.

### ReleaseFetcher

- **Release Management**\
  Fetch the latest release information from GitHub repositories, check for updates, and download release files tailored to the platform.
- **File Downloader**\
  Easily download application files with progress tracking through the Downloader class.
- **Update Checker**\
  Includes a function to check for updates (`checkForUpdate`). This function determines whether a newer version of the application is available and provides the new version details and changelog if an update is needed.
- **Platform-specific Download Link Retrieval**\
  Contains a function (`getDownloadLinkForPlatform`) to retrieve the download link for a release asset suitable for the current platform (e.g., APK for Android, MSI for Windows).

---

## 📦 Installation

Add the following dependencies to your Kotlin Multiplatform project:

### Core

```kotlin
implementation("io.github.kdroidfilter:platformtools.core:0.1.4")
```

### AppManager

```kotlin
implementation("io.github.kdroidfilter:platformtools.appmanager:0.1.4")
```

### ReleaseFetcher

```kotlin
implementation("io.github.kdroidfilter:platformtools.releasefetcher:0.1.4")
```

---

## 🌟 Modules

### Core

The main goal of this library is to serve as a versatile toolkit for implementing platform-specific utilities. While this approach might seem contrary to the expect/actual pattern of Kotlin Multiplatform, PlatformTools fills a specific gap:

- It enables operating system detection for multiplatform environments such as JVM, WASM, or JS, where direct OS detection is not always straightforward.
- It allows OS detection in common code for minor functionalities where embedding such logic can be beneficial.

The core module provides tools for platform detection, including support for enums to represent different platforms:

```kotlin
import io.github.kdroidfilter.platformtools.Platform
import io.github.kdroidfilter.platformtools.getOperatingSystem

val operatingSystem = getOperatingSystem()
println("Current operating system: $operatingSystem")
```

### AppManager (JVM and Android only)

The appmanager module provides an interface for managing application installation and uninstallation. For each platform, the required file format is as follows:

- **Android**: APK file
- **Windows**: MSI file
- **Linux**: DEB file
- **Mac**: PKG file

```kotlin
import io.github.kdroidfilter.platformtools.appmanager.getAppInstaller
import java.io.File

val appInstaller = getAppInstaller()

// Check if the app can request permission to install packages
val canRequest = appInstaller.canRequestInstallPackages()
println("Can request install packages permission: $canRequest")

// Request permission to install packages
appInstaller.requestInstallPackagesPermission()

// Install an application
val appFile = File("/path/to/app.apk")
appInstaller.installApp(appFile) { success, message ->
    if (success) {
        println("Installation successful")
    } else {
        println("Installation failed: $message")
    }
}

// Uninstall an app by package name (Android only)
appInstaller.uninstallApp("com.example.app") { success, message ->
    if (success) {
        println("Uninstallation started: $message")
    } else {
        println("Failed to uninstall: $message")
    }
}

// Uninstall the current app (Android only)
appInstaller.uninstallApp { success, message ->
    if (success) {
        println("Uninstallation started: $message")
    } else {
        println("Failed to uninstall: $message")
    }
}
```

On **Windows**, you can configure the installation of an app by setting `WindowsConfig.requireAdmin` to `true` or `false` depending on your needs. By default, this value is `true`. For example:

```kotlin
WindowsConfig.requireAdmin = false
```

### ReleaseFetcher

The releasefetcher module simplifies release management with GitHub:

```kotlin
import io.github.kdroidfilter.platformtools.releasefetcher.github.GitHubReleaseFetcher

val releaseFetcher = GitHubReleaseFetcher(owner = "owner", repo = "repo")

// Check for updates
releaseFetcher.checkForUpdate { latestVersion, changelog ->
    println("New version available: $latestVersion")
    println("Changelog: $changelog")
}

// Fetch download link for the current platform
val release = releaseFetcher.getLatestRelease()
val downloadLink = releaseFetcher.getDownloadLinkForPlatform(release!!)
println("Download link: $downloadLink")
```

#### Downloader

The Downloader class provides functionality for downloading application files with progress tracking:

```kotlin
import io.github.kdroidfilter.platformtools.releasefetcher.Downloader

val downloader = Downloader()

suspend fun downloadAppFile(downloadUrl: String) {
    downloader.downloadApp(downloadUrl) { percentage, file ->
        if (percentage >= 0) {
            println("Download progress: $percentage%")
        } else {
            println("Download failed")
        }

        file?.let {
            println("Downloaded file: ${it.absolutePath}")
        }
    }
}
```

---

## 🛌 Additional Advice

For Compose Desktop configuration under Windows, you can structure your settings as follows to optimize installation and ensure smooth operation:

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

Setting `perUserInstall = true` avoids requiring admin rights during installation and enables background updates. **Note:** This feature is not feasible on Linux or Mac.

---

## 🛍️ License

PlatformTools is licensed under the [MIT License](https://opensource.org/licenses/MIT). Feel free to use, modify, and distribute the library under the terms of the license.

---

## 🤝 Contributions

Contributions are welcome! If you want to improve this library, please feel free to submit a pull request or open an issue.

---

## 📢 Demo Application

A demo application with an integrated updater using this library is available [here](https://github.com/kdroidFilter/AppwithAutoUpdater).

