package com.mateusz.frontend

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mateusz.frontend.api.ApiClient
import com.mateusz.frontend.model.WalkingSession
import kotlinx.coroutines.launch

@Composable
fun WalkingSessionsScreen(
    onOverviewChoice: () -> Unit,
) {
//    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var walkingSessions by remember { mutableStateOf<List<WalkingSession>?>(null) }

    // Fetch the walking data from the API
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            walkingSessions = try {
                ApiClient.apiService.getWalkingSessions()
            } catch (e: Exception) {
                // Handle exception
                emptyList()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Walking Header
            Text(
                text = "Walking",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

            if (!walkingSessions.isNullOrEmpty()) {
                Column {
                    walkingSessions?.forEach { session ->
                        WalkingCard(session.average_pulse, "BPM", "Pulse")
                        WalkingCard(session.duration, "Minutes", "Training")
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

            // Add walking session Button
            Button(
                onClick = {

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

            Spacer(modifier = Modifier.height(8.dp))

            // Refresh Button
            Button(
                onClick = {

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 42.dp, start = 32.dp, end = 32.dp)
                    .height(56.dp),  // Adjust height to match the screenshot
                shape = RoundedCornerShape(50),  // Rounded corners to match the screenshot
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.light_blue),  // Custom blue background
                    contentColor = colorResource(id = R.color.white)  // White text color
                )
            ) {
                Text(
                    "Refresh",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cancel Button
            OutlinedButton(
                onClick = {
                    onOverviewChoice()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp),  // Adjust height to match the screenshot
                shape = RoundedCornerShape(50),  // Rounded corners to match the screenshot
                border = BorderStroke(
                    2.dp,
                    color = colorResource(id = R.color.light_blue)
                )  // Using custom color
            ) {
                Text(
                    "Overview screen",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.light_blue)  // White text for outlined button
                )
            }
        }
    }
}

@Composable
fun WalkingCard(value: Int, unit: String, description: String) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .padding(top = 4.dp, start = 8.dp, end = 8.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.light_blue),  // Custom blue background
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
        onOverviewChoice = {}
    )
}

