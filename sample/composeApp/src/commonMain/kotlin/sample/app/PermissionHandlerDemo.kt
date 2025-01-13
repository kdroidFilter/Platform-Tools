package sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionHandlerDemo(screens: List<PermissionScreen> ) {

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

