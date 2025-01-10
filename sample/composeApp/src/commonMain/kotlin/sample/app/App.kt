package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.appmanager.getAppInstaller
import io.github.kdroidfilter.platformtools.appmanager.restartApplication
import io.github.kdroidfilter.platformtools.getAppVersion
import io.github.kdroidfilter.platformtools.getOperatingSystem
import io.github.kdroidfilter.platformtools.releasefetcher.downloader.Downloader
import io.github.kdroidfilter.platformtools.releasefetcher.github.GitHubReleaseFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun App() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            UpdateCheckerUI(GitHubReleaseFetcher(owner = "kdroidfilter", repo = "KmpRealTimeLogger"))

            Text(
                "Platform: " + getOperatingSystem().name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                "Version: " + getAppVersion(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

        }
    }
}

@Composable
fun UpdateCheckerUI(fetcher: GitHubReleaseFetcher) {

    var latestVersion by remember { mutableStateOf<String?>(null) }
    var changelog by remember { mutableStateOf<String?>(null) }

    var isChecking by remember { mutableStateOf(false) }

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0.0) }
    var downloadedFile by remember { mutableStateOf<File?>(null) }

    var isInstalling by remember { mutableStateOf(false) }
    var installMessage by remember { mutableStateOf<String?>(null) }

    var showUpdateAvailableDialog by remember { mutableStateOf(false) }
    var showNoUpdateDialog by remember { mutableStateOf(false) }
    var showDownloadProgressDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button({
            restartApplication()
        }){
            Text("Restart App")
        }

        Text(
            text = "Application Update",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isChecking = true
                CoroutineScope(Dispatchers.IO).launch {
                    fetcher.checkForUpdate { version, notes ->
                        isChecking = false
                        latestVersion = version
                        changelog = notes
                        showUpdateAvailableDialog = true
                    }
                }
            },
            enabled = !isChecking && !isInstalling && !isDownloading,
        ) {
            if (isChecking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Checking updates…", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text("Check for updates", style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (showUpdateAvailableDialog && latestVersion != null) {
            AlertDialog(
                onDismissRequest = {
                    showUpdateAvailableDialog = false
                },
                title = {
                    Text("New version available: $latestVersion")
                },
                text = {
                    Text("Changelog:\n$changelog")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showUpdateAvailableDialog = false
                            showDownloadProgressDialog = true
                            CoroutineScope(Dispatchers.IO).launch {
                                isDownloading = true
                                val release = fetcher.getLatestRelease()
                                val downloadLink = release?.let { fetcher.getDownloadLinkForPlatform(it) }
                                if (downloadLink != null) {
                                    val downloader = Downloader()
                                    downloader.downloadApp(downloadLink) { progress, file ->
                                        downloadProgress = progress
                                        if (progress >= 100.0) {
                                            downloadedFile = file
                                            isDownloading = false
                                        }
                                    }
                                } else {
                                    isDownloading = false
                                }
                            }
                        },
                        enabled = !isDownloading && !isInstalling
                    ) {
                        Text("Download & Install")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showUpdateAvailableDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showNoUpdateDialog) {
            AlertDialog(
                onDismissRequest = {
                    showNoUpdateDialog = false
                },
                title = {
                    Text("No updates available")
                },
                text = {
                    Text("You are already using the latest version of the application.")
                },
                confirmButton = {
                    Button(onClick = {
                        showNoUpdateDialog = false
                    }) {
                        Text("OK")
                    }
                }
            )
        }

        if (showDownloadProgressDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDownloadProgressDialog = false
                },
                title = {
                    Text(if (isDownloading) "Downloading…" else "Download Complete")
                },
                text = {
                    if (isDownloading) {
                        Column {
                            Text("Downloading update, please wait.")
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (downloadProgress / 100).toFloat() },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Progress: ${downloadProgress.toInt()}%")
                        }
                    } else {
                        Text("The update has been downloaded and is ready to install.")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDownloadProgressDialog = false
                            CoroutineScope(Dispatchers.IO).launch {
                                isInstalling = true
                                val installer = getAppInstaller()
                                if (!installer.canRequestInstallPackages()) {
                                    installer.requestInstallPackagesPermission()
                                }
                                downloadedFile?.let { file ->
                                    installer.installApp(file) { success, message ->
                                        installMessage = if (success) {
                                            "Installation succeeded."
                                        } else {
                                            "Installation failed: $message"
                                        }
                                        isInstalling = false
                                    }
                                }
                            }
                        },
                        enabled = downloadedFile != null && !isInstalling
                    ) {
                        Text("Install Now")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showDownloadProgressDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        installMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        }

        if (isInstalling) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
        }
    }
}
