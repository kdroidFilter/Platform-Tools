package sample.app.permissions

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.permissionhandler.hasLocationPermission
import io.github.kdroidfilter.platformtools.permissionhandler.requestLocationPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPermissionSample() {
    val context = LocalContext.current
    var preciseLocationGranted by remember { mutableStateOf(hasLocationPermission(preciseLocation = true)) }
    var approximateLocationGranted by remember { mutableStateOf(hasLocationPermission(preciseLocation = false)) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Location Permissions") })
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
                    // Precise Location Status
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = if (preciseLocationGranted)
                                "Precise location granted ✅"
                            else
                                "Precise location required 🚫",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (!preciseLocationGranted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    requestLocationPermission(
                                        preciseLocation = true,
                                        onGranted = {
                                            preciseLocationGranted = true
                                            Toast.makeText(
                                                context,
                                                "Precise location granted",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onDenied = {
                                            preciseLocationGranted = false
                                            Toast.makeText(
                                                context,
                                                "Precise location denied",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            ) {
                                Text("Request Precise Location")
                            }
                        }
                    }

                    // Approximate Location Status
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (approximateLocationGranted)
                                "Approximate location granted ✅"
                            else
                                "Approximate location required 🚫",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (!approximateLocationGranted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    requestLocationPermission(
                                        preciseLocation = false,
                                        onGranted = {
                                            approximateLocationGranted = true
                                            Toast.makeText(
                                                context,
                                                "Approximate location granted",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onDenied = {
                                            approximateLocationGranted = false
                                            Toast.makeText(
                                                context,
                                                "Approximate location denied",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            ) {
                                Text("Request Approximate Location")
                            }
                        }
                    }
                }
            }
        }
    )
}