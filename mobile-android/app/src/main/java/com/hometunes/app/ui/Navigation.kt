package com.hometunes.app.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.hometunes.app.ui.screens.*
import com.hometunes.app.ui.theme.Primary

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Library : Screen("library", "Library", Icons.Default.LibraryMusic)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Player : Screen("player", "Player", Icons.Default.LibraryMusic) // Hidden from bottom nav
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Library,
    Screen.Settings
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isPlayerScreen = currentDestination?.route == Screen.Player.route

    Scaffold(
        bottomBar = {
            if (!isPlayerScreen) {
                NavigationBar(
                    containerColor = Color(0xFF0F0F1A),
                    contentColor = Color.White
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = Color(0xFF1A1A2E), // Subtle dark background for selected item
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) } // Simplified for now
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(showSnackbar = {})
            }
            composable(Screen.Library.route) {
                LibraryScreen(
                    onNavigateToPlayer = { navController.navigate(Screen.Player.route) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(Screen.Player.route) {
                PlayerScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
