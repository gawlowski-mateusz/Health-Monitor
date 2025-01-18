package com.mateusz.frontend

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.DirectionsWalk
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import javax.net.ssl.HttpsURLConnection

sealed class LogoutResult {
    data object Success : LogoutResult()
    data class Error(val message: String) : LogoutResult()
}

@Composable
fun ActivitySelectionCard(
    overviewData: Map<String, Any>?,
    selectedActivity: String,
    onActivitySelected: (String) -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activityData = when (selectedActivity) {
        "walking" -> Triple(
            "walkingAvgPulse",
            "walkingDuration"
        ) { onNavigate("walking") }

        "running" -> Triple(
            "runningAvgPulse",
            "runningDuration"
        ) { onNavigate("running") }

        "cycling" -> Triple(
            "cyclingAvgPulse",
            "cyclingDuration"
        ) { onNavigate("cycling") }

        else -> Triple("walkingAvgPulse", "walkingDuration") { onNavigate("walking") }
    }

    overviewData?.let { data ->
        val pulse = data[activityData.first]
        val duration = data[activityData.second]

        if (pulse != null && duration != null) {
            UnifiedActivityCard(
                selectedActivity = selectedActivity,
                onActivitySelected = onActivitySelected,
                pulse = (pulse as Number).toInt(),
                duration = (duration as Number).toInt(),
                modifier = modifier.clickable(onClick = activityData.third)
            )
        } else {
            UnifiedActivityCard(
                selectedActivity = selectedActivity,
                onActivitySelected = onActivitySelected,
                pulse = 0,
                duration = 0,
                modifier = modifier.clickable(onClick = activityData.third)
            )
        }
    } ?: UnifiedActivityCard(
        selectedActivity = selectedActivity,
        onActivitySelected = onActivitySelected,
        pulse = 0,
        duration = 0,
        modifier = modifier.clickable(onClick = activityData.third)
    )
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
    var selectedActivity by remember { mutableStateOf("walking") }

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
                text = "$selectedDate",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
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

            ActivityChart(sevenDaysData)

            ActivitySelectionCard(
                overviewData = overviewData,
                selectedActivity = selectedActivity,
                onActivitySelected = { newActivity ->
                    selectedActivity = newActivity
                },
                onNavigate = { activity ->
                    when (activity) {
                        "walking" -> handleNavigation { onWalkingSessionsChoice(selectedDate) }
                        "running" -> handleNavigation { onRunningSessionsChoice(selectedDate) }
                        "cycling" -> handleNavigation { onCyclingSessionsChoice(selectedDate) }
                    }
                }
            )
        }
    }
}

@Composable
fun StepsProgressBar(
    steps: Int,
    stepsGoal: Int,
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Steps progress",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )

            // Calculate progress as a fraction
            val progress = if (stepsGoal > 0) steps / stepsGoal.toFloat() else 0f

            LinearProgressIndicator(
                progress = progress,
                color = Color(0xFF48CAE4),
                trackColor = Color(0xFFADE8F4),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Text(
                text = "$steps out of $stepsGoal",
                fontSize = 16.sp,
                color = Color(0xFF424242),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun UnifiedActivityCard(
    selectedActivity: String,
    onActivitySelected: (String) -> Unit,
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
            // Activity Icons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActivityIcon(
                    icon = Icons.Outlined.DirectionsWalk,
                    isSelected = selectedActivity == "walking",
                    onClick = { onActivitySelected("walking") }
                )
                ActivityIcon(
                    icon = Icons.Outlined.DirectionsRun,
                    isSelected = selectedActivity == "running",
                    onClick = { onActivitySelected("running") }
                )
                ActivityIcon(
                    icon = Icons.Outlined.DirectionsBike,
                    isSelected = selectedActivity == "cycling",
                    onClick = { onActivitySelected("cycling") }
                )
            }

            // Stats Row
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

@Composable
private fun ActivityIcon(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) Color(0xFF48CAE4)
                else Color(0xFF424242)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
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

private suspend fun fetchOverviewData(
    context: Context,
    selectedDate: String? = null
): Map<String, Any?>? {
    val TAG = "OverviewAPI"

    return withContext(Dispatchers.IO) {
        val url = URL("${NetworkConfig.getBaseUrl()}/activity-list")
        Log.d(TAG, "Fetching overview data from: $url")

        val connection = try {
            url.openConnection() as HttpsURLConnection
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create connection", e)
            return@withContext null
        }

        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")

            // Get and verify token
            val jwtToken = TokenManager.getAccessToken(context)
            Log.d(TAG, "JWT token retrieved: ${jwtToken?.take(10) ?: "null"}...")

            if (jwtToken == null) {
                Log.w(TAG, "No JWT token found")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Please login to view overview", Toast.LENGTH_LONG).show()
                }
                return@withContext null
            }

            connection.setRequestProperty("Authorization", "Bearer $jwtToken")
            connection.doOutput = true

            // Prepare request data
            val dateToUse = selectedDate ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val jsonPayload = JSONObject().apply {
                put("date", dateToUse)
            }
            Log.d(TAG, "Sending request for date: $dateToUse")

            // Send request
            withContext(Dispatchers.IO) {
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonPayload.toString())
                    writer.flush()
                }
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")

            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "Received response: $responseText")
                    parseOverviewData(responseText)
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    Log.w(TAG, "Unauthorized - attempting token refresh")
                    if (TokenManager.refreshToken(context)) {
                        Log.d(TAG, "Token refreshed, retrying request")
                        fetchOverviewData(context, selectedDate)
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
                        Toast.makeText(context, "No data found for selected date", Toast.LENGTH_SHORT).show()
                    }
                    null
                }
                else -> {
                    val errorStream = connection.errorStream
                    val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e(TAG, "Error response: $errorResponse")

                    val errorMessage = errorResponse?.let {
                        try {
                            JSONObject(it).getString("message")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse error response", e)
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
                    "Error fetching overview data: ${e.localizedMessage}"
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
    val TAG = "SevenDaysAPI"

    return withContext(Dispatchers.IO) {
        val url = URL("${NetworkConfig.getBaseUrl()}/activity-list-seven-days")
        Log.d(TAG, "Fetching seven days data from: $url")

        val connection = try {
            url.openConnection() as HttpsURLConnection
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create connection", e)
            return@withContext null
        }

        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")

            // Get and verify token
            val jwtToken = TokenManager.getAccessToken(context)
            Log.d(TAG, "JWT token retrieved: ${jwtToken?.take(10) ?: "null"}...")

            if (jwtToken == null) {
                Log.w(TAG, "No JWT token found")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Please login to view data", Toast.LENGTH_LONG).show()
                }
                return@withContext null
            }

            connection.setRequestProperty("Authorization", "Bearer $jwtToken")
            connection.doOutput = true

            // Prepare request data
            val dateToUse = selectedDate ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val jsonPayload = JSONObject().apply {
                put("date", dateToUse)
            }
            Log.d(TAG, "Sending request for date: $dateToUse")

            // Send request
            withContext(Dispatchers.IO) {
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonPayload.toString())
                    writer.flush()
                }
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")

            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "Received response: $responseText")
                    parseSevenDaysData(responseText)
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    Log.w(TAG, "Unauthorized - attempting token refresh")
                    if (TokenManager.refreshToken(context)) {
                        Log.d(TAG, "Token refreshed, retrying request")
                        fetchSevenDaysData(context, selectedDate)
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
                        Toast.makeText(context, "No data found for selected date range", Toast.LENGTH_SHORT).show()
                    }
                    null
                }
                else -> {
                    val errorStream = connection.errorStream
                    val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e(TAG, "Error response: $errorResponse")

                    val errorMessage = errorResponse?.let {
                        try {
                            JSONObject(it).getString("message")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse error response", e)
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
                    "Error fetching data: ${e.localizedMessage}"
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

suspend fun makeUpdateStepsRequest(
    count: Int?,
    context: Context,
    testConnection: HttpsURLConnection? = null
): StepsUpdateResult {
    val TAG = "StepsAPI"

    val url = URL("${NetworkConfig.getBaseUrl()}/steps/goal")
    Log.d(TAG, "Making steps update request to: $url")

    val connection = testConnection ?: try {
        withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpsURLConnection
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create connection", e)
        return StepsUpdateResult.Error("Could not connect to server")
    }

    return try {
        connection.requestMethod = "PATCH"
        connection.setRequestProperty("Content-Type", "application/json")

        // Get and verify token
        val jwtToken = TokenManager.getAccessToken(context)
        Log.d(TAG, "JWT token retrieved: ${jwtToken?.take(10) ?: "null"}...")

        if (jwtToken == null) {
            Log.w(TAG, "No JWT token found")
            return StepsUpdateResult.Error("Please login to update steps goal")
        }

        connection.setRequestProperty("Authorization", "Bearer $jwtToken")
        connection.doOutput = true

        val jsonBody = JSONObject().apply {
            count?.let { put("count", it) }
        }
        Log.d(TAG, "Sending update data: $jsonBody")

        withContext(Dispatchers.IO) {
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }
        }

        val responseCode = connection.responseCode
        Log.d(TAG, "Response code: $responseCode")

        when (responseCode) {
            HttpURLConnection.HTTP_CREATED -> {
                Log.d(TAG, "Steps goal updated successfully")
                StepsUpdateResult.Success
            }

            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                Log.w(TAG, "Unauthorized - attempting token refresh")
                if (TokenManager.refreshToken(context)) {
                    Log.d(TAG, "Token refreshed, retrying request")
                    makeUpdateStepsRequest(count, context)
                } else {
                    Log.w(TAG, "Token refresh failed")
                    StepsUpdateResult.Error("Session expired. Please login again")
                }
            }

            HttpURLConnection.HTTP_BAD_REQUEST -> {
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "Bad request error: $errorResponse")

                val errorMessage = errorResponse?.let {
                    try {
                        JSONObject(it).getString("message") ?: "Invalid steps count"
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse error response", e)
                        "Invalid steps goal"
                    }
                } ?: "Invalid steps goal"

                StepsUpdateResult.Error(errorMessage)
            }

            else -> {
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "Unexpected error response: $errorResponse")

                val errorMessage = errorResponse?.let {
                    try {
                        JSONObject(it).getString("message")
                            ?: "Failed to update steps goal: $responseCode"
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse error response", e)
                        "Failed to update steps goal: $responseCode"
                    }
                } ?: "Failed to update steps goal: $responseCode"

                StepsUpdateResult.Error(errorMessage)
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
                "Error updating steps goal: ${e.localizedMessage}"
            }
        }
        StepsUpdateResult.Error(errorMessage)
    } finally {
        connection.disconnect()
    }
}

private suspend fun makeLogoutRequest(context: Context): LogoutResult {
    val TAG = "LogoutAPI"

    val url = URL("${NetworkConfig.getBaseUrl()}/logout")
    Log.d(TAG, "Making logout request to: $url")

    val connection = try {
        withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpsURLConnection
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create connection", e)
        clearUserData(context)
        return LogoutResult.Success
    }

    return try {
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")

        val jwtToken = TokenManager.getAccessToken(context)
        Log.d(TAG, "JWT token retrieved: ${jwtToken?.take(10) ?: "null"}...")

        if (jwtToken == null) {
            Log.w(TAG, "No JWT token found")
            clearUserData(context)
            return LogoutResult.Success
        }

        connection.setRequestProperty("Authorization", "Bearer $jwtToken")

        val responseCode = connection.responseCode
        Log.d(TAG, "Response code: $responseCode")

        when (responseCode) {
            HttpURLConnection.HTTP_OK -> {
                Log.d(TAG, "Logout successful")
                clearUserData(context)
                LogoutResult.Success
            }

            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                Log.w(TAG, "Unauthorized during logout")
                clearUserData(context)
                LogoutResult.Success
            }

            else -> {
                Log.w(TAG, "Unexpected response during logout: $responseCode")
                clearUserData(context)
                LogoutResult.Success
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
                "Error during logout: ${e.localizedMessage}"
            }
        }
        Log.e(TAG, "Logout error: $errorMessage", e)
        clearUserData(context)
        LogoutResult.Success
    } finally {
        connection.disconnect()
    }
}

private suspend fun clearUserData(context: Context) {
    Log.d("TokenManager", "Clearing user data")
    withContext(Dispatchers.IO) {
        context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
    Log.d("TokenManager", "User data cleared")
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

@Preview(showBackground = true)
@Composable
fun ActivityIconsPreview() {
    MaterialTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActivityIcon(
                    icon = Icons.Outlined.DirectionsWalk,
                    isSelected = true,
                    onClick = {}
                )
                ActivityIcon(
                    icon = Icons.Outlined.DirectionsRun,
                    isSelected = false,
                    onClick = {}
                )
                ActivityIcon(
                    icon = Icons.Outlined.DirectionsBike,
                    isSelected = false,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UnifiedActivityCardPreview() {
    Surface {
        UnifiedActivityCard(
            selectedActivity = "walking",
            onActivitySelected = {},
            pulse = 85,
            duration = 4,
            modifier = Modifier.padding(16.dp)
        )
    }
}