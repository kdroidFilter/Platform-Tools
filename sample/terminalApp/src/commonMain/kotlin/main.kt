import io.github.kdroidfilter.platformtools.getOperatingSystem

fun main() {
    println("The Operating System is " + getOperatingSystem().name.lowercase().replaceFirstChar { it.uppercase()})
}