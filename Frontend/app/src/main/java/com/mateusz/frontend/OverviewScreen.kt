package com.mateusz.frontend

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

sealed class LogoutResult {
    data object Success : LogoutResult()
    data class Error(val message: String) : LogoutResult()
}

@SuppressLint("NewApi")
@Composable
fun OverviewScreen(
    onViewProfileChoice: () -> Unit,
    onLogOutChoice: () -> Unit,
    onEditStepsChoice: () -> Unit,
    onWalkingSessionsChoice: (LocalDate?) -> Unit,
    onRunningSessionsChoice: (LocalDate?) -> Unit,
    onCyclingSessionsChoice: (LocalDate?) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var overviewData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    val calendar = Calendar.getInstance()

    // Fetch overview data when the screen is first composed
    LaunchedEffect(Unit) {
        val result = fetchOverviewData(
            context, selectedDate?.format(DateTimeFormatter.ISO_DATE).toString()
        )
        overviewData = result as Map<String, Any>?
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Profile Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                overviewData?.let { data ->
                    val userName = data["userName"] as? String ?: "unknown"
                    Text(
                        text = "Hi, $userName",
                        textAlign = TextAlign.Center,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(end = 16.dp)
                    )
                } ?: Text(
                    text = "Hi, unknown",
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(end = 16.dp)
                )

                IconButton(onClick = { onViewProfileChoice() }) {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = "Calendar"
                    )
                }

                val datePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                            coroutineScope.launch {
                                val result = fetchOverviewData(
                                    context,
                                    selectedDate?.format(DateTimeFormatter.ISO_DATE)
                                )
                                overviewData = result as Map<String, Int>?
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                }

                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Calendar"
                    )
                }

                IconButton(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = makeLogoutRequest(context)
                            withContext(Dispatchers.Main) {
                                when (result) {
                                    is LogoutResult.Success -> {
                                        Toast.makeText(
                                            context,
                                            "Logout successful",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onLogOutChoice()
                                    }
                                    is LogoutResult.Error -> {
                                        Toast.makeText(
                                            context,
                                            result.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout"
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Your Daily Health Statistics",
                fontSize = 14.sp,
                color = Color.Gray,
            )
            Text(
                text = "$selectedDate",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .width(300.dp)
                    .clickable {
                        onEditStepsChoice()
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                overviewData?.let { data ->
                    if (data["steps"] != null && data["stepsGoal"] != null) {
                        val steps = (data["steps"] as? Number)?.toInt() ?: 0
                        val stepsGoal = (data["stepsGoal"] as? Number)?.toInt() ?: 0

                        StepsProgressBar(steps, stepsGoal)
                    } else {
                        StepsProgressBar(0, 0)
                    }
                } ?: StepsProgressBar(0, 0)

            }

            Spacer(modifier = Modifier.height(24.dp))

            // Walking
            Text(
                text = "Walking",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier
                    .width(300.dp)
                    .clickable {
                        onWalkingSessionsChoice(selectedDate)
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                overviewData?.let { data ->
                    if (data["walkingAvgPulse"] != null && data["walkingDuration"] != null) {
                        HealthCard(data["walkingAvgPulse"], "BPM", "Pulse")
                        HealthCard(data["walkingDuration"], "Minutes", "Training")
                    } else {
                        NoActivityCard(activity = "walking")
                    }
                } ?: NoActivityCard(activity = "walking")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Running
            Text(
                text = "Running",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier
                    .width(300.dp)
                    .clickable {
                        onRunningSessionsChoice(selectedDate)
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                overviewData?.let { data ->
                    if (data["runningAvgPulse"] != null && data["runningDuration"] != null) {
                        HealthCard(data["runningAvgPulse"], "BPM", "Pulse")
                        HealthCard(data["runningDuration"], "Minutes", "Training")
                    } else {
                        NoActivityCard(activity = "running")
                    }
                } ?: NoActivityCard(activity = "running")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cycling
            Text(
                text = "Cycling",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier
                    .width(300.dp)
                    .clickable {
                        onCyclingSessionsChoice(selectedDate)
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                overviewData?.let { data ->
                    if (data["cyclingAvgPulse"] != null && data["cyclingDuration"] != null) {
                        HealthCard(data["cyclingAvgPulse"], "BPM", "Pulse")
                        HealthCard(data["cyclingDuration"], "Minutes", "Training")
                    } else {
                        NoActivityCard(activity = "cycling")
                    }
                } ?: NoActivityCard(activity = "cycling")
            }
        }
    }
}


private suspend fun fetchOverviewData(
    context: Context,
    selectedDate: String? = null
): Map<String, Any?>? {
    return withContext(Dispatchers.IO) {
        val url = URL("${NetworkConfig.getBaseUrl()}/activity-list")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")

            // Retrieve JWT token from SharedPreferences
            val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val jwtToken = sharedPreferences.getString("access_token", null)
                ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Please login to view overview", Toast.LENGTH_LONG).show()
                    }
                    return@withContext null
                }

            connection.setRequestProperty("Authorization", "Bearer $jwtToken")
            connection.doOutput = true

            // Use the selected date or default to current date
            val dateToUse = selectedDate ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE)

            // Create JSON payload with the date
            val jsonPayload = JSONObject().apply {
                put("date", dateToUse)
            }

            OutputStreamWriter(connection.outputStream).use { it.write(jsonPayload.toString()) }

            // Process response
            when (val responseCode = connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    parseOverviewData(responseText)
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Session expired. Please login again", Toast.LENGTH_LONG).show()
                    }
                    null
                }
                HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No data found for selected date", Toast.LENGTH_SHORT).show()
                    }
                    null
                }
                else -> {
                    // Try to get error message from response
                    val errorStream = connection.errorStream
                    val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                    val errorMessage = errorResponse?.let {
                        try {
                            val jsonError = JSONObject(it)
                            jsonError.getString("message")
                        } catch (e: Exception) {
                            "Failed to fetch overview data: $responseCode"
                        }
                    } ?: "Failed to fetch overview data: $responseCode"

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                    null
                }
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is java.net.ConnectException -> "Could not connect to server"
                is java.net.SocketTimeoutException -> "Connection timed out"
                is java.net.UnknownHostException -> "No internet connection"
                else -> "Error fetching overview data: ${e.localizedMessage}"
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
            e.printStackTrace()
            null
        } finally {
            connection.disconnect()
        }
    }
}

private fun parseOverviewData(response: String): Map<String, Any?>? {
    return try {
        val jsonResponse = JSONObject(response)
        val activity = jsonResponse.optJSONObject("activity") ?: return null

        // Helper function to safely get value from array
        fun getValueFromArray(arrayName: String, key: String): Int? {
            return if ((activity.optJSONArray(arrayName)?.length() ?: 0) > 0) {
                activity.optJSONArray(arrayName)?.optJSONObject(0)?.optInt(key)
            } else null
        }

        mapOf(
            // User data - now we have user array in the response
            "userName" to if ((activity.optJSONArray("user")?.length() ?: 0) > 0) {
                activity.optJSONArray("user")?.optJSONObject(0)?.optString("name")
            } else null,

            // Steps data
            "steps" to getValueFromArray("steps", "count"),
            "stepsGoal" to getValueFromArray("steps", "goal"),

            // Walking data
            "walkingDuration" to getValueFromArray("walking", "duration"),
            "walkingAvgPulse" to getValueFromArray("walking", "average_pulse"),

            // Running data - empty array in response will return null
            "runningDuration" to getValueFromArray("running", "duration"),
            "runningAvgPulse" to getValueFromArray("running", "average_pulse"),

            // Cycling data - empty array in response will return null
            "cyclingDuration" to getValueFromArray("cycling", "duration"),
            "cyclingAvgPulse" to getValueFromArray("cycling", "average_pulse")
        )
    } catch (e: Exception) {
        null
    }
}

private suspend fun makeLogoutRequest(context: Context): LogoutResult {
    val url = URL("${NetworkConfig.getBaseUrl()}/logout")
    val connection = withContext(Dispatchers.IO) {
        url.openConnection() as HttpURLConnection
    }

    return try {
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")

        // Get the token
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPreferences.getString("access_token", null)
            ?: return LogoutResult.Success.also {
                clearUserData(context)
            }

        // Add the JWT token to the Authorization header WITH "Bearer " prefix
        connection.setRequestProperty("Authorization", "Bearer $jwtToken") // Make sure there's a space after "Bearer"

        when (connection.responseCode) {
            HttpURLConnection.HTTP_OK -> {
                clearUserData(context)
                LogoutResult.Success
            }
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                clearUserData(context)
                // Maybe add debug logging here
                println("Unauthorized error. Token: $jwtToken")
                LogoutResult.Success
            }
            else -> {
                clearUserData(context)
                LogoutResult.Success
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        clearUserData(context)
        LogoutResult.Success
    } finally {
        connection.disconnect()
    }
}

private fun isTokenValid(token: String): Boolean {
    return try {
        val parts = token.split(".")
        parts.size == 3
    } catch (e: Exception) {
        false
    }
}

private suspend fun clearUserData(context: Context) {
    withContext(Dispatchers.IO) {
        context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}

@Composable
fun StepsProgressBar(steps: Int, stepsGoal: Int) {
    Column(
        modifier = Modifier
            .width(320.dp)
            .padding(32.dp)
            .height(70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Steps progress",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        // Calculate progress as a fraction (steps / stepsGoal)
        val progress = if (stepsGoal > 0) steps / stepsGoal.toFloat() else 0f

        LinearProgressIndicator(
            progress = progress,
            color = colorResource(id = R.color.light_blue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        Text(text = "$steps out of $stepsGoal steps")
    }
}

@Composable
fun HealthCard(value: Any?, unit: String?, description: String?) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .padding(top = 4.dp, start = 8.dp, end = 8.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.light_blue),
            contentColor = colorResource(id = R.color.white)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit.toString(),
                fontSize = 14.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = description.toString(),
                fontSize = 12.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun NoActivityCard(
    activity: String,
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .padding(top = 4.dp, start = 8.dp, end = 8.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.light_blue),
            contentColor = colorResource(id = R.color.white)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("No $activity sessions found...")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOverviewScreen() {
    OverviewScreen(
        onViewProfileChoice = {},
        onLogOutChoice = {},
        onEditStepsChoice = {},
        onWalkingSessionsChoice = {},
        onRunningSessionsChoice = {},
        onCyclingSessionsChoice = {},
    )
}
