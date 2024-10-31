package com.mateusz.frontend

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun OverviewScreen(
    onEditProfileChoice: () -> Unit,
    onLogOutChoice: () -> Unit,
    onWalkingSessionsChoice: () -> Unit,
    onRunningSessionsChoice: () -> Unit,
    name: String?,
    steps: Int?,
    stepsGoal: Int?,
    walkingTime: Int?,
    runningTime: Int?,
    cyclingTime: Int?,
    walkingPulse: Int?,
    runningPulse: Int?,
    cyclingPulse: Int?,
) {
    val context = LocalContext.current

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
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (name != null) {
                    Text(
                        text = name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 4.dp, end = 4.dp)
                            .weight(0.5F)
                    )
                } else {
                    Text(
                        text = "No data...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    onClick = {
                        onEditProfileChoice()
                    },
                    modifier = Modifier
                        .padding(start = 4.dp, end = 4.dp)
                        .weight(0.5F)
                        .height(50.dp),  // Adjust height to match the screenshot
                    shape = RoundedCornerShape(50),  // Rounded corners to match the screenshot
                    border = BorderStroke(
                        2.dp,
                        color = colorResource(id = R.color.light_blue)
                    )  // Using custom color
                ) {
                    Text(
                        "Edit Profile",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.light_blue)  // White text for outlined button
                    )
                }

                OutlinedButton(
                    onClick = {
                        //TODO implement functionality
                        onLogOutChoice()
                    },
                    modifier = Modifier
                        .padding(start = 4.dp, end = 4.dp)
                        .weight(0.5F)
                        .height(50.dp),  // Adjust height to match the screenshot
                    shape = RoundedCornerShape(50),  // Rounded corners to match the screenshot
                    border = BorderStroke(
                        2.dp,
                        color = colorResource(id = R.color.light_blue)
                    )  // Using custom color
                ) {
                    Text(
                        "Logout",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.light_blue)  // White text for outlined button
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Health Overview Header
            Text(
                text = "Training Sessions Overview",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Your Daily Health Statistics",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onLogOutChoice()
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                if (steps != null && stepsGoal != null) {
                    WalkingCard(steps, "Steps", "Out of $stepsGoal")
                } else {
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
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("No steps data found...")
                        }
                    }
                }
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
                        onWalkingSessionsChoice()
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                if (walkingPulse != null && walkingTime != null) {
                    WalkingCard(walkingPulse, "BPM", "Pulse")
                    WalkingCard(walkingTime, "Minutes", "Training")
                } else {
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .padding(top = 4.dp, start = 8.dp, end = 8.dp)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(id = R.color.light_blue),  // Custom blue background
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
                        onRunningSessionsChoice()
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                if (runningPulse != null && runningTime != null) {
                    WalkingCard(runningPulse, "BPM", "Pulse")
                    WalkingCard(runningTime, "Minutes", "Training")
                } else {
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .padding(top = 4.dp, start = 8.dp, end = 8.dp)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(id = R.color.light_blue),  // Custom blue background
                            contentColor = colorResource(id = R.color.white)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("No running sessions found...")
                        }
                    }
                }
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
                        onLogOutChoice()
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                if (cyclingPulse != null && cyclingTime != null) {
                    WalkingCard(cyclingPulse, "BPM", "Pulse")
                    WalkingCard(cyclingTime, "Minutes", "Training")
                } else {
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .padding(top = 4.dp, start = 8.dp, end = 8.dp)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(id = R.color.light_blue),  // Custom blue background
                            contentColor = colorResource(id = R.color.white)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("No cycling sessions found...")
                        }
                    }
                }
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
//                Choose data button
                OutlinedButton(
                    onClick = {
                        //TODO implement functionality
                    },
                    modifier = Modifier
                        .padding(top = 24.dp, start = 4.dp, end = 4.dp)
                        .weight(0.5F)
                        .height(56.dp),  // Adjust height to match the screenshot
                    shape = RoundedCornerShape(50),  // Rounded corners to match the screenshot
                    border = BorderStroke(
                        2.dp,
                        color = colorResource(id = R.color.light_blue)
                    )  // Using custom color
                ) {
                    Text(
                        "Choose date",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.light_blue)  // White text for outlined button
                    )
                }

//                Refresh button
                OutlinedButton(
                    onClick = {
                        //TODO implement functionality
                    },
                    modifier = Modifier
                        .padding(top = 24.dp, start = 4.dp, end = 4.dp)
                        .weight(0.5F)
                        .height(56.dp),  // Adjust height to match the screenshot
                    shape = RoundedCornerShape(50),  // Rounded corners to match the screenshot
                    border = BorderStroke(
                        2.dp,
                        color = colorResource(id = R.color.light_blue)
                    )  // Using custom color
                ) {
                    Text(
                        "Refresh",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.light_blue)  // White text for outlined button
                    )
                }
            }
        }
    }
}

@Composable
fun HealthCard(value: Int, unit: String, description: String) {
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
fun PreviewOverviewScreen() {
    OverviewScreen(
        onEditProfileChoice = {},
        onLogOutChoice = {},
        onWalkingSessionsChoice = {},
        onRunningSessionsChoice = {},
        name = "Your Name",
        steps = 8249,
        stepsGoal = null,
        walkingPulse = null,
        walkingTime = 20,
        runningPulse = 96,
        runningTime = 45,
        cyclingPulse = 89,
        cyclingTime = 30
    )
}
