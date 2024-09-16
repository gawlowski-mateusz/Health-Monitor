package com.mateusz.frontend

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OverviewScreen(
    name: String,
    steps: Int,
    walkingTime: Int,
    runningTime: Int,
    cyclingTime: Int,
    walkingPulse:Int,
    runningPulse:Int,
    cyclingPulse:Int,
) {
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
            // Profile Picture and Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = {}) {
                    Text(text = "Edit profile")
                }
                Button(onClick = {
                }) {
                    Text(text = "Log out")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Health Overview Header
            Text(
                text = "Health Overview",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Your Daily Health Statistics",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Steps, Water, Calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                HealthCard(steps, "Steps", "Out of 10,000")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Walking
            Text(
                text = "Walking",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HealthCard(walkingPulse, "BPM", "Pulse")
                HealthCard(walkingTime, "Minutes", "Training")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Running
            Text(
                text = "Running",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HealthCard(runningPulse, "BPM", "Pulse")
                HealthCard(runningTime, "Minutes", "Training")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cycling
            Text(
                text = "Cycling",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

            // Running
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HealthCard(cyclingPulse, "BPM", "Pulse")
                HealthCard(cyclingTime, "Minutes", "Training")
            }
        }
    }
}

@Composable
fun HealthCard(value: Int, unit: String, description: String) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0E6FF))
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
                color = Color.Gray
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOverviewScreen() {
    OverviewScreen(
        name = "Your Name",
        steps = 8249,
        walkingPulse = 72,
        walkingTime = 20,
        runningPulse = 96,
        runningTime = 45,
        cyclingPulse = 89,
        cyclingTime = 30
    )
}
