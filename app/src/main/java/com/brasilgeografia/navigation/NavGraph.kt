package com.brasilgeografia.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.brasilgeografia.ui.screens.HomeScreen

@Composable
fun NavGraph(startDestination: String = "home") {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable("home") {
            HomeScreen()
        }
    }
}
