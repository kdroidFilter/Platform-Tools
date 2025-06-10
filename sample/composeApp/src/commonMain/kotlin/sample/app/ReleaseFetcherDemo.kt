package sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.releasefetcher.github.GitHubReleaseFetcher
import io.github.kdroidfilter.platformtools.releasefetcher.github.model.Release
import io.github.kdroidfilter.platformtools.releasefetcher.downloader.Downloader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseFetcherDemo() {
    val scope = rememberCoroutineScope()
    val fetcher = remember { GitHubReleaseFetcher("kdroidFilter", "KmpRealTimeLogger") }
    val downloader = remember { Downloader() }

    var release by remember { mutableStateOf<Release?>(null) }
    var progress by remember { mutableStateOf(0.0) }
    var downloadStatus by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Button(onClick = {
                scope.launch {
                    release = fetcher.getLatestRelease()
                }
            }) {
                Text("Fetch Latest Release")
            }
        }
        item {
            release?.let {
                Text("Latest Version: ${it.tag_name}", style = MaterialTheme.typography.bodyLarge)
                Text("Changelog: ${it.body}", style = MaterialTheme.typography.bodyLarge)

                Button(onClick = {
                    scope.launch {
                        val downloadLink = fetcher.getDownloadLinkForPlatform(it)
                        if (downloadLink != null) {
                            downloadStatus = "Downloading..."
                            downloader.download(downloadLink) { percentage, file ->
                                progress = percentage
                                if (file != null && percentage == 100.0) {
                                    downloadStatus = "Download complete: ${file.absolutePath}"
                                } else if (percentage == -1.0) {
                                    downloadStatus = "Download failed."
                                }
                            }
                        } else {
                            downloadStatus = "No suitable download link for platform."
                        }
                    }
                }) {
                    Text("Download Release")
                }

            }
        }
        item {
            Text("Progress: $progress %", style = MaterialTheme.typography.bodyLarge)
            Text(downloadStatus, style = MaterialTheme.typography.bodyLarge)
        }
    }

}