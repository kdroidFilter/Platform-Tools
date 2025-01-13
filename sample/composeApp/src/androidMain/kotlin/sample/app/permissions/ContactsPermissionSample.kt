package sample.app.permissions

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.platformtools.permissionhandler.hasReadContactsPermission
import io.github.kdroidfilter.platformtools.permissionhandler.requestReadContactsPermission
import io.github.kdroidfilter.platformtools.permissionhandler.hasWriteContactsPermission
import io.github.kdroidfilter.platformtools.permissionhandler.requestWriteContactsPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsPermissionSample() {
    val context = LocalContext.current

    var readContactsGranted by remember {
        mutableStateOf(hasReadContactsPermission())
    }
    var writeContactsGranted by remember {
        mutableStateOf(hasWriteContactsPermission())
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Contacts Permissions") })
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

                    // ---- READ CONTACTS ----
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = if (readContactsGranted)
                                "Read contacts granted ✅"
                            else
                                "Read contacts required 🚫",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (!readContactsGranted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    requestReadContactsPermission(
                                        onGranted = {
                                            readContactsGranted = true
                                            Toast.makeText(
                                                context,
                                                "Read contacts granted",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onDenied = {
                                            readContactsGranted = false
                                            Toast.makeText(
                                                context,
                                                "Read contacts denied",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            ) {
                                Text("Request Read Contacts Permission")
                            }
                        }
                    }

                    // ---- WRITE CONTACTS ----
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (writeContactsGranted)
                                "Write contacts granted ✅"
                            else
                                "Write contacts required 🚫",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (!writeContactsGranted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    requestWriteContactsPermission(
                                        onGranted = {
                                            writeContactsGranted = true
                                            Toast.makeText(
                                                context,
                                                "Write contacts granted",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onDenied = {
                                            writeContactsGranted = false
                                            Toast.makeText(
                                                context,
                                                "Write contacts denied",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            ) {
                                Text("Request Write Contacts Permission")
                            }
                        }
                    }
                }
            }
        }
    )
}
