package com.example.csci3310project

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.login.takeuserface.CameraScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "loginScreen"
    ) {
        composable("loginScreen") {
            LoginScreen(
                paddingValues = PaddingValues(),
                onNavigateToCameraScreen = { navController.navigate("cameraScreen") }
            )
        }
        composable("cameraScreen") {
            CameraScreen(paddingValues = PaddingValues())
        }
    }
}
