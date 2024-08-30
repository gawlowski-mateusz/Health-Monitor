package com.mateusz.frontend

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onNavigateToHomeScreen: () -> Unit) {
    val context = LocalContext.current
    var loginResult by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login Screen")

        Button(onClick = {
            // Launch a coroutine to perform the network request
            CoroutineScope(Dispatchers.IO).launch {
                val result = makeLoginRequest()
                withContext(Dispatchers.Main) {
                    loginResult = result
                    if (result == "Success") {
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onNavigateToHomeScreen) {
            Text("Home screen")
        }
    }
}

private suspend fun makeLoginRequest(): String {
    val url = URL("http://10.0.2.2:8000/api/auth/login")
    val connection = withContext(Dispatchers.IO) {
        url.openConnection()
    } as HttpURLConnection

    return try {
        connection.requestMethod = "GET"

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
