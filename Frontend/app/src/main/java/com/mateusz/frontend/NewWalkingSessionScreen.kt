package com.mateusz.frontend

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun NewWalkingSessionScreen (onSaveChoice: () -> Unit, onCancelChoice: () -> Unit) {
    // State variables to hold the values entered by the user (nullable by default)
    var duration by remember { mutableIntStateOf(0) }
    var averagePulse by remember { mutableIntStateOf(0) }
    val password by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    var loginResult by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 48.dp, end = 48.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "New walking session",
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Average pulse TextField
            OutlinedTextField(
                value = averagePulse.toString(),
                onValueChange = {
                    averagePulse = it.toInt() // Convert to Int
                },
                label = { Text("Average pulse") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Duration TextField
            OutlinedTextField(
                value = duration.toString(),
                onValueChange = {
                    duration = it.toInt() // Convert to Int
                },
                label = { Text("Duration") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Save Button
            Button(
                onClick = {
                    // Launch a coroutine to perform the network request
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = makeAddNewWalkingSessionRequest(duration, averagePulse, password, context)
                        withContext(Dispatchers.Main) {
                            loginResult = result
                            if (result == "Success") {
                                onSaveChoice()
                            } else {
                                Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 42.dp, start = 32.dp, end = 32.dp)
                    .height(56.dp),  // Adjust height to match the screenshot
                shape = RoundedCornerShape(50),  // Rounded corners to match the screenshot
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.light_blue),  // Custom blue background
                    contentColor = colorResource(id = R.color.white)  // White text color
                )
            ) {
                Text(
                    "Save",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cancel Button
            OutlinedButton(
                onClick = {
                    onCancelChoice()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp),  // Adjust height to match the screenshot
                shape = RoundedCornerShape(50),  // Rounded corners to match the screenshot
                border = BorderStroke(
                    2.dp,
                    color = colorResource(id = R.color.light_blue)
                )  // Using custom color
            ) {
                Text(
                    "Cancel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.light_blue)  // White text for outlined button
                )
            }
        }
    }
}

private suspend fun makeAddNewWalkingSessionRequest(
    duration: Int,
    averagePulse: Int,
    password: String?,
    context: Context // Pass the context to access SharedPreferences
): String {
    val url = URL("http://10.0.2.2:8000/walking") // Replace with your actual API URL

    val connection = withContext(Dispatchers.IO) {
        url.openConnection() as HttpURLConnection
    }

    return try {
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")

        // Retrieve the JWT token from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPreferences.getString("access_token", null)
            ?: return "Token not found. Please log in."

        // Add the JWT token to the Authorization header
        connection.setRequestProperty("Authorization", "Bearer $jwtToken")

        connection.doOutput = true

        // Create JSON object for the profile update data
        val jsonBody = JSONObject().apply {
            // Only put values in the JSON object if they are not null
            put("average_pulse", averagePulse)
            put("duration", duration)
            password?.let { put("password", it) }
        }

        // Write the JSON data to the output stream
        withContext(Dispatchers.IO) {
            OutputStreamWriter(connection.outputStream).apply {
                write(jsonBody.toString())
                flush()
                close()
            }
        }

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            connection.inputStream.bufferedReader().use { it.readText() }
            return "Success"
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

@Preview(showBackground = true)
@Composable
fun PreviewNewWalkingSessionScreen() {
    NewWalkingSessionScreen (onSaveChoice = {}, onCancelChoice = {})
}