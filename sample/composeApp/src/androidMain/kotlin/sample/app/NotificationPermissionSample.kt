package sample.app

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
import io.github.kdroidfilter.platformtools.permissionhandler.notification.hasNotificationPermission
import io.github.kdroidfilter.platformtools.permissionhandler.notification.requestNotificationPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPermissionSample() {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(hasNotificationPermission()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Notification Permissions") })
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
                        text = if (permissionGranted) "Notification permission granted ✅" else "Notification permission required 🚫",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!permissionGranted) {
                        Button(
                            onClick = {
                                requestNotificationPermission(
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

