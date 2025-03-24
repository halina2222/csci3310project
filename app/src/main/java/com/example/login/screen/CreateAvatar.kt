package com.example.login.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CreateAvatar(paddingValues: PaddingValues,
                 onNavigateToLogin: () -> Unit = {},
                 onBack: () -> Unit = {}){
    Column {
        Text(text = "CreateAvatar Page undo")
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack
        ) {
            Text("back to login")
        }
    }
}