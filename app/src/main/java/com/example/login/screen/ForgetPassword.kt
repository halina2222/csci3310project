package com.example.login.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ForgetPasswordScreen(paddingValues: PaddingValues, onNavigateToLogin: () -> Unit = {}){
    Column {
        Text(text = "ForgetPassword Page undo")
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onNavigateToLogin()
            }
        ) {
            Text("back to login")
        }
    }
}