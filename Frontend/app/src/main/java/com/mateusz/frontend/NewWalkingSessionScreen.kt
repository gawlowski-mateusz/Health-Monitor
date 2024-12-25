package com.mateusz.frontend

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.HostnameVerifier

sealed class WalkingSessionResult {
    data object Success : WalkingSessionResult()
    data class Error(val message: String) : WalkingSessionResult()
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
    var isProcessing by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    // First, get the BluetoothManager
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter

    val isBluetoothEnabled = remember {
        bluetoothAdapter?.isEnabled ?: false
    }

    // Function to handle cleanup and navigation
    val handleNavigation = { date: LocalDate?, navigateAction: (LocalDate?) -> Unit ->
        BluetoothMeasurementsManager.stopMonitoring()
        isReceivingData = false
        navigateAction(date)
    }

    DisposableEffect(Unit) {
        BluetoothMeasurementsManager.startMonitoring(
            context,
            onHeartRateReceived = { heartRate ->
                heartRateReadings.add(heartRate)
                averagePulse = heartRateReadings.average().toInt()
                lastHeartRateUpdate = System.currentTimeMillis()
                isReceivingData = true
            },
            onStepCountReceived = { }
        )

        onDispose {
            BluetoothMeasurementsManager.stopMonitoring()
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
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        showConfirmationDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    if (showConfirmationDialog) {
                        Dialog(
                            onDismissRequest = { showConfirmationDialog = false }
                        ) {
                            Card(
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFCAF0F8)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Are you sure you want to give up?",
                                        style = MaterialTheme.typography.titleLarge,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF424242),
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        // No Button
                                        Button(
                                            onClick = { showConfirmationDialog = false },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colorResource(id = R.color.light_blue)
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text("No")
                                        }

                                        // Yes Button
                                        Button(
                                            onClick = {
                                                showConfirmationDialog = false
                                                handleNavigation(selectedDate, onCancelChoice)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colorResource(id = R.color.light_blue)
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text("Yes")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "New Walking Session",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        isProcessing = true
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = makeAddNewWalkingSessionRequest(duration, averagePulse, password, context)
                            withContext(Dispatchers.Main) {
                                when (result) {
                                    is WalkingSessionResult.Success -> {
                                        handleNavigation(selectedDate, onSaveChoice)
                                    }
                                    is WalkingSessionResult.Error -> {
                                        isProcessing = false
                                        Toast.makeText(
                                            context,
                                            result.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Edit"
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    fun formatDuration(seconds: Int): String {
                        if (seconds < 60) {
                            return "00:00:%02d".format(seconds)
                        }

                        val hours = seconds / 3600
                        val remainingSecondsAfterHours = seconds % 3600
                        val minutes = remainingSecondsAfterHours / 60
                        val remainingSeconds = remainingSecondsAfterHours % 60

                        return if (hours > 0) {
                            "%02d:%02d:%02d".format(hours, minutes, remainingSeconds)
                        } else {
                            "%02d:%02d:%02d".format(0, minutes, remainingSeconds)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFCAF0F8)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Pulse Section
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = averagePulse.toString(),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF424242)
                                        )
                                    }

                                    Text(
                                        text = "BPM",
                                        fontSize = 12.sp,
                                        color = Color(0xFF424242)
                                    )
                                    Text(
                                        text = "Pulse",
                                        fontSize = 12.sp,
                                        color = Color(0xFF424242)
                                    )
                                }

                                // Vertical Divider
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(80.dp)
                                        .background(Color(0xFF757575))
                                )

                                // Duration Section
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = formatDuration(duration),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF424242)
                                        )
                                    }
                                    Text(
                                        text = "Training",
                                        fontSize = 12.sp,
                                        color = Color(0xFF424242)
                                    )
                                    Text(
                                        text = "Duration",
                                        fontSize = 12.sp,
                                        color = Color(0xFF424242)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFCAF0F8)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bluetooth Info",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF424242),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )

                        FloatingActionButton(
                            onClick = {
                                if (isReceivingData) {
                                    Toast.makeText(context, "Data read status: \n${heartRateReadings.size} heart rate reading(s)", Toast.LENGTH_LONG).show()
                                } else if (isBluetoothEnabled) {
                                    Toast.makeText(context, "Bluetooth connection enabled.\nWaiting for device to pair...", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Bluetooth turned off.\nPlease enable it in phone settings", Toast.LENGTH_LONG).show()
                                }
                            },
                            containerColor = colorResource(id = R.color.light_blue),
                            contentColor = colorResource(id = R.color.white)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = if (isReceivingData) "Stop monitoring"
                                else "Enable Bluetooth",
                                tint = if (isReceivingData) Color.Green
                                else if (isBluetoothEnabled) Color.Yellow
                                else Color.Red
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFCAF0F8)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reset Timer",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF424242),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )

                        FloatingActionButton(
                            onClick = {
                                duration = 0
                            },
                            containerColor = colorResource(id = R.color.light_blue),
                            contentColor = colorResource(id = R.color.white)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset timer",
                                tint = Color.White
                            )
                        }
                    }
                }
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
        createHttpsConnection(url, context)
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
            is javax.net.ssl.SSLHandshakeException -> "SSL certificate verification failed"
            else -> "Error adding walking session: ${e.localizedMessage}"
        }
        WalkingSessionResult.Error(errorMessage)
    } finally {
        connection.disconnect()
    }
}

// Helper function to create SSL context
private fun createSSLContext(context: Context): SSLContext {
    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    keyStore.load(null, null)

    context.resources.openRawResource(R.raw.cert).use { certInputStream ->
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificate = certificateFactory.generateCertificate(certInputStream)
        keyStore.setCertificateEntry("my_cert", certificate)
    }

    trustManagerFactory.init(keyStore)
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustManagerFactory.trustManagers, SecureRandom())
    return sslContext
}

// Helper function to create HTTPS connection
private fun createHttpsConnection(url: URL, context: Context): HttpsURLConnection {
    return (url.openConnection() as HttpsURLConnection).apply {
        sslSocketFactory = createSSLContext(context).socketFactory
        hostnameVerifier = HostnameVerifier { _, _ -> true }
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