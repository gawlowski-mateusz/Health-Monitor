package com.mateusz.frontend

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

sealed class RunningSessionResult {
    data object Success : RunningSessionResult()
    data class Error(val message: String) : RunningSessionResult()
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NewRunningSessionScreen(
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
    var isProcessing by remember { mutableStateOf(false) }

    // First, get the BluetoothManager
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter

    val isBluetoothEnabled = remember {
        bluetoothAdapter?.isEnabled ?: false
    }

    // Function to handle cleanup and navigation
    val handleNavigation = { date: LocalDate?, navigateAction: (LocalDate?) -> Unit ->
        HeartRateManager.stopHeartRateMonitoring()
        isReceivingData = false
        navigateAction(date)
    }

    DisposableEffect(Unit) {
        HeartRateManager.startHeartRateMonitoring(context) { heartRate ->
            heartRateReadings.add(heartRate)
            averagePulse = heartRateReadings.average().toInt()
            lastHeartRateUpdate = System.currentTimeMillis()
            isReceivingData = true
        }

        onDispose {
            HeartRateManager.stopHeartRateMonitoring()
            isReceivingData = false
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            duration++
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
                text = "New running session",
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
                    value = "$averagePulse BPM",
                    onValueChange = { }, // Empty because we don't want manual changes
                    label = { Text("Average pulse") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.black),
                        focusedLabelColor = colorResource(id = R.color.black),
                        disabledTextColor = Color.Black,
                        disabledBorderColor = colorResource(id = R.color.black),
                        disabledLabelColor = colorResource(id = R.color.black),
                    ),
                    enabled = false,
                )

                IconButton(
                    onClick = {
                        if (!isBluetoothEnabled) {
                            Toast.makeText(context, "Please enable Bluetooth in phone settings", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isReceivingData) R.drawable.ic_watch_receiving
                            else if (isBluetoothEnabled) R.drawable.ic_watch_connected
                            else R.drawable.ic_watch_disconnected
                        ),
                        contentDescription = if (isReceivingData) "Stop monitoring"
                        else "Enable Bluetooth",
                        tint = if (isReceivingData) Color.Green
                        else if (isBluetoothEnabled) colorResource(id = R.color.light_blue)
                        else Color.Gray
                    )
                }
            }

            if (!isBluetoothEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enable Bluetooth to connect with external device",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (isBluetoothEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Data read status: ${heartRateReadings.size} heart rate reading(s)",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Function to format duration
                fun formatDuration(seconds: Int): String {
                    return if (seconds < 60) {
                        "00 minutes %02d seconds".format(seconds)
                    } else {
                        val minutes = seconds / 60
                        val remainingSeconds = seconds % 60
                        "%02d minutes %02d seconds".format(minutes, remainingSeconds)
                    }
                }

                // Duration TextField
                OutlinedTextField(
                    value = formatDuration(duration),
                    onValueChange = { },
                    label = { Text("Duration") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.black),
                        focusedLabelColor = colorResource(id = R.color.black),
                        disabledTextColor = Color.Black,
                        disabledBorderColor = colorResource(id = R.color.black),
                        disabledLabelColor = colorResource(id = R.color.black),
                    ),
                    enabled = false,
                )

                IconButton(
                    onClick = { duration = 0 }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset timer"
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    isProcessing = true
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = makeAddNewRunningSessionRequest(duration, averagePulse, password, context)
                        withContext(Dispatchers.Main) {
                            when (result) {
                                is RunningSessionResult.Success -> {
                                    handleNavigation(selectedDate, onSaveChoice)
                                }
                                is RunningSessionResult.Error -> {
                                    isProcessing = false
                                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                },
                enabled = !isProcessing,
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
                    if (isProcessing) "Processing..." else "Save",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cancel Button
            OutlinedButton(
                onClick = { handleNavigation(selectedDate, onCancelChoice) },
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                border = BorderStroke(2.dp, color = colorResource(id = R.color.light_blue))
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

private suspend fun makeAddNewRunningSessionRequest(
    duration: Int,
    averagePulse: Int,
    password: String?,
    context: Context
): RunningSessionResult {
    val url = URL("${NetworkConfig.getBaseUrl()}/running")

    val connection = withContext(Dispatchers.IO) {
        url.openConnection() as HttpURLConnection
    }

    return try {
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")

        // Retrieve the JWT token from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPreferences.getString("access_token", null)
            ?: return RunningSessionResult.Error("Please login to add running session")

        // Add the JWT token to the Authorization header
        connection.setRequestProperty("Authorization", "Bearer $jwtToken")
        connection.doOutput = true

        // Create JSON object for the running session data
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
                RunningSessionResult.Success
            }
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                RunningSessionResult.Error("Session expired. Please login again")
            }
            HttpURLConnection.HTTP_BAD_REQUEST -> {
                // Try to get error message from response
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                val errorMessage = errorResponse?.let {
                    try {
                        val jsonError = JSONObject(it)
                        jsonError.getString("message") ?: "Invalid running session data"
                    } catch (e: Exception) {
                        "Invalid running session data"
                    }
                } ?: "Invalid running session data"
                RunningSessionResult.Error(errorMessage)
            }
            HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                RunningSessionResult.Error("Server error. Please try again later")
            }
            else -> {
                // Try to get error message from response
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                val errorMessage = errorResponse?.let {
                    try {
                        val jsonError = JSONObject(it)
                        jsonError.getString("message") ?: "Failed to add running session: $responseCode"
                    } catch (e: Exception) {
                        "Failed to add running session: $responseCode"
                    }
                } ?: "Failed to add running session: $responseCode"
                RunningSessionResult.Error(errorMessage)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        val errorMessage = when (e) {
            is java.net.ConnectException -> "Could not connect to server"
            is java.net.SocketTimeoutException -> "Connection timed out"
            is java.net.UnknownHostException -> "No internet connection"
            is java.lang.NumberFormatException -> "Invalid number format for duration or pulse"
            else -> "Error adding running session: ${e.localizedMessage}"
        }
        RunningSessionResult.Error(errorMessage)
    } finally {
        connection.disconnect()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun PreviewNewRunningSessionScreen() {
    NewRunningSessionScreen (
        onSaveChoice = {},
        onCancelChoice = {}
    )
}