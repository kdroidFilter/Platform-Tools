// ReadExternalStoragePermissionSample.kt
package sample.app.permissions

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.permissionhandler.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadExternalStoragePermissionSample() {
    val context = LocalContext.current
    var permissionImagesGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                hasReadExternalStoragePermission(setOf(MediaType.IMAGES))
            } else {
                hasReadExternalStoragePermission()
            }
        )
    }
    var permissionVideoGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                hasReadExternalStoragePermission(setOf(MediaType.VIDEO))
            } else {
                true
            }
        )
    }
    var permissionAudioGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                hasReadExternalStoragePermission(setOf(MediaType.AUDIO))
            } else {
                true
            }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("External Storage Permissions") }
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // Android 13+ : Show three separate buttons
                        Text(
                            text = "Media Permissions Required",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                requestReadExternalStoragePermission(
                                    mediaTypes = setOf(MediaType.IMAGES),
                                    onGranted = {
                                        permissionImagesGranted = true
                                        Toast.makeText(context, "Image permission granted", Toast.LENGTH_SHORT).show()
                                    },
                                    onDenied = {
                                        permissionImagesGranted = false
                                        Toast.makeText(context, "Image permission denied", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            enabled = !permissionImagesGranted
                        ) {
                            Text("Request Image Permission")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                requestReadExternalStoragePermission(
                                    mediaTypes = setOf(MediaType.VIDEO),
                                    onGranted = {
                                        permissionVideoGranted = true
                                        Toast.makeText(context, "Video permission granted", Toast.LENGTH_SHORT).show()
                                    },
                                    onDenied = {
                                        permissionVideoGranted = false
                                        Toast.makeText(context, "Video permission denied", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            enabled = !permissionVideoGranted
                        ) {
                            Text("Request Video Permission")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                requestReadExternalStoragePermission(
                                    mediaTypes = setOf(MediaType.AUDIO),
                                    onGranted = {
                                        permissionAudioGranted = true
                                        Toast.makeText(context, "Audio permission granted", Toast.LENGTH_SHORT).show()
                                    },
                                    onDenied = {
                                        permissionAudioGranted = false
                                        Toast.makeText(context, "Audio permission denied", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            enabled = !permissionAudioGranted
                        ) {
                            Text("Request Audio Permission")
                        }
                    } else {
                        // Android <13 : Show a single button
                        Text(
                            text = if (permissionImagesGranted) {
                                "External storage permission granted ✅"
                            } else {
                                "External storage permission required 🚫"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (!permissionImagesGranted) {
                            Button(
                                onClick = {
                                    requestReadExternalStoragePermission(
                                        mediaTypes = emptySet(),
                                        onGranted = {
                                            permissionImagesGranted = true
                                            Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
                                        },
                                        onDenied = {
                                            permissionImagesGranted = false
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
        }
    )
}
