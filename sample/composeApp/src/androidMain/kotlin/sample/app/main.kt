package sample.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import sample.app.permissionHandler.NotificationPermissionSample
import sample.app.permissions.*

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val permissionScreenList = listOf(
                PermissionScreen("Notification", { NotificationPermissionSample() }),
                PermissionScreen("Installation", { InstallPermissionSample() }),
                PermissionScreen("Overlay", { OverlayPermissionSample() }),
                PermissionScreen("Location", { LocationPermissionSample() }),
                PermissionScreen("Background Location", { BackgroundLocationPermissionSample() }),
                PermissionScreen("Camera", { CameraPermissionSample() }),
                PermissionScreen("Contacts", { ContactsPermissionSample() }),
                PermissionScreen("Record Audio", { RecordAudioPermissionSample() }),
                PermissionScreen("Read External Storage", { ReadExternalStoragePermissionSample() }),
                PermissionScreen("Bluetooth", { BluetoothPermissionSample() }),

                )
            App(permissionScreenList)
        }
    }
}


