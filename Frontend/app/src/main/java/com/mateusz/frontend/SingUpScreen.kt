package com.mateusz.frontend

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun SignUpScreen(onSignUpSuccess: () -> Unit, onNavigateToHomeScreen: () -> Unit) {
    val context = LocalContext.current
    var signUpResult by remember { mutableStateOf("") }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Name TextField
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email TextField
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password TextField
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Birth Date TextField
        OutlinedTextField(
            value = birthDate,
            onValueChange = { birthDate = it },
            label = { Text("Birth Date (YYYY-MM-DD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sign Up Button
        Button(onClick = {
            // Launch a coroutine to perform the network request
            CoroutineScope(Dispatchers.IO).launch {
                val result = makeSignUpRequest()
                withContext(Dispatchers.Main) {
                    signUpResult = result
                    if (result == "Success") {
                        onSignUpSuccess()
                    } else {
                        Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Home screen Button
        Button(onClick = {
            onNavigateToHomeScreen()
        }) {
            Text("Home Screen")
        }
    }
}

private suspend fun makeSignUpRequest(): String {
    val url = URL("http://10.0.2.2:8000/api/auth/sign-up") // Use 10.0.2.2 instead of localhost for emulator

    val connection = withContext(Dispatchers.IO) {
        url.openConnection() as HttpURLConnection
    }

    return try {
        connection.requestMethod = "GET" // Using GET method

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            "Failed with response code $responseCode"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Error: ${e.localizedMessage}"
    } finally {
        connection.disconnect()
    }
}

