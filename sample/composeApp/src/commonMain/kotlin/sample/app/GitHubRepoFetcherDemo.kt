package sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.releasefetcher.github.GitHubReleaseFetcher
import io.github.kdroidfilter.platformtools.releasefetcher.github.model.Release
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubRepoFetcherDemo() {
    val scope = rememberCoroutineScope()

    var url by remember { mutableStateOf(TextFieldValue("https://github.com/kdroidFilter/KmpRealTimeLogger")) }
    var owner by remember { mutableStateOf<String?>("kdroidFilter") }
    var repo by remember { mutableStateOf<String?>("KmpRealTimeLogger") }

    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var release by remember { mutableStateOf<Release?>(null) }

    fun parseRepo(input: String) {
        error = null
        release = null
        val trimmed = input.trim()
        // Accept forms: https://github.com/owner/repo, http://github.com/owner/repo, github.com/owner/repo, owner/repo
        val cleaned = trimmed
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
        val withoutHost =
            if (cleaned.startsWith("github.com/", ignoreCase = true)) cleaned.substringAfter("github.com/")
            else cleaned
        val parts = withoutHost.split('/').filter { it.isNotBlank() }

        if (parts.size >= 2) {
            owner = parts[0]
            repo = parts[1]
        } else if (trimmed.contains('/')) {
            val sub = trimmed.split('/').filter { it.isNotBlank() }
            if (sub.size >= 2) {
                owner = sub[0]
                repo = sub[1]
            } else {
                owner = null
                repo = null
                error = "Invalid repository format. Use owner/repo or a GitHub URL."
            }
        } else {
            owner = null
            repo = null
            error = "Invalid repository format. Use owner/repo or a GitHub URL."
        }
    }

    fun canFetch(): Boolean = !loading && !owner.isNullOrBlank() && !repo.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("GitHub Release Fetcher (by repository link)", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = url,
            onValueChange = {
                url = it
                parseRepo(it.text)
            },
            label = { Text("GitHub repository link (e.g., https://github.com/owner/repo)") },
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            singleLine = true
        )

        if (owner != null && repo != null) {
            AssistChip(
                onClick = {},
                label = { Text("owner: $owner | repo: $repo") }
            )
        }

        if (error != null) {
            Text(
                error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    parseRepo(url.text)
                    if (!canFetch()) return@Button
                    loading = true
                    error = null
                    release = null
                    val o = owner!!
                    val r = repo!!
                    scope.launch {
                        try {
                            val fetcher = GitHubReleaseFetcher(o, r)
                            release = fetcher.getLatestRelease()
                            if (release == null) {
                                error = "No release found for $o/$r."
                            }
                        } catch (e: Exception) {
                            error = "Fetch error: ${e.message}"
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = canFetch()
            ) {
                Text(if (loading) "Loading..." else "Fetch latest release")
            }
        }

        HorizontalDivider()

        release?.let { rel ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        "${rel.name.ifBlank { rel.tag_name }} (${rel.tag_name})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                item { Text("Published at: ${rel.published_at}", style = MaterialTheme.typography.bodyMedium) }
                item { Text("Author: ${rel.author.login}", style = MaterialTheme.typography.bodyMedium) }
                item { Text("Link: ${rel.html_url}", style = MaterialTheme.typography.bodyMedium) }
                item { Text("Tag : ${rel.tag_name}", style = MaterialTheme.typography.bodyMedium) }


                if (rel.body.isNotBlank()) {
                    item { Text("Release notes:") }
                    item { Text(rel.body, style = MaterialTheme.typography.bodySmall) }
                }

                if (rel.assets.isNotEmpty()) {
                    item { Text("Assets:", style = MaterialTheme.typography.titleSmall) }
                    items(rel.assets) { asset ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(asset.name, style = MaterialTheme.typography.bodyLarge)
                                Text("Size: ${asset.size} bytes", style = MaterialTheme.typography.bodySmall)
                                Text("Download: ${asset.browser_download_url}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
