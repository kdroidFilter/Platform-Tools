package sample.app.permissions

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.permissionhandler.hasRecordAudioPermission
import io.github.kdroidfilter.platformtools.permissionhandler.requestRecordAudioPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordAudioPermissionSample() {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(hasRecordAudioPermission()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Record Audio Permissions") })
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
                        text = if (permissionGranted) "Record Audio permission granted ✅" else "Record Audio permission required 🚫",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!permissionGranted) {
                        Button(
                            onClick = {
                                requestRecordAudioPermission(
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
