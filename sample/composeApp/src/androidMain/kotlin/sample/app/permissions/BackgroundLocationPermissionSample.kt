package sample.app.permissions

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.permissionhandler.hasBackgroundLocationPermission
import io.github.kdroidfilter.platformtools.permissionhandler.requestBackgroundLocationPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundLocationPermissionSample() {
    val context = LocalContext.current
    var backgroundLocationGranted by remember { mutableStateOf(hasBackgroundLocationPermission()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Background Location Permission") })
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Background Location Status
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (backgroundLocationGranted)
                                "Background location granted ✅"
                            else
                                "Background location required 🚫",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (!backgroundLocationGranted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    requestBackgroundLocationPermission(
                                        onGranted = {
                                            backgroundLocationGranted = true
                                            Toast.makeText(
                                                context,
                                                "Background location granted",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onDenied = {
                                            backgroundLocationGranted = false
                                            Toast.makeText(
                                                context,
                                                "Background location denied",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            ) {
                                Text("Request Background Location")
                            }
                        }
                    }
                }
            }
        }
    )
}
