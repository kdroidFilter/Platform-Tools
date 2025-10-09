# Repository Guidelines

Use this guide to align contributions with PlatformTools’ Kotlin Multiplatform setup before opening a pull request.

## Project Structure & Module Organization
- `platformtools/core`, `appmanager`, `releasefetcher`, `darkmodedetector`, and `rtlwindows` follow the KMP layout with `src/commonMain`, platform-specific `src/<target>Main`, and optional `src/commonTest`. Keep shared logic in `commonMain` and add `expect/actual` pairs only when necessary.
- `sample/composeApp` hosts the Compose multiplatform demo (Android + Desktop), while `sample/terminalApp` builds native CLIs that showcase the core APIs.
- `kotlin-js-store/wasm` stores generated JS/WASM artifacts required by the sample apps; do not hand-edit its contents.
- Treat `build/` directories as disposable Gradle outputs.

## Build, Test, and Development Commands
- `./gradlew build` compiles all modules and runs the default verification suite.
- `./gradlew check` executes platform-specific tests (e.g., `:platformtools:releasefetcher:allTests`) and Dokka validation.
- `./gradlew :sample:composeApp:run` launches the desktop demo; use `installDebug` for Android when a device/emulator is present.
- `./gradlew dokkaHtmlMultiModule` regenerates API docs for Maven publishing previews.
- `./gradlew publishToMavenLocal` publishes every library module with the version inferred from `GITHUB_REF`.

## Coding Style & Naming Conventions
- Kotlin 2.1 defaults: 4-space indentation, trailing commas where idiomatic, and explicit visibility (`internal` within modules).
- Namespaces mirror directory structure (`io.github.kdroidfilter.platformtools.<module>`); files with multiple expect/actual declarations should group related APIs.
- Use UpperCamelCase for classes/object singletons, lowerCamelCase for functions and properties, and `snake_case` only for native interop identifiers.

## Testing Guidelines
- Place new tests under the matching `src/commonTest/kotlin` package; add target-specific suites (e.g., `jvmTest`) when behaviour diverges.
- Use `kotlin.test` assertions and descriptive method names such as `fun isSystemInDarkMode_returnsTrue_onWindowsInDarkTheme()`.
- When adding public APIs, cover at least the happy path and one failure path per supported platform. Update demos to exercise new primitives if UI confirmation is required.

## Commit & Pull Request Guidelines
- Follow the existing history: short, imperative commit subjects (`Add Windows title bar helper`, `Fix release fetcher cache`). Reference affected module(s) when helpful.
- Each PR should include: a concise summary, linked issue (if any), testing notes (`./gradlew check` output or emulator/device evidence), and screenshots when UI changes affect the samples.
- Keep PRs atomic—version bumps, feature work, and doc updates should land separately to simplify Maven publishing.
