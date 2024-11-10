package com.mateusz.frontend

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

sealed class StepsUpdateResult {
    data object Success : StepsUpdateResult()
    data class Error(val message: String) : StepsUpdateResult()
}

@Composable
fun EditStepsScreen(onSaveChoice: () -> Unit, onCancelChoice: () -> Unit) {
    var goal by remember { mutableStateOf<Int?>(null) }
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
                text = "Edit steps",
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Goal TextField
            OutlinedTextField(
                value = goal?.toString() ?: "",
                onValueChange = {
                    goal = it.toIntOrNull()
                },
                label = { Text("Goal (e.g. 10000)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.light_blue),
                    focusedLabelColor = colorResource(id = R.color.light_blue),
                )
            )

            // Save Button
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = makeEditStepsRequest(goal, context)
                        withContext(Dispatchers.Main) {
                            when (result) {
                                is StepsUpdateResult.Success -> {
                                    onSaveChoice()
                                }
                                is StepsUpdateResult.Error -> {
                                    Toast.makeText(
                                        context,
                                        result.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 42.dp, start = 32.dp, end = 32.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.light_blue),
                    contentColor = colorResource(id = R.color.white)
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
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    2.dp,
                    color = colorResource(id = R.color.light_blue)
                )
            ) {
                Text(
                    "Cancel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.light_blue)
                )
            }
        }
    }
}

private suspend fun makeEditStepsRequest(
    goal: Int?,
    context: Context
): StepsUpdateResult {
    val url = URL("${NetworkConfig.getBaseUrl()}/steps/goal")

    val connection = withContext(Dispatchers.IO) {
        url.openConnection() as HttpURLConnection
    }

    return try {
        connection.requestMethod = "PATCH"
        connection.setRequestProperty("Content-Type", "application/json")

        // Retrieve the JWT token from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPreferences.getString("access_token", null)
            ?: return StepsUpdateResult.Error("Please login to update steps goal")

        // Add the JWT token to the Authorization header
        connection.setRequestProperty("Authorization", "Bearer $jwtToken")
        connection.doOutput = true

        // Create JSON object for the steps update data
        val jsonBody = JSONObject().apply {
            goal?.let { put("goal", it) }
        }

        // Write the JSON data to the output stream
        withContext(Dispatchers.IO) {
            OutputStreamWriter(connection.outputStream).apply {
                write(jsonBody.toString())
                flush()
                close()
            }
        }

        when (val responseCode = connection.responseCode) {
            HttpURLConnection.HTTP_CREATED -> {
                StepsUpdateResult.Success
            }
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                StepsUpdateResult.Error("Session expired. Please login again")
            }
            HttpURLConnection.HTTP_BAD_REQUEST -> {
                // Try to get error message from response
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                val errorMessage = errorResponse?.let {
                    try {
                        val jsonError = JSONObject(it)
                        jsonError.getString("message") ?: "Invalid steps goal"
                    } catch (e: Exception) {
                        "Invalid steps goal"
                    }
                } ?: "Invalid steps goal"
                StepsUpdateResult.Error(errorMessage)
            }
            else -> {
                // Try to get error message from response
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                val errorMessage = errorResponse?.let {
                    try {
                        val jsonError = JSONObject(it)
                        jsonError.getString("message") ?: "Failed to update steps goal: $responseCode"
                    } catch (e: Exception) {
                        "Failed to update steps goal: $responseCode"
                    }
                } ?: "Failed to update steps goal: $responseCode"
                StepsUpdateResult.Error(errorMessage)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        val errorMessage = when (e) {
            is java.net.ConnectException -> "Could not connect to server"
            is java.net.SocketTimeoutException -> "Connection timed out"
            is java.net.UnknownHostException -> "No internet connection"
            else -> "Error updating steps goal: ${e.localizedMessage}"
        }
        StepsUpdateResult.Error(errorMessage)
    } finally {
        connection.disconnect()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEditStepsScreen() {
    EditStepsScreen(onSaveChoice = {}, onCancelChoice = {})
}