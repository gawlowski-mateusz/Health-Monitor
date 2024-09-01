package com.mateusz.frontend

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun EditProfileScreen() {
    // State variables to hold the values entered by the user
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Height TextField
            TextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Name TextField
            TextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password TextField
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Save Button
                Button(
                    onClick = {
                        // Handle save logic here
                    },
                ) {
                    Text(text = "Save")
                }

                // Cancel Button
                Button(
                    onClick = {
                        // Handle save logic here
                    },
                ) {
                    Text(text = "Cancel")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEditProfileScreen() {
    EditProfileScreen()
}