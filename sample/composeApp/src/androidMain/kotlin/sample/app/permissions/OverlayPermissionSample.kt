package sample.app.permissions

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.permissionhandler.hasOverlayPermission
import io.github.kdroidfilter.platformtools.permissionhandler.requestOverlayPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayPermissionSample() {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(hasOverlayPermission()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Overlay Permissions") })
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
                        text = if (permissionGranted) "Overlay permission granted ✅" else "Overlay permission required 🚫",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!permissionGranted) {
                        Button(
                            onClick = {
                                requestOverlayPermission(
                                    onGranted = {
                                        permissionGranted = true
                                        Toast.makeText(context, "Overlay permission granted", Toast.LENGTH_SHORT).show()
                                    },
                                    onDenied = {
                                        permissionGranted = false
                                        Toast.makeText(context, "Overlay permission denied", Toast.LENGTH_SHORT).show()
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

