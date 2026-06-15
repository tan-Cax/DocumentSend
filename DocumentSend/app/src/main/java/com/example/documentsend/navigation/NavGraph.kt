package com.example.documentsend.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.documentsend.ui.view.History
import com.example.documentsend.ui.view.Send
import com.example.documentsend.ui.view.Home
import com.example.documentsend.ui.view.Log
import com.example.documentsend.ui.view.About
import com.example.documentsend.ui.view.Receive
import com.example.documentsend.ui.view.Settings
import com.example.documentsend.ui.view.Text
import com.example.documentsend.viewmodel.DocViewModel
import com.example.documentsend.viewmodel.HistoryViewModel
import com.example.documentsend.viewmodel.SettingsViewModel
import com.example.documentsend.ui.components.FirstLaunchDialog

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val settingsViewModel: SettingsViewModel = viewModel()
    val settingsState = settingsViewModel.settingsState

    if (settingsState.isSettingsLoaded && settingsState.isFirstLaunch == 1) {
        FirstLaunchDialog(
            onDismiss = {
                settingsViewModel.updateIsFirstLaunch(0)
            }
        )
    }

    val viewmodel : DocViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ){
        composable(
            route = Screen.Home.route
        ){
            Home(
                navController = navController,
                viewModel = viewmodel
            )
        }
        composable(
            route = Screen.Send.route
        ){
            Send(
                navController = navController,
                viewModel = viewmodel
            )
        }
        composable(
            route = Screen.Receive.route
        ){
            Receive(
                navController = navController,
                viewModel = viewmodel
            )
        }
        composable(
            route = Screen.Text.route
        ){
            Text(
                navController = navController,
                viewModel = viewmodel
            )
        }
        composable(
            route = Screen.Settings.route
        ){
            Settings(
                navController = navController,
                viewModel = settingsViewModel
            )
        }
        composable(
            route = Screen.History.route
        ){
            val historyViewModel: HistoryViewModel = viewModel()
            History(
                navController = navController,
                viewModel = historyViewModel,
                DocViewModel = viewmodel
            )
        }
        composable(
            route = Screen.Log.route
        ){
            Log(
                navController = navController
            )
        }
        composable(
            route = Screen.About.route
        ){
            About(
                navController = navController
            )
        }
    }
}