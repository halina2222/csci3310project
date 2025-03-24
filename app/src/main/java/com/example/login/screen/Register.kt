package com.example.login.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun registerScreen(paddingValues: PaddingValues,
                   onNavigateToLogin: () -> Unit = {},
                   onNavigateToCreation: () -> Unit = {}){
    Column {
        Text(text = "Register Page undo")
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onNavigateToLogin()
            }
        ) {
            Text("back to login")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onNavigateToCreation()
            }
        ) {
            Text("go to creation")
        }
    }
}

