package sample.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionExamplesApp()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionExamplesApp() {
    val screens = listOf(
        PermissionScreen("Common App", {App()}),
        PermissionScreen("Notification", { NotificationPermissionSample() }),
        PermissionScreen("Installation", { InstallPermissionSample() }),
        PermissionScreen("Overlay", { OverlayPermissionSample() }),
        PermissionScreen("Location", { LocationPermissionSample() }),
    )
    var currentScreenIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permission Examples") },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = currentScreenIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                screens.forEachIndexed { index, screen ->
                    Tab(
                        selected = currentScreenIndex == index,
                        onClick = { currentScreenIndex = index },
                        text = { Text(screen.title) }
                    )
                }
            }

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                screens[currentScreenIndex].content()
            }
        }
    }
}

data class PermissionScreen(
    val title: String,
    val content: @Composable () -> Unit
)

