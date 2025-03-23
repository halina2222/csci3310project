package com.example.csci3310project

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.example.login.login_screen.LoginScreen as OriginalLoginScreen

@Composable
fun LoginScreen(paddingValues: PaddingValues, onNavigateToCameraScreen: () -> Unit = {}) {
    OriginalLoginScreen(paddingValues, onNavigateToCameraScreen)
}
