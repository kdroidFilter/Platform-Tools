package sample.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.getAppVersion
import io.github.kdroidfilter.platformtools.getCacheDir
import io.github.kdroidfilter.platformtools.getOperatingSystem
import io.github.kdroidfilter.platformtools.getPlatform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoreDemo() {
    var operatingSystem = getOperatingSystem()
    var platform = getPlatform()
    var cacheDir = getCacheDir()
    var appVersion = getAppVersion()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Operating System: $operatingSystem", style = MaterialTheme.typography.bodyLarge)
        Text("Platform: $platform", style = MaterialTheme.typography.bodyLarge)
        Text("Cache Directory: ${cacheDir.absolutePath}", style = MaterialTheme.typography.bodyLarge)
        Text("App Version: $appVersion", style = MaterialTheme.typography.bodyLarge)
    }
}
