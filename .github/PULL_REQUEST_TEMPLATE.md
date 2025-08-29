## 🚀 PR: Release 0.6.0

### Summary
This PR prepares and documents the 0.6.0 release of PlatformTools with major improvements to Linux dark mode detection, especially for KDE.

### Highlights
- Linux Dark Mode (JVM/Compose):
  - KDE: Real-time reactivity via DBus monitoring; new isKdeInDarkMode() for API homogeneity.
  - KDE: getKdeThemeState() is now auto‑reactive with background monitoring and cached state.
  - KDE: rememberKdeDarkModeState() is public for Compose UIs.
  - GNOME: Reactive detection remains via background monitor.
  - isLinuxInDarkMode() now routes to KDE/GNOME reactive paths; other DEs use best-effort detection.
- README updated to document the API changes and usage examples.
- Sample updated to use rememberKdeDarkModeState() directly (no manual DisposableEffect needed).

### Breaking Changes
- None. All changes are additive and backwards compatible. Existing APIs continue to work. KDE behavior is improved to be reactive.

### Motivation and Context
Provides parity between GNOME and KDE for real-time dark mode detection and simplifies Compose usage by exposing a ready-to-use reactive helper.

### Testing
- Manual verification on Linux KDE and GNOME where available.
- Code compiles across modules; no public API removals.

### Checklist
- [x] README updated
- [x] Version bumped to 0.6.0 (tag-driven via CI; see below)
- [x] Sample updated

### Release/Publishing Notes
This repository uses tag-driven versioning via GITHUB_REF (see root build.gradle.kts). To publish 0.6.0 to Maven Central:
1. Merge this PR into main.
2. Create a Git tag `v0.6.0` on main and push it.
3. CI will set libVersion to 0.6.0 based on the tag (refs/tags/v0.6.0) and publish.

### Related
- Addresses: Real-time KDE theme reactivity and API homogenization requests.
