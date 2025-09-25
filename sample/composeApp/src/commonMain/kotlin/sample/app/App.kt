package sample.app


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.kdroidfilter.platformtools.darkmodedetector.isSystemInDarkMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class Route(
    val title: String,
    val destination: String,
    val content : @Composable () -> Unit
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val routes = listOf(
        Route("Core", "core", { CoreDemo() }),
        Route("App Manager", "appmanager", { AppManagerDemo() }),
        Route("Release Fetcher", "releasefetcher", { ReleaseFetcherDemo() }),
        Route("GitHub Repo Fetcher", "githubrepo", { GitHubRepoFetcherDemo() }),
        Route("Clipboard", "clipboard", { ClipboardDemo() }),
    )

    MaterialTheme(
        colorScheme = if (isSystemInDarkMode()) darkColorScheme() else lightColorScheme(),
        typography = Typography(),
        content = {
            ModalNavigationDrawer(
                drawerContent = {
                    DrawerContent(navController, scope, drawerState, routes)
                },
                drawerState = drawerState,
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Platform Tools Demo") },
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = routes.first().destination) {
                            routes.forEach { route ->
                                composable(route.destination) {
                                    route.content()
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun DrawerContent(
    navController: NavHostController,
    scope: CoroutineScope,
    drawerState: DrawerState,
    routes: List<Route>
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Modules", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))
            HorizontalDivider()

            routes.forEach { route ->
                NavigationDrawerItem(
                    label = { Text(route.title, fontSize = 18.sp) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        navController.navigate(route.destination) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

