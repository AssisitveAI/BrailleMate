package kr.ac.kaist.aailab.braillemate.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kr.ac.kaist.aailab.braillemate.android.R
import kr.ac.kaist.aailab.braillemate.android.ui.bookmark.BookmarkScreen
import kr.ac.kaist.aailab.braillemate.android.ui.chat.ChatScreen
import kr.ac.kaist.aailab.braillemate.android.ui.home.HomeScreen
import kr.ac.kaist.aailab.braillemate.android.ui.regulation.RegulationDetailScreen
import kr.ac.kaist.aailab.braillemate.android.ui.regulation.RegulationListScreen
import kr.ac.kaist.aailab.braillemate.android.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Regulation : Screen("regulation")
    data object RegulationSection : Screen("regulation/{section}") {
        fun createRoute(section: String) = "regulation/$section"
    }
    data object RegulationDetail : Screen("regulation_detail/{id}") {
        fun createRoute(id: Int) = "regulation_detail/$id"
    }
    data object Chat : Screen("chat")
    data object Bookmark : Screen("bookmark")
    data object Settings : Screen("settings")
}

data class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Regulation, R.string.nav_regulation, Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    BottomNavItem(Screen.Chat, R.string.nav_chat, Icons.Filled.SmartToy, Icons.Outlined.SmartToy),
    BottomNavItem(Screen.Bookmark, R.string.nav_bookmark, Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
    BottomNavItem(Screen.Settings, R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun BrailleMateMainScreen() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.labelRes)
                                )
                            },
                            label = { Text(stringResource(item.labelRes)) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToChat = {
                        navController.navigate(Screen.Chat.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToRegulation = {
                        navController.navigate(Screen.Regulation.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToRegulationSection = { section ->
                        navController.navigate(Screen.RegulationSection.createRoute(section))
                    },
                    onNavigateToRegulationDetail = { id ->
                        navController.navigate(Screen.RegulationDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.Regulation.route) {
                RegulationListScreen(
                    section = null,
                    onNavigateBack = { navController.popBackStack() },
                    onRegulationClick = { id ->
                        navController.navigate(Screen.RegulationDetail.createRoute(id))
                    }
                )
            }

            composable(
                Screen.RegulationSection.route,
                arguments = listOf(navArgument("section") { type = NavType.StringType })
            ) { backStackEntry ->
                val section = backStackEntry.arguments?.getString("section") ?: ""
                RegulationListScreen(
                    section = section,
                    onNavigateBack = { navController.popBackStack() },
                    onRegulationClick = { id ->
                        navController.navigate(Screen.RegulationDetail.createRoute(id))
                    }
                )
            }

            composable(
                Screen.RegulationDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                RegulationDetailScreen(
                    regulationId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Chat.route) {
                ChatScreen(
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Bookmark.route) {
                BookmarkScreen(
                    onRegulationClick = { id ->
                        navController.navigate(Screen.RegulationDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
