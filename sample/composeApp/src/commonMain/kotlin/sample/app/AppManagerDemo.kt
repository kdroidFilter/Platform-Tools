package sample.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.appmanager.hasAppVersionChanged
import io.github.kdroidfilter.platformtools.appmanager.isFirstInstallation
import io.github.kdroidfilter.platformtools.appmanager.restartApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerDemo() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(if (isFirstInstallation()) "This is the first Installation !" else "This is not the first Installation !", style = MaterialTheme.typography.bodyLarge)

            Text(if (hasAppVersionChanged()) "The app was updated !" else "The app was not updated !", style = MaterialTheme.typography.bodyLarge)
            Button(onClick = { restartApplication() }) {
                Text("Restart Application")
            }
            Text("For detailed usage of this module, refer to https://github.com/kdroidFilter/AppwithAutoUpdater", style = MaterialTheme.typography.bodyLarge)
        }

}
