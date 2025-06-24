package sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.releasefetcher.github.GitHubReleaseFetcher
import io.github.kdroidfilter.platformtools.releasefetcher.github.model.Release
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseFetcherDemo() {
    val scope = rememberCoroutineScope()
    val fetcher = remember { GitHubReleaseFetcher("kdroidFilter", "KmpRealTimeLogger") }

    var release by remember { mutableStateOf<Release?>(null) }
    var releases by remember { mutableStateOf<List<Release>?>(null) }
    var releaseCount by remember { mutableStateOf(3) }

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
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Fetch Multiple Releases", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = releaseCount.toString(),
                    onValueChange = { 
                        releaseCount = it.toIntOrNull() ?: 3
                    },
                    label = { Text("Number of releases") },
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        scope.launch {
                            releases = fetcher.getReleases(releaseCount)
                        }
                    },
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                ) {
                    Text("Fetch Releases")
                }
            }
        }

        releases?.let { releaseList ->
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("${releaseList.size} Releases Found:", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(releaseList) { release ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Version: ${release.tag_name}", style = MaterialTheme.typography.titleMedium)
                        Text("Published: ${release.published_at}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Changelog:", style = MaterialTheme.typography.bodyMedium)
                        Text(release.body, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
