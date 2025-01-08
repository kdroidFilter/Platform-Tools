package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.appmanager.getAppInstaller
import io.github.kdroidfilter.platformtools.getOperatingSystem
import io.github.kdroidfilter.platformtools.releasefetcher.downloader.Downloader
import io.github.kdroidfilter.platformtools.releasefetcher.github.GitHubReleaseFetcher
import kotlinx.coroutines.*
import java.io.File

@Composable
fun App() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BasicText(
                "Le système d'exploitation est " +
                        getOperatingSystem().name.lowercase().replaceFirstChar { it.uppercase() }
            )

            // Utilise un fetcher GitHub pour la démo
            val fetcher = remember {
                GitHubReleaseFetcher(
                    repoOwner = "kdroidfilter",
                    repo = "KmpRealTimeLogger"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            UpdateCheckerUI(fetcher)
        }
    }
}

@Composable
fun UpdateCheckerUI(fetcher: GitHubReleaseFetcher) {
    // États pour la version et le changelog
    var latestVersion by remember { mutableStateOf<String?>(null) }
    var changelog by remember { mutableStateOf<String?>(null) }

    // États pour le chargement
    var isChecking by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0.0) }
    var downloadedFile by remember { mutableStateOf<File?>(null) }

    // États pour l’installation
    var isInstalling by remember { mutableStateOf(false) }
    var installMessage by remember { mutableStateOf<String?>(null) }

    // Contrôle d’affichage de la boîte de dialogue
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Mise à jour de l'application",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton pour vérifier les mises à jour
        Button(
            onClick = {
                isChecking = true
                CoroutineScope(Dispatchers.IO).launch {
                    fetcher.checkForUpdate { version, notes ->
                        latestVersion = version
                        changelog = notes
                        isChecking = false
                        // Ouvre le dialog si on a récupéré une nouvelle version
                        showDialog = true
                    }
                }
            },
            enabled = !isChecking && !isDownloading && !isInstalling
        ) {
            if (isChecking) {
                // Loader pendant la vérification
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Vérification en cours…")
            } else {
                Text("Vérifier les mises à jour")
            }
        }

        // Boîte de dialogue pour afficher les nouveautés et proposer l'installation
        if (showDialog && latestVersion != null) {
            AlertDialog(
                onDismissRequest = {
                    // Ferme la boîte de dialogue lorsqu'on clique à l'extérieur ou sur 'Back'
                    showDialog = false
                },
                title = {
                    Text("Nouvelle version disponible : $latestVersion")
                },
                text = {
                    Text("Changelog :\n$changelog")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            // Lance le téléchargement
                            CoroutineScope(Dispatchers.IO).launch {
                                val release = fetcher.getLatestRelease()
                                val downloadLink = release?.let { fetcher.getDownloadLinkForPlatform(it) }
                                if (downloadLink != null) {
                                    isDownloading = true
                                    val downloader = Downloader()
                                    downloader.downloadApp(downloadLink) { progress, file ->
                                        downloadProgress = progress
                                        // On récupère le fichier téléchargé quand le téléchargement est terminé
                                        if (progress >= 100.0) {
                                            downloadedFile = file
                                        }
                                    }
                                    isDownloading = false
                                }
                            }
                        },
                        enabled = !isDownloading && !isInstalling
                    ) {
                        Text("Installer")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barre de progression pendant le téléchargement
        if (isDownloading) {
            Text("Téléchargement en cours…")
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = (downloadProgress / 100).toFloat(),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Si le fichier est téléchargé, on propose l'installation
        downloadedFile?.let { file ->
            Spacer(modifier = Modifier.height(16.dp))
            Text("Le fichier de mise à jour est prêt à être installé.")

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        isInstalling = true
                        val installer = getAppInstaller()

                        // Vérifie si on peut installer directement, sinon demande la permission
                        val canInstall = installer.canRequestInstallPackages()
                        if (!canInstall) {
                            installer.requestInstallPackagesPermission()
                            // Sur certaines plateformes, la demande de permission est asynchrone.
                            // On peut donc re-checker plus tard ou réessayer l’installation.
                        }

                        // Lance l’installation
                        installer.installApp(file) { success, message ->
                            installMessage = if (success) {
                                "Installation réussie."
                            } else {
                                "Échec de l'installation : $message"
                            }
                            isInstalling = false
                        }
                    }
                },
                enabled = !isInstalling
            ) {
                Text("Installer la mise à jour")
            }
        }

        // Message d’installation : succès ou erreur
        installMessage?.let { msg ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(msg, style = MaterialTheme.typography.bodyMedium)
        }

        // Loader pendant l’installation
        if (isInstalling) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }
    }
}
