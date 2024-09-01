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
fun HealthOverviewScreen(
    name: String,
    steps: String,
    trainingTime: String,
    calories: String,
    pulse: String,
    weight: String
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
                Button(onClick = {}) {
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HealthCard(steps, "Steps", "Out of 10,000")
                HealthCard(trainingTime, "Minutes", "Training")
                HealthCard(calories, "kCal", "Calories")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pulse and Weight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HealthCard(pulse, "BPM", "Pulse")
                HealthCard(weight, "KG", "Weight")
            }
        }
    }
}

@Composable
fun HealthCard(value: String, unit: String, description: String) {
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
                text = value,
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
fun PreviewHealthOverviewScreen() {
    HealthOverviewScreen(
        name = "Your Name",
        steps = "8k",
        trainingTime = "90",
        calories = "2k",
        pulse = "78",
        weight = "64"
    )
}
