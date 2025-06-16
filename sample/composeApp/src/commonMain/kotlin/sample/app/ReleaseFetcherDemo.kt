package sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    }
}
