package sample.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.clipboardmanager.ClipboardContent
import io.github.kdroidfilter.platformtools.clipboardmanager.ClipboardListener
import io.github.kdroidfilter.platformtools.clipboardmanager.ClipboardMonitor
import io.github.kdroidfilter.platformtools.clipboardmanager.ClipboardMonitorFactory

@Composable
fun ClipboardDemo() {
    var clipboardContent by remember { mutableStateOf<ClipboardContent?>(null) }

    DisposableEffect(Unit) {
        val listener = object : ClipboardListener {
            override fun onClipboardChange(content: ClipboardContent) {
                clipboardContent = content
            }
        }
        val monitor: ClipboardMonitor = ClipboardMonitorFactory.create(listener)
        monitor.start()
        runCatching { clipboardContent = monitor.getCurrentContent() }.onFailure { /* ignore */ }
        onDispose {
            monitor.stop()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Presse-papiers", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors()) {
            Column(Modifier.padding(16.dp).fillMaxWidth()) {
                if (clipboardContent == null) {
                    Text("En attente des changements du presse-papiers…")
                } else {
                    ClipboardContentView(clipboardContent!!)
                }
            }
        }
    }
}

@Composable
private fun ClipboardContentView(content: ClipboardContent) {
    Column(Modifier.fillMaxWidth()) {
        InfoRow("Texte", content.text ?: "<aucun>")
        Spacer(Modifier.height(8.dp))
        InfoRow("HTML", content.html ?: "<aucun>")
        Spacer(Modifier.height(8.dp))
        InfoRow("RTF", content.rtf ?: "<aucun>")
        Spacer(Modifier.height(8.dp))
        InfoRow("Fichiers", content.files?.joinToString() ?: "<aucun>")
        Spacer(Modifier.height(8.dp))
        InfoRow("Image disponible", if (content.imageAvailable) "Oui" else "Non")
        Spacer(Modifier.height(8.dp))
        InfoRow("Horodatage", content.timestamp.toString())
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(2f))
    }
}
