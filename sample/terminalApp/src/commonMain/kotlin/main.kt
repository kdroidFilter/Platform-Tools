import io.github.kdroidfilter.platformtools.getPlatform

fun main() {
    println("The Operating System is " + getPlatform().name.lowercase().replaceFirstChar { it.uppercase()})
}