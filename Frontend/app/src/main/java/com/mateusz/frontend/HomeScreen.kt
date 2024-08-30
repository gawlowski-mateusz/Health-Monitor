package com.mateusz.frontend

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext


@Composable
fun HomeScreen(onLoginPanelChoose : () -> Unit, onSignupPanelChoice : () -> Unit) {
    val context = LocalContext.current // Access the current context

    // Simple UI for home screen
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Health Care")

        // Login Button
        Button(onClick = {
//            Toast.makeText(context, "Login clicked", Toast.LENGTH_SHORT).show()
//             Simulate Sign Up logic here
            onLoginPanelChoose()
        }) {
            Text("Login")
        }

        // Sign Up Button
        Button(onClick = {
//            Toast.makeText(context, "Sign up clicked", Toast.LENGTH_SHORT).show()
//             Simulate Sign Up logic here
            onSignupPanelChoice()
        }) {
            Text("Sign Up")
        }
    }
}