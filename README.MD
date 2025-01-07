# 🌐 PlatformTools

**PlatformTools** is a Kotlin Multiplatform library designed to provide platform-specific utilities and tools for managing operating systems, cache directories, and application versioning seamlessly across various platforms.

---

## ✨ Features

- **Operating System Detection**
  Easily detect the current operating system (e.g., Windows, Mac, Linux, Android, iOS, JS, WASM).

- **Cache Directory Access**
  Retrieve the directory used for storing cached data in a platform-independent way.

- **Application Versioning**
  Retrieve the application version for consistent version management across platforms.

---

## 📦 Installation

Add the following dependency to your Kotlin Multiplatform project:

```kotlin
implementation("io.github.kdroidfilter:platformtools:<latest_version>")
```

Replace `<latest_version>` with the latest version available on Maven Central.

---

## 🌟 Purpose

The main goal of this library is to serve as a versatile toolkit for implementing platform-specific utilities. While this approach might seem contrary to the `expect/actual` pattern of Kotlin Multiplatform, **PlatformTools** fills a specific gap:

- It enables operating system detection within the JVM, which is not natively supported by KMP.
- It allows OS detection in common code for minor functionalities where embedding such logic can be beneficial.

Currently, there is no precise long-term objective for this library beyond its existing functionality. It was initially developed to facilitate communication between Android and JVM platforms. However, there is potential for expanding to a fully-fledged Kotlin Multiplatform implementation as needs arise.

The main goal of this library is to serve as a versatile toolkit for implementing platform-specific utilities. While this approach might seem contrary to the `expect/actual` pattern of Kotlin Multiplatform, **PlatformTools** fills a specific gap:

- It enables operating system detection within the JVM, which is not natively supported by KMP.
- It allows OS detection in common code for minor functionalities where embedding such logic can be beneficial.

This makes **PlatformTools** a practical choice when small but essential platform-specific implementations are needed.

---

## 📚 Usage

### Detect Operating System

```kotlin
import io.github.kdroidfilter.platformtools.OperatingSystem
import io.github.kdroidfilter.platformtools.getOperatingSystem

val os = getOperatingSystem()
println("Current operating system: \$os")
```

### Get Cache Directory (Available for JVM and Android only)

```kotlin
import io.github.kdroidfilter.platformtools.getCacheDir

val cacheDir = getCacheDir()
println("Cache directory: \$cacheDir")
```

### Get Application Version (Available for JVM and Android only)

```kotlin
import io.github.kdroidfilter.platformtools.getAppVersion

val appVersion = getAppVersion()
println("Application version: \$appVersion")
```

---

## 📄 License

PlatformTools is licensed under the [MIT License](https://opensource.org/licenses/MIT). Feel free to use, modify, and distribute the library under the terms of the license.

---

## 🤝 Contributions

Contributions are welcome! If you want to improve this library, please feel free to submit a pull request or open an issue on [GitHub](https://github.com/kdroidFilter/platformtools).

---
