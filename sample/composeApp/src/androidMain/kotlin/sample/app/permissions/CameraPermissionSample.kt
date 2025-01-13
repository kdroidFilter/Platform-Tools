package sample.app.permissions

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.permissionhandler.hasCameraPermission
import io.github.kdroidfilter.platformtools.permissionhandler.requestCameraPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPermissionSample() {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(hasCameraPermission()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Camera Permissions") })
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
                        text = if (permissionGranted) "Camera permission granted ✅" else "Camera permission required 🚫",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!permissionGranted) {
                        Button(
                            onClick = {
                                requestCameraPermission(
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
