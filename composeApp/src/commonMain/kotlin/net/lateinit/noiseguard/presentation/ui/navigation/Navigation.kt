package net.lateinit.noiseguard.presentation.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.lateinit.noiseguard.presentation.ui.screen.AnalysisScreen
import net.lateinit.noiseguard.presentation.ui.screen.HistoryScreen
import net.lateinit.noiseguard.presentation.ui.screen.HomeScreen
import net.lateinit.noiseguard.presentation.ui.screen.RecordingScreen
import net.lateinit.noiseguard.presentation.ui.screen.SettingsScreen
import net.lateinit.noiseguard.presentation.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Recording : Screen("recording")
    object History : Screen("history")
    object Analysis : Screen("analysis")
    object Settings : Screen("settings")
}

@Composable
fun NoiseGuardNavigation(
    homeViewModel: HomeViewModel = koinViewModel()
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            // TODO: BottomNavigationBar 구현
            // BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(homeViewModel)
            }
            composable(Screen.Recording.route) {
                RecordingScreen()
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.Analysis.route) {
                AnalysisScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}