package io.github.kdroidfilter.platformtools.clipboardmanager

import com.sun.jna.Platform
import java.time.LocalDateTime

fun main() {
    println("=== System Info ===")
    println("OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")}")
    println("Arch: ${System.getProperty("os.arch")}")
    println("Java: ${System.getProperty("java.version")}")

    if (Platform.isLinux()) {
        println("\n=== Linux Environment ===")
        println("DISPLAY: ${System.getenv("DISPLAY") ?: "unset"}")
        println("WAYLAND_DISPLAY: ${System.getenv("WAYLAND_DISPLAY") ?: "unset"}")
        println("XDG_SESSION_TYPE: ${System.getenv("XDG_SESSION_TYPE") ?: "unset"}")

        val tools = listOf("xclip", "xsel", "wl-paste")
        println("\nClipboard tools available:")
        tools.forEach { t ->
            val available = runCatching {
                ProcessBuilder("which", t).start().waitFor() == 0
            }.getOrDefault(false)
            println("  $t: ${if (available) "✓" else "✗"}")
        }
    }

    println("\n=== Starting monitor ===")

    val monitor = ClipboardMonitorFactory.create(object : ClipboardListener {
        override fun onClipboardChange(content: ClipboardContent) {
            println("\n🔔 Change at ${LocalDateTime.now()}")
            content.text?.let { txt ->
                val preview = if (txt.length > 100) "${txt.take(100)}... (${txt.length} chars)" else txt
                println("📝 Text: $preview")
            }
            content.html?.let { println("🌐 HTML: ${it.length} chars") }
            content.rtf?.let  { println("📄 RTF: ${it.length} chars") }
            content.files?.let { files ->
                println("📁 Files (${files.size}):"); files.forEach { println("   - $it") }
            }
            if (content.imageAvailable) println("🖼️ Image available")
            println("─".repeat(50))
        }
    })

    monitor.start()

    Runtime.getRuntime().addShutdownHook(Thread {
        println("\n⏹️ Stopping monitor...")
        monitor.stop()
        println("✅ Stopped")
    })

    println("\n✅ Monitoring is active!")
    println("\n📋 INSTRUCTIONS:")
    println("• Copy text, files or images")
    println("• Changes are detected automatically")
    println("• Press Ctrl+C to exit")

    println("\n📌 Initial clipboard content:")
    monitor.getCurrentContent().let { initial ->
        if (initial.text != null) {
            val t = initial.text
            println("Text: ${t.take(100)}${if (t.length > 100) "..." else ""}")
        } else {
            println("(empty)")
        }
    }

    println("\n" + "=".repeat(50) + "\n")
    Thread.sleep(Long.MAX_VALUE)
}
