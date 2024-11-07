package com.mateusz.frontend

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WalkingSessionsScreen(
    onOverviewChoice: () -> Unit,
    onAddNewWalkingSessionChoice: () -> Unit,
    selectedDate: LocalDate? = null
) {
    val context = LocalContext.current
    var walkingSessions by remember { mutableStateOf<List<Map<String, Any>>?>(null) }
    var walkingSessionCount = 0

    // Fetch walking sessions when the screen is first composed
    LaunchedEffect(selectedDate) {  // This will re-trigger when selectedDate changes
        val formattedDate = selectedDate?.format(DateTimeFormatter.ISO_DATE)
        val result = fetchWalkingSessions(context, formattedDate)
        walkingSessions = result
        walkingSessionCount = 0  // Reset counter when new data is fetched
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.20f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,  // Changed to Start alignment
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
                        text = "Walking sessions",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(0.8f)  // 90% of the space
                    )
                }
            }

            // Lower 25% non-scrollable area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.80f)
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 64.dp)
                    .verticalScroll(rememberScrollState()),
//                contentAlignment = Alignment.Center  // Add this line to center the content
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),  // Add this modifier
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center  // Add this line to center vertically
                ) {
                    if (!walkingSessions.isNullOrEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),  // Add this modifier
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            walkingSessions?.forEach { session ->
                                walkingSessionCount++

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()  // Add this modifier
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.Center,
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

                    val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                    val selectedDateStr = selectedDate?.format(DateTimeFormatter.ISO_DATE)

                    if (selectedDateStr == today) {
                        // Add walking session Button
                        Button(
                            onClick = {
                                onAddNewWalkingSessionChoice()
                            },
                            modifier = Modifier
                                .width(300.dp)
                                .padding(top = 4.dp, start = 8.dp, end = 8.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(25),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.light_blue),
                                contentColor = colorResource(id = R.color.white)
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
            }
        }
    }
}


private suspend fun fetchWalkingSessions(
    context: Context,
    selectedDate: String? = null
): List<Map<String, Any>>? {
    return withContext(Dispatchers.IO) {
        val dateStr = selectedDate ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val url = URL("http://10.0.2.2:8000/walking/$dateStr")

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
        onAddNewWalkingSessionChoice = {},
        selectedDate = null
    )
}
