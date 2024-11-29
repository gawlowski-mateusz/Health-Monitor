package com.mateusz.frontend

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

sealed class WalkingSessionResult {
    data object Success : WalkingSessionResult()
    data class Error(val message: String) : WalkingSessionResult()
}

@Composable
fun NewWalkingSessionScreen(
    onSaveChoice: (LocalDate?) -> Unit,
    onCancelChoice: (LocalDate?) -> Unit
) {
    var duration by remember { mutableIntStateOf(0) }
    var averagePulse by remember { mutableIntStateOf(0) }
    val password by remember { mutableStateOf<String?>(null) }
    val selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    val context = LocalContext.current

    val heartRateReadings = remember { mutableStateListOf<Int>() }
    var lastHeartRateUpdate by remember { mutableLongStateOf(0L) }
    var isReceivingData by remember { mutableStateOf(false) }

    // Add a coroutine scope for the polling
    val scope = rememberCoroutineScope()
    var pollingJob by remember { mutableStateOf<Job?>(null) }
    var isPolling by remember { mutableStateOf(false) }

    // State for Gadgetbridge availability
    val isGadgetBridgeInstalled = remember {
        GadgetBridgeHelper.isGadgetBridgeInstalled(context)
    }

    // Function to start polling
    fun startPolling() {
        Log.d("HeartRatePolling", "Starting polling...")
        pollingJob?.cancel()
        isPolling = true
        pollingJob = scope.launch {
            Log.d("HeartRatePolling", "Inside launch block")
            try {
                while (isActive) {
                    Log.d("HeartRatePolling", "Polling iteration started")
                    try {
                        val heartRate = GadgetbridgeDatabase.getLatestHeartRate(context)
                        Log.d("HeartRatePolling", "Got heart rate: $heartRate")
                        if (heartRate > 0) {
                            heartRateReadings.add(heartRate)
                            lastHeartRateUpdate = System.currentTimeMillis()
                            isReceivingData = true
                            averagePulse = heartRateReadings.average().toInt()
                            Log.d("HeartRatePolling", "Updated average pulse to: $averagePulse")
                        }
                    } catch (e: Exception) {
                        Log.e("HeartRatePolling", "Error in polling: ${e.message}", e)
                    }
                    delay(1000)
                }
            } finally {
                Log.d("HeartRatePolling", "Polling loop ended")
                isPolling = false
            }
        }
    }

    // Clean up when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            pollingJob?.cancel()
        }
    }

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

            // Average pulse TextField with Gadgetbridge button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = averagePulse.toString(),
                    onValueChange = { }, // Empty because we don't want manual changes
                    label = { Text("Average pulse") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.light_blue),
                        focusedLabelColor = colorResource(id = R.color.light_blue),
                        disabledTextColor = Color.Black,
                        disabledBorderColor = colorResource(id = R.color.light_blue),
                        disabledLabelColor = colorResource(id = R.color.light_blue),
                    ),
                    enabled = false,
                    trailingIcon = if (heartRateReadings.isNotEmpty()) {
                        { Text("(${heartRateReadings.size} readings)") }
                    } else null
                )

                IconButton(
                    onClick = {
                        if (isGadgetBridgeInstalled) {
                            Log.d("HeartRatePolling", "IconButton clicked, isPolling: $isPolling")
                            if (isPolling) {
                                Log.d("HeartRatePolling", "Stopping polling")
                                pollingJob?.cancel()
                                isPolling = false
                            } else {
                                Log.d("HeartRatePolling", "Starting polling")
                                startPolling()
                            }
                            // Open Gadgetbridge
                            val intent = context.packageManager.getLaunchIntentForPackage(GadgetBridgeHelper.GADGETBRIDGE_PACKAGE)
                            if (intent != null) {
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Please install Gadgetbridge from F-Droid to connect with PineTime",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isPolling && isReceivingData) R.drawable.ic_watch_receiving
                            else if (isGadgetBridgeInstalled) R.drawable.ic_watch_connected
                            else R.drawable.ic_watch_disconnected
                        ),
                        contentDescription = if (isPolling) "Stop monitoring"
                        else if (isGadgetBridgeInstalled) "Start monitoring"
                        else "Install Gadgetbridge",
                        tint = if (isPolling && isReceivingData) Color.Green
                        else if (isGadgetBridgeInstalled) colorResource(id = R.color.light_blue)
                        else Color.Gray
                    )
                }
            }

            if (!isGadgetBridgeInstalled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Install Gadgetbridge to connect with PineTime",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (isGadgetBridgeInstalled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Debug: ${heartRateReadings.size} readings, Last update: ${
                        if (lastHeartRateUpdate > 0)
                            "${(System.currentTimeMillis() - lastHeartRateUpdate) / 1000}s ago"
                        else "never"
                    }",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Duration TextField
            OutlinedTextField(
                value = duration.toString(),
                onValueChange = {
                    try {
                        duration = it.toIntOrNull() ?: 0
                    } catch (e: NumberFormatException) {
                        // Handle invalid input
                    }
                },
                label = { Text("Duration") },
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
                    pollingJob?.cancel() // Stop polling when saving
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = makeAddNewWalkingSessionRequest(duration, averagePulse, password, context)
                        withContext(Dispatchers.Main) {
                            when (result) {
                                is WalkingSessionResult.Success -> {
                                    onSaveChoice(selectedDate)
                                }
                                is WalkingSessionResult.Error -> {
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
                    pollingJob?.cancel() // Stop polling when canceling
                    onCancelChoice(selectedDate)
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

private suspend fun makeAddNewWalkingSessionRequest(
    duration: Int,
    averagePulse: Int,
    password: String?,
    context: Context
): WalkingSessionResult {
    val url = URL("${NetworkConfig.getBaseUrl()}/walking")

    val connection = withContext(Dispatchers.IO) {
        url.openConnection() as HttpURLConnection
    }

    return try {
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")

        // Retrieve the JWT token from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPreferences.getString("access_token", null)
            ?: return WalkingSessionResult.Error("Please login to add walking session")

        // Add the JWT token to the Authorization header
        connection.setRequestProperty("Authorization", "Bearer $jwtToken")
        connection.doOutput = true

        // Create JSON object for the walking session data
        val jsonBody = JSONObject().apply {
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

        when (val responseCode = connection.responseCode) {
            HttpURLConnection.HTTP_CREATED -> {
                WalkingSessionResult.Success
            }
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                WalkingSessionResult.Error("Session expired. Please login again")
            }
            HttpURLConnection.HTTP_BAD_REQUEST -> {
                // Try to get error message from response
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                val errorMessage = errorResponse?.let {
                    try {
                        val jsonError = JSONObject(it)
                        jsonError.getString("message") ?: "Invalid walking session data"
                    } catch (e: Exception) {
                        "Invalid walking session data"
                    }
                } ?: "Invalid walking session data"
                WalkingSessionResult.Error(errorMessage)
            }
            HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                WalkingSessionResult.Error("Server error. Please try again later")
            }
            else -> {
                // Try to get error message from response
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                val errorMessage = errorResponse?.let {
                    try {
                        val jsonError = JSONObject(it)
                        jsonError.getString("message") ?: "Failed to add walking session: $responseCode"
                    } catch (e: Exception) {
                        "Failed to add walking session: $responseCode"
                    }
                } ?: "Failed to add walking session: $responseCode"
                WalkingSessionResult.Error(errorMessage)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        val errorMessage = when (e) {
            is java.net.ConnectException -> "Could not connect to server"
            is java.net.SocketTimeoutException -> "Connection timed out"
            is java.net.UnknownHostException -> "No internet connection"
            is java.lang.NumberFormatException -> "Invalid number format for duration or pulse"
            else -> "Error adding walking session: ${e.localizedMessage}"
        }
        WalkingSessionResult.Error(errorMessage)
    } finally {
        connection.disconnect()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun PreviewNewWalkingSessionScreen() {
    NewWalkingSessionScreen(
        onSaveChoice = {},
        onCancelChoice = {}
    )
}