package com.mateusz.frontend

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun WalkingSessionsScreen(
    onOverviewChoice: () -> Unit,
    onAddNewWalkingSessionChoice: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var walkingSessions by remember { mutableStateOf<List<Map<String, Any>>?>(null) }
    var walkingSessionCount = 0

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper 75% scrollable area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.80f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Walking sessions",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(24.dp)
                    )

                    if (!walkingSessions.isNullOrEmpty()) {
                        Column {
                            walkingSessions?.forEach { session ->
                                walkingSessionCount++

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .padding(8.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Session $walkingSessionCount",
                                            fontSize = 24.sp,
                                        )

                                        Row {
                                            WalkingCard(
                                                value = session["average_pulse"] as Int,
                                                unit = "BPM",
                                                description = "Pulse"
                                            )
                                            WalkingCard(
                                                value = session["duration"] as Int,
                                                unit = "Minutes",
                                                description = "Training"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
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
                                Text("No walking sessions found...")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Add walking session Button
                    Button(
                        onClick = {
                            onAddNewWalkingSessionChoice()
                        },
                        modifier = Modifier
                            .width(300.dp)
                            .padding(top = 4.dp, start = 8.dp, end = 8.dp)
                            .height(56.dp),  // Adjust height to match the screenshot
                        shape = RoundedCornerShape(25),  // Rounded corners to match the screenshot
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.light_blue),  // Custom blue background
                            contentColor = colorResource(id = R.color.white)  // White text color
                        )
                    ) {
                        Text(
                            "Add new walking session",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Lower 25% non-scrollable area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.20f)
                    .padding(horizontal = 32.dp, vertical = 16.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Refresh Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val result = fetchWalkingSessions(context)
                                walkingSessions = result
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.light_blue),
                            contentColor = colorResource(id = R.color.white)
                        )
                    ) {
                        Text(
                            "Refresh",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Overview Button
                    OutlinedButton(
                        onClick = {
                            onOverviewChoice()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            2.dp,
                            color = colorResource(id = R.color.light_blue)
                        )
                    ) {
                        Text(
                            "Overview screen",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(id = R.color.light_blue)
                        )
                    }
                }
            }
        }
    }
}


private suspend fun fetchWalkingSessions(context: Context): List<Map<String, Any>>? {
    return withContext(Dispatchers.IO) { // Switch to IO dispatcher for network operation
        val url = URL("http://10.0.2.2:8000/walking") // Replace with your actual API URL
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json")

            // Retrieve the JWT token from SharedPreferences
            val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val jwtToken = sharedPreferences.getString("access_token", null)
                ?: return@withContext null // Return null if token is not found

            // Add the JWT token to the Authorization header
            connection.setRequestProperty("Authorization", "Bearer $jwtToken")

            when (val responseCode = connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    parseWalkingSessions(responseText)
                }
                HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "No walking sessions found...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    null
                }
                else -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Failed to fetch sessions: $responseCode",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection.disconnect()
        }
    }
}

private fun parseWalkingSessions(response: String): List<Map<String, Any>> {
    val walkingSessions = mutableListOf<Map<String, Any>>()
    val jsonArray = JSONArray(response)

    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val session = mapOf(
            "average_pulse" to jsonObject.getInt("average_pulse"),
            "date" to jsonObject.getString("date"),
            "duration" to jsonObject.getInt("duration"),
            "training_id" to jsonObject.getJSONObject("training").getInt("training_id"),
            "walking_id" to jsonObject.getInt("walking_id")
        )
        walkingSessions.add(session)
    }

    return walkingSessions
}

@Composable
fun WalkingCard(value: Int, unit: String, description: String) {
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
                text = unit,
                fontSize = 14.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWalkingSessionsScreen() {
    WalkingSessionsScreen(
        onOverviewChoice = {},
        onAddNewWalkingSessionChoice = {}
    )
}
