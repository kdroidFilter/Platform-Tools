package io.github.kdroidfilter.platformtools

/** Most common Linux desktop environments. */
enum class LinuxDesktopEnvironment {
    GNOME, KDE, XFCE, CINNAMON, MATE, UNKNOWN
}

/**
 * Detects the Linux Desktop Environment.
 *
 * - Returns **null** if the OS is not Linux († avoids unnecessary computation).
 * - Uses common environment variables:
 *   `XDG_CURRENT_DESKTOP`, `DESKTOP_SESSION`.
 */
fun detectLinuxDesktopEnvironment(): LinuxDesktopEnvironment? {
    if (getOperatingSystem() != OperatingSystem.LINUX) return null

    val combinedEnv = buildList {
        System.getenv("XDG_CURRENT_DESKTOP")?.let(::add)
        System.getenv("DESKTOP_SESSION")?.let(::add)
    }.joinToString("|").lowercase()

    return when {
        "gnome"    in combinedEnv               -> LinuxDesktopEnvironment.GNOME
        "kde" in combinedEnv || "plasma" in combinedEnv -> LinuxDesktopEnvironment.KDE
        "xfce"     in combinedEnv               -> LinuxDesktopEnvironment.XFCE
        "cinnamon" in combinedEnv               -> LinuxDesktopEnvironment.CINNAMON
        "mate"     in combinedEnv               -> LinuxDesktopEnvironment.MATE
        else                                    -> LinuxDesktopEnvironment.UNKNOWN
    }
}
