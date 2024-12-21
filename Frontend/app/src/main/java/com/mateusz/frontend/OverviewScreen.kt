package com.mateusz.frontend

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    var sevenDaysData by remember { mutableStateOf<Map<String, List<Map<String, Any>>>?>(null) }

    var stepsCount by remember { mutableIntStateOf(0) }
    var lastSentStepCount by remember { mutableIntStateOf(-1) }
    var currentPulse by remember { mutableIntStateOf(0) }
    var isReceivingData by remember { mutableStateOf(false) }

    // Function to handle cleanup and navigation
    val handleNavigation = { navigateAction: () -> Unit ->
        BluetoothMeasurementsManager.stopMonitoring()
        isReceivingData = false
        navigateAction()
    }

    DisposableEffect(Unit) {
        BluetoothMeasurementsManager.startMonitoring(
            context,
            onHeartRateReceived = { heartRate ->
                currentPulse = heartRate
                isReceivingData = true
            },
            onStepCountReceived = { steps ->
                stepsCount = steps
                // Only make request if steps count has changed
                if (steps != lastSentStepCount) {
                    lastSentStepCount = steps
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = makeUpdateStepsRequest(stepsCount, context)
                        withContext(Dispatchers.Main) {
                            when (result) {
                                is StepsUpdateResult.Success -> {

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
                        val fetchResult = fetchOverviewData(
                            context, selectedDate?.format(DateTimeFormatter.ISO_DATE).toString()
                        )
                        overviewData = fetchResult as Map<String, Any>?

                    }
                }
            }
        )

        onDispose {
            BluetoothMeasurementsManager.stopMonitoring()
            isReceivingData = false
            lastSentStepCount = -1  // Reset the last sent count
        }
    }

    // Fetch overview data when the screen is first composed
    LaunchedEffect(Unit) {
        val result = fetchOverviewData(
            context, selectedDate?.format(DateTimeFormatter.ISO_DATE).toString()
        )
        overviewData = result as Map<String, Any>?

        val res = fetchSevenDaysData(
            context, selectedDate?.format(DateTimeFormatter.ISO_DATE)
        )
        sevenDaysData = res
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

                IconButton(onClick = { handleNavigation(onViewProfileChoice) }) {
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
                                        handleNavigation(onLogOutChoice)
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .width(300.dp)
                    .clickable {
                        handleNavigation(onEditStepsChoice)
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

            Spacer(modifier = Modifier.height(8.dp))

            ActivityChart(sevenDaysData)

            Spacer(modifier = Modifier.height(8.dp))

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
                        handleNavigation { onWalkingSessionsChoice(selectedDate) }
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                overviewData?.let { data ->
                    if (data["walkingAvgPulse"] != null && data["walkingDuration"] != null) {
                        HealthCard(data["walkingAvgPulse"], "BPM", "Pulse")
                        HealthCard(data["walkingDuration"], "Training", "Duration")
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
                        handleNavigation { onRunningSessionsChoice(selectedDate) }
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                overviewData?.let { data ->
                    if (data["runningAvgPulse"] != null && data["runningDuration"] != null) {
                        HealthCard(data["runningAvgPulse"], "BPM", "Pulse")
                        HealthCard(data["runningDuration"], "Training", "Duration")
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
                        handleNavigation { onCyclingSessionsChoice(selectedDate) }
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                overviewData?.let { data ->
                    if (data["cyclingAvgPulse"] != null && data["cyclingDuration"] != null) {
                        HealthCard(data["cyclingAvgPulse"], "BPM", "Pulse")
                        HealthCard(data["cyclingDuration"], "Training", "Duration")
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

private suspend fun fetchSevenDaysData(
    context: Context,
    selectedDate: String? = null
): Map<String, List<Map<String, Any>>>? {
    return withContext(Dispatchers.IO) {
        val url = URL("${NetworkConfig.getBaseUrl()}/activity-list-seven-days")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")

            // Retrieve JWT token from SharedPreferences
            val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val jwtToken = sharedPreferences.getString("access_token", null)
                ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Please login to view data", Toast.LENGTH_LONG).show()
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
                    parseSevenDaysData(responseText)
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Session expired. Please login again", Toast.LENGTH_LONG).show()
                    }
                    null
                }
                HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No data found for selected date range", Toast.LENGTH_SHORT).show()
                    }
                    null
                }
                else -> {
                    val errorStream = connection.errorStream
                    val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                    val errorMessage = errorResponse?.let {
                        try {
                            val jsonError = JSONObject(it)
                            jsonError.getString("message")
                        } catch (e: Exception) {
                            "Failed to fetch data: $responseCode"
                        }
                    } ?: "Failed to fetch data: $responseCode"

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
                else -> "Error fetching data: ${e.localizedMessage}"
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

private fun parseSevenDaysData(response: String): Map<String, List<Map<String, Any>>>? {
    return try {
        val jsonResponse = JSONObject(response)
        val activities = jsonResponse.getJSONArray("activities")
        val dateRange = jsonResponse.getJSONObject("date_range")

        val activityList = mutableListOf<Map<String, Any>>()

        // Parse each day's data
        for (i in 0 until activities.length()) {
            val dayData = activities.getJSONObject(i)
            activityList.add(
                mapOf(
                    "date" to dayData.getString("date"),
                    "duration" to dayData.getInt("duration"),
                    "steps" to dayData.getInt("steps")
                )
            )
        }

        mapOf(
            "activities" to activityList,
            "dateRange" to listOf(
                mapOf(
                    "startDate" to dateRange.getString("start_date"),
                    "endDate" to dateRange.getString("end_date")
                )
            )
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private suspend fun makeUpdateStepsRequest(
    count: Int?,
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
            count?.let { put("count", it) }
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
                        jsonError.getString("message") ?: "Invalid steps count"
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
                text = if (description == "Duration") formatDuration(value as Int) else value.toString(),
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

@SuppressLint("MissingColorAlphaChannel")
@Composable
fun ActivityChart(
    data: Map<String, List<Map<String, Any>>>?,
    negativeActivity: String? = null
) {
    val activities = data?.get("activities") ?: return

    // Define chart dimensions
    val chartHeight = 120.dp

    val durationData = activities.map { dayData ->
        val durationInSeconds = dayData["duration"] as Int
        val date = dayData["date"] as String
        durationInSeconds to date
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFCAF0F8)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Activity",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF424242),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
                // Horizontal grid lines
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 56.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF424242))
                        )
                    }
                }

                // Y-axis labels
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val yLabels = listOf("3h+", "2h", "1h", "0")
                    yLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF757575),
                            modifier = Modifier.offset(x = (-8).dp)
                        )
                    }
                }

                // Chart bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(start = 64.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    durationData.forEach { (durationInSeconds, date) ->
                        val barHeight = when {
                            durationInSeconds >= 10800 -> chartHeight
                            durationInSeconds > 0 -> {
                                val ratio = when {
                                    durationInSeconds >= 7200 -> {
                                        val progress = (durationInSeconds - 7200) / 3600f
                                        2f / 3f + (progress / 3f)
                                    }
                                    durationInSeconds >= 3600 -> {
                                        val progress = (durationInSeconds - 3600) / 3600f
                                        1f / 3f + (progress / 3f)
                                    }
                                    else -> {
                                        durationInSeconds / 3600f / 3f
                                    }
                                }
                                (ratio * chartHeight.value).dp
                            }
                            else -> 0.dp
                        }

                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(barHeight)
                                .background(
                                    color = colorResource(id = R.color.light_blue),
                                    shape = if (date == negativeActivity) {
                                        RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                                    } else {
                                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                    }
                                )
                        )
                    }
                }
            }

            // Date labels in separate row below the chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 72.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                durationData.forEach { (_, date) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = date.substring(8..9),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF757575)
                        )
                        Text(
                            text = when (date.substring(5..6)) {
                                "01" -> "Jan"
                                "02" -> "Feb"
                                "03" -> "Mar"
                                "04" -> "Apr"
                                "05" -> "May"
                                "06" -> "Jun"
                                "07" -> "Jul"
                                "08" -> "Aug"
                                "09" -> "Sep"
                                "10" -> "Oct"
                                "11" -> "Nov"
                                "12" -> "Dec"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
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

@Preview
@Composable
fun NoActivityChart() {
    val sampleData = mapOf(
        "activities" to listOf(
            mapOf(
                "date" to "2024-12-15",
                "duration" to 5220,  // 1h 27min = 87min = 1.45h
                "steps" to 1880
            ),
            mapOf(
                "date" to "2024-12-16",
                "duration" to 2700,  // 45min = 0.75h
                "steps" to 2500
            ),
            mapOf(
                "date" to "2024-12-17",
                "duration" to 10800,  // 3h = 180min = 3.0h
                "steps" to 3200
            ),
            mapOf(
                "date" to "2024-12-18",
                "duration" to 3900,  // 1h 5min = 65min = 1.08h
                "steps" to 2800
            ),
            mapOf(
                "date" to "2024-12-19",
                "duration" to 5700,  // 1h 35min = 95min = 1.58h
                "steps" to 4100
            ),
            mapOf(
                "date" to "2024-12-20",
                "duration" to 2100,  // 35min = 0.58h
                "steps" to 1500
            ),
            mapOf(
                "date" to "2024-12-21",
                "duration" to 4500,  // 1h 15min = 75min = 1.25h
                "steps" to 2900
            )
        ),
        "dateRange" to listOf(
            mapOf(
                "startDate" to "2024-12-15",
                "endDate" to "2024-12-21"
            )
        )
    )

    ActivityChart(data = sampleData)
}