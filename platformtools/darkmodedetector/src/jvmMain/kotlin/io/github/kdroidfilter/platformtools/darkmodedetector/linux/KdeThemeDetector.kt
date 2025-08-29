package io.github.kdroidfilter.platformtools.darkmodedetector.linux

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.kdroidfilter.platformtools.LinuxDesktopEnvironment
import io.github.kdroidfilter.platformtools.detectLinuxDesktopEnvironment
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.math.sqrt

/* ----------------------------- Helpers (KDE) ------------------------------ */

private fun runKReadConfig(file: String, group: String, key: String): String? {
    val cmds = listOf(
        arrayOf("kreadconfig6", "--file", file, "--group", group, "--key", key),
        arrayOf("kreadconfig5", "--file", file, "--group", group, "--key", key)
    )
    for (cmd in cmds) {
        try {
            val p = Runtime.getRuntime().exec(cmd)
            val out = BufferedReader(InputStreamReader(p.inputStream)).use { it.readLine()?.trim() }
            p.waitFor()
            if (!out.isNullOrBlank()) return out
        } catch (_: Exception) {}
    }
    return null
}

private fun effectivePlasmaThemeName(rawTheme: String?, lookAndFeel: String?): String? {
    if (!lookAndFeel.isNullOrBlank() && lookAndFeel.contains("breeze", true) && lookAndFeel.contains("twilight", true)) {
        return "breezedark"
    }
    if (rawTheme.isNullOrBlank() || rawTheme.equals("default", true)) return "breeze"
    return when (rawTheme.lowercase()) {
        "org.kde.breezedark.desktop", "breeze-dark", "breezedark" -> "breezedark"
        "org.kde.breeze.desktop", "breeze" -> "breeze"
        else -> rawTheme
    }
}

private fun findExistingFile(vararg candidates: String): File? =
    candidates.asSequence().map(::File).firstOrNull { it.isFile && it.canRead() }

private fun plasmaThemeColorsFile(themeName: String): File? {
    val home = System.getenv("HOME") ?: System.getProperty("user.home")
    val variants = listOf(
        themeName,
        themeName.replace(' ', '_'),
        themeName.replace(' ', '-'),
        themeName.lowercase(),
        themeName.lowercase().replace(' ', '_'),
        themeName.lowercase().replace(' ', '-')
    ).distinct()

    val candidates = buildList {
        for (v in variants) {
            add("$home/.local/share/plasma/desktoptheme/$v/colors")
            add("/usr/share/plasma/desktoptheme/$v/colors")
        }
    }
    return findExistingFile(*candidates.toTypedArray())
}

private fun kdeColorSchemeFile(schemeName: String): File? {
    val home = System.getenv("HOME") ?: System.getProperty("user.home")
    val variants = listOf(
        schemeName,
        schemeName.replace(' ', '_'),
        schemeName.replace(' ', '-'),
        schemeName.lowercase(),
        schemeName.lowercase().replace(' ', '_'),
        schemeName.lowercase().replace(' ', '-')
    ).distinct()

    val candidates = buildList {
        for (v in variants) {
            add("$home/.local/share/color-schemes/$v.colors")
            add("/usr/share/color-schemes/$v.colors")
        }
    }
    return findExistingFile(*candidates.toTypedArray())
}

private fun parseIni(file: File): Map<String, Map<String, String>> {
    val result = mutableMapOf<String, MutableMap<String, String>>()
    var section = ""
    file.forEachLine { raw ->
        val line = raw.trim()
        if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) return@forEachLine
        if (line.startsWith("[") && line.endsWith("]")) {
            section = line.substring(1, line.length - 1)
            result.putIfAbsent(section, mutableMapOf())
        } else {
            val idx = line.indexOf('=')
            if (idx > 0) {
                val k = line.substring(0, idx).trim()
                val v = line.substring(idx + 1).trim()
                result.getOrPut(section) { mutableMapOf() }[k] = v
            }
        }
    }
    return result
}

private fun parseKdeRgb(value: String): Triple<Int, Int, Int>? {
    val nums = value.split(',', ' ')
        .mapNotNull { it.trim().toIntOrNull() }
        .filterIndexed { idx, _ -> idx < 3 }
    if (nums.size == 3) return Triple(nums[0].coerceIn(0, 255), nums[1].coerceIn(0, 255), nums[2].coerceIn(0, 255))
    return null
}

private fun isRgbDark(r: Int, g: Int, b: Int): Boolean {
    val p = kotlin.math.sqrt(0.299 * r * r + 0.587 * g * g + 0.114 * b * b)
    return p < 140.0
}

private fun deriveDarkFromColorsMap(colors: Map<String, Map<String, String>>): Boolean? {
    val sectionsInPriority = listOf(
        "Colors:Panel",
        "Colors:Window",
        "Colors:View",
        "Colors:Button"
    )
    for (sec in sectionsInPriority) {
        val bg = colors[sec]?.get("BackgroundNormal") ?: continue
        val rgb = parseKdeRgb(bg) ?: continue
        return isRgbDark(rgb.first, rgb.second, rgb.third)
    }
    colors.values.forEach { section ->
        section["BackgroundNormal"]?.let { v ->
            parseKdeRgb(v)?.let { (r, g, b) -> return isRgbDark(r, g, b) }
        }
    }
    return null
}

/* ------------------------------- KDE logic -------------------------------- */

internal fun detectKdeDarkTheme(): Boolean? {
    return try {
        val scheme = runKReadConfig(file = "kdeglobals", group = "General", key = "ColorScheme") ?: return null
        val schemeFile = kdeColorSchemeFile(scheme) ?: return null
        val colors = parseIni(schemeFile)
        deriveDarkFromColorsMap(colors)
    } catch (_: Exception) {
        try {
            val laf = runKReadConfig(file = "kdeglobals", group = "KDE", key = "LookAndFeelPackage")
            laf?.contains("dark", ignoreCase = true)
        } catch (_: Exception) {
            null
        }
    }
}

fun isKdePanelDark(): Boolean? {
    if (detectLinuxDesktopEnvironment() != LinuxDesktopEnvironment.KDE) return null
    return try {
        val rawTheme = runKReadConfig("plasmarc", "Theme", "name")
            ?: runKReadConfig("plasmashellrc", "Theme", "name")
        val lookAndFeel = runKReadConfig("kdeglobals", "KDE", "LookAndFeelPackage")
        val theme = effectivePlasmaThemeName(rawTheme, lookAndFeel) ?: return null
        val colorsFile: File? = when {
            plasmaThemeColorsFile(theme) != null -> plasmaThemeColorsFile(theme)
            theme.equals("breeze", true) -> plasmaThemeColorsFile("breezedark")
            theme.equals("breezedark", true) -> plasmaThemeColorsFile("breeze")
            else -> null
        }
        if (colorsFile != null) {
            val colors = parseIni(colorsFile)
            val derived = deriveDarkFromColorsMap(colors)
            if (derived != null) return derived
        }
        if (!lookAndFeel.isNullOrBlank() && lookAndFeel.contains("breeze", true) && lookAndFeel.contains("twilight", true)) {
            return true
        }
        val scheme = runKReadConfig("kdeglobals", "General", "ColorScheme")
        when {
            scheme?.contains("dark", true) == true -> true
            scheme?.contains("light", true) == true -> false
            lookAndFeel?.contains("dark", true) == true -> true
            lookAndFeel?.contains("light", true) == true -> false
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

data class KdeThemeState(
    val windowTheme: Boolean? = null,
    val panelTheme: Boolean? = null
) {
    val isMixed: Boolean
        get() = windowTheme != null && panelTheme != null && windowTheme != panelTheme
    val isFullDark: Boolean
        get() = windowTheme == true && panelTheme == true
    val isFullLight: Boolean
        get() = windowTheme == false && panelTheme == false
}

@Volatile
private var lastKdeThemeState: KdeThemeState? = null

@Volatile
private var kdeMonitorThread: Thread? = null

private fun ensureKdeMonitoring() {
    if (kdeMonitorThread?.isAlive == true) return
    kdeMonitorThread = Thread({
        // initialize
        lastKdeThemeState = KdeThemeState(
            windowTheme = detectKdeDarkTheme(),
            panelTheme = isKdePanelDark()
        )
        val process = try {
            Runtime.getRuntime().exec(
                arrayOf(
                    "dbus-monitor",
                    "--session",
                    "type='signal',interface='org.kde.KGlobalSettings',member='notifyChange'",
                    "type='signal',interface='org.kde.PlasmaShell',member='themeChanged'",
                    "type='signal',interface='org.kde.kdeglobals',member='configChanged'"
                )
            )
        } catch (_: Exception) { null }
        if (process == null) return@Thread
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            while (!Thread.currentThread().isInterrupted) {
                val line = reader.readLine() ?: break
                if (
                    line.contains("notifyChange") ||
                    line.contains("themeChanged") ||
                    line.contains("configChanged")
                ) {
                    try { Thread.sleep(100) } catch (_: InterruptedException) { break }
                    val newState = KdeThemeState(
                        windowTheme = detectKdeDarkTheme(),
                        panelTheme = isKdePanelDark()
                    )
                    lastKdeThemeState = newState
                }
            }
        }
        if (process.isAlive) {
            try { process.destroy() } catch (_: Throwable) {}
        }
    }, "KDE-Theme-State-Monitor").apply { isDaemon = true; start() }
}

fun getKdeThemeState(): KdeThemeState? {
    if (detectLinuxDesktopEnvironment() != LinuxDesktopEnvironment.KDE) return null
    if (lastKdeThemeState == null) {
        // compute once
        lastKdeThemeState = KdeThemeState(
            windowTheme = detectKdeDarkTheme(),
            panelTheme = isKdePanelDark()
        )
        // try to start background monitoring so subsequent calls are reactive
        ensureKdeMonitoring()
    }
    return lastKdeThemeState
}

fun monitorKdeThemeChanges(onThemeChange: (KdeThemeState) -> Unit): Process? {
    if (detectLinuxDesktopEnvironment() != LinuxDesktopEnvironment.KDE) return null
    return try {
        val process = Runtime.getRuntime().exec(
            arrayOf(
                "dbus-monitor",
                "--session",
                "type='signal',interface='org.kde.KGlobalSettings',member='notifyChange'",
                "type='signal',interface='org.kde.PlasmaShell',member='themeChanged'",
                "type='signal',interface='org.kde.kdeglobals',member='configChanged'"
            )
        )
        Thread({
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var lastState = getKdeThemeState()
                reader.forEachLine { line ->
                    if (
                        line.contains("notifyChange") ||
                        line.contains("themeChanged") ||
                        line.contains("configChanged")
                    ) {
                        Thread.sleep(100)
                        val newState = getKdeThemeState()
                        if (newState != null && newState != lastState) {
                            lastState = newState
                            onThemeChange(newState)
                        }
                    }
                }
            }
        }, "KDE-Theme-DBus-Monitor").apply { isDaemon = true; start() }
        process
    } catch (_: Exception) {
        null
    }
}

@Composable
fun rememberKdeDarkModeState(): KdeThemeState? {
    val state = remember { mutableStateOf(getKdeThemeState()) }
    DisposableEffect(Unit) {
        val process = monitorKdeThemeChanges { newState ->
            state.value = newState
        }
        onDispose {
            process?.destroy()
        }
    }
    return state.value
}

@Composable
internal fun isKdeInDarkMode(): Boolean {
    val state = rememberKdeDarkModeState()
    return state?.windowTheme ?: state?.panelTheme ?: false
}
