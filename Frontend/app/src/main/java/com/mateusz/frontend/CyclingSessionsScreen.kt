package com.mateusz.frontend

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.net.ssl.HttpsURLConnection

@Composable
fun CyclingSessionsScreen(
    onOverviewChoice: () -> Unit,
    onAddNewCyclingSessionChoice: () -> Unit,
    selectedDate: LocalDate? = null
) {
    val context = LocalContext.current
    var cyclingSessions by remember { mutableStateOf<List<Map<String, Any>>?>(null) }
    var cyclingSessionCount = 0

    LaunchedEffect(selectedDate) {
        val formattedDate = selectedDate?.format(DateTimeFormatter.ISO_DATE)
        val result = fetchCyclingSessions(context, formattedDate)
        cyclingSessions = result
        cyclingSessionCount = 0
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.20f)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onOverviewChoice() },
                        modifier = Modifier
                            .weight(0.2f)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Text(
                        text = "Cycling sessions",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(0.8f)
                    )
                }
            }

            // Content section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.60f)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!cyclingSessions.isNullOrEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            cyclingSessions?.forEach { session ->
                                cyclingSessionCount++
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CyclingSessionCard(
                                            count = cyclingSessionCount,
                                            pulse = session["average_pulse"] as Int,
                                            duration = session["duration"] as Int
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        NoCyclingSessionCard()
                    }
                }
            }

            // Bottom section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.20f)
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                val selectedDateStr = selectedDate?.format(DateTimeFormatter.ISO_DATE)

                if (selectedDateStr == today) {
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
                                text = "Start new session",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFF424242),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                            )

                            FloatingActionButton(
                                onClick = {
                                    onAddNewCyclingSessionChoice()
                                },
                                containerColor = colorResource(id = R.color.light_blue),
                                contentColor = colorResource(id = R.color.white)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add new cycling session",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CyclingSessionCard(
    count: Int,
    pulse: Int,
    duration: Int,
    modifier: Modifier = Modifier
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
        modifier = modifier
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
                Text(
                    text = "Session $count",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF424242),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

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
                    Text(
                        text = pulse.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    )
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
                    Text(
                        text = formatDuration(duration),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    )
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

@Composable
fun NoCyclingSessionCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                Text(
                    text = "No sessions found",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF424242),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

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
                    Text(
                        text = "-",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    )
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
                    Text(
                        text = "-",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    )
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

private suspend fun fetchCyclingSessions(
    context: Context,
    selectedDate: String? = null
): List<Map<String, Any>>? {
    val TAG = "CyclingAPI"

    return withContext(Dispatchers.IO) {
        val dateStr = selectedDate ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val url = URL("${NetworkConfig.getBaseUrl()}/cycling/$dateStr")
        Log.d(TAG, "Fetching cycling sessions from: $url")

        val connection = try {
            url.openConnection() as HttpsURLConnection
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create connection", e)
            return@withContext null
        }

        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json")

            // Get and verify token
            val jwtToken = TokenManager.getAccessToken(context)
            Log.d(TAG, "JWT token retrieved: ${jwtToken?.take(10) ?: "null"}...")

            if (jwtToken == null) {
                Log.w(TAG, "No JWT token found")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Please login first", Toast.LENGTH_LONG).show()
                }
                return@withContext null
            }

            connection.setRequestProperty("Authorization", "Bearer $jwtToken")
            Log.d(TAG, "Fetching sessions for date: $dateStr")

            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")

            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "Received response: $responseText")
                    parseCyclingSessions(responseText)
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    Log.w(TAG, "Unauthorized - attempting token refresh")
                    if (TokenManager.refreshToken(context)) {
                        Log.d(TAG, "Token refreshed, retrying request")
                        fetchCyclingSessions(context, selectedDate)
                    } else {
                        Log.w(TAG, "Token refresh failed")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Session expired. Please login again", Toast.LENGTH_LONG).show()
                        }
                        null
                    }
                }
                HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                    Log.e(TAG, "Server returned internal error")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No cycling sessions found for $dateStr", Toast.LENGTH_SHORT).show()
                    }
                    null
                }
                else -> {
                    val errorStream = connection.errorStream
                    val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e(TAG, "Error response: $errorResponse")

                    val errorMessage = try {
                        val jsonError = JSONObject(errorResponse ?: "")
                        jsonError.optString("message", "Failed to fetch sessions: $responseCode")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse error response", e)
                        "Failed to fetch sessions: $responseCode"
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                    null
                }
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is java.net.ConnectException -> {
                    Log.e(TAG, "Connection error", e)
                    "Could not connect to server"
                }
                is java.net.SocketTimeoutException -> {
                    Log.e(TAG, "Timeout error", e)
                    "Connection timed out"
                }
                is java.net.UnknownHostException -> {
                    Log.e(TAG, "No internet connection", e)
                    "No internet connection"
                }
                is javax.net.ssl.SSLHandshakeException -> {
                    Log.e(TAG, "SSL error", e)
                    "SSL certificate verification failed"
                }
                else -> {
                    Log.e(TAG, "Unexpected error", e)
                    e.localizedMessage ?: "Unknown error occurred"
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
            null
        } finally {
            connection.disconnect()
        }
    }
}

private fun parseCyclingSessions(response: String): List<Map<String, Any>>? {
    return try {
        val cyclingSessions = mutableListOf<Map<String, Any>>()
        val jsonArray = JSONArray(response)

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val session = mapOf(
                "average_pulse" to jsonObject.getInt("average_pulse"),
                "date" to jsonObject.getString("date"),
                "duration" to jsonObject.getInt("duration"),
                "training_id" to jsonObject.getJSONObject("training").getInt("training_id"),
                "cycling_id" to jsonObject.getInt("cycling_id")
            )
            cyclingSessions.add(session)
        }

        cyclingSessions
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCyclingSessionsScreen() {
    CyclingSessionsScreen(
        onOverviewChoice = {},
        onAddNewCyclingSessionChoice = {}
    )
}
