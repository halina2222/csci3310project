package com.example.csci3310project

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.login.takeuserface.CameraScreen
import com.example.login.screen.*

sealed class Screen(val route: String){
    object Login : Screen("login_screen")
    object Camera : Screen("camera_screen")
    object Register : Screen("register_screen")
    object ForgetPassword : Screen("forgetpw_screen")
    object AvatarCreation: Screen("create_screen")

}
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                paddingValues = PaddingValues(),
                onNavigateToCameraScreen = { navController.navigate(Screen.Camera.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgetPw = { navController.navigate(Screen.ForgetPassword.route) }
            )
        }
        composable(Screen.Camera.route) {
            CameraScreen(paddingValues = PaddingValues())
        }

        composable(Screen.Register.route) {
            registerScreen(
                paddingValues = PaddingValues(),
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToCreation = {navController.navigate(Screen.AvatarCreation.route)}
            )
        }
        composable(Screen.ForgetPassword.route) {
            ForgetPasswordScreen(
                paddingValues = PaddingValues(),
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.AvatarCreation.route) {
            CreateAvatar(
                paddingValues = PaddingValues(),
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onBack = {navController.popBackStack()}
            )
        }
    }
}
