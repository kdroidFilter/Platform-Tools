package sample.app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.permissionhandler.hasInstallPermission
import io.github.kdroidfilter.platformtools.permissionhandler.requestInstallPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallPermissionSample() {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(hasInstallPermission()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Install Permissions") })
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (permissionGranted) "Install permission granted ✅" else "Install permission required 🚫",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!permissionGranted) {
                        Button(
                            onClick = {
                                requestInstallPermission(
                                    onGranted = {
                                        permissionGranted = true
                                        Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
                                    },
                                    onDenied = {
                                        permissionGranted = false
                                        Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        ) {
                            Text("Request Permission")
                        }
                    }
                }
            }
        }
    )
}

