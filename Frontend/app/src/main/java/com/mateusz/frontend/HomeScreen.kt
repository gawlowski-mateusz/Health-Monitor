package com.mateusz.frontend

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreen(onLoginPanelChoose: () -> Unit, onSignupPanelChoice: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the logo at the top
        Image(
            painter = painterResource(id = R.drawable.logo_full),  // Assuming logo.png is placed in res/drawable
            contentDescription = "App Logo",
            modifier = Modifier
                .size(300.dp)  // Adjust size accordingly
                .padding(top = 32.dp, bottom = 24.dp),
            contentScale = ContentScale.Fit
        )

        // Subtitle
        Text(
            text = "The Smart Platform for Activity Tracking",
            fontSize = 16.sp,
            color = colorResource(id = R.color.light_blue),
            textAlign = TextAlign.Center,  // Centers the text content
            modifier = Modifier
                .fillMaxWidth()  // Makes the text take the full width of the parent
                .padding(bottom = 80.dp)
                .padding(horizontal = 32.dp)
        )

        // Login Button - filled style
        Button(
            onClick = { onLoginPanelChoose() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp),  // Adjust height to match the screenshot
            shape = RoundedCornerShape(50),  // Rounded corners to match the screenshot
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.light_blue),  // Custom blue background
                contentColor = colorResource(id = R.color.white)  // White text color
            )
        ) {
            Text(
                "Login",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Spacing between buttons
        Spacer(modifier = Modifier.height(16.dp))

        // "Register" Button - outlined style
        OutlinedButton(
            onClick = { onSignupPanelChoice() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp),  // Adjust height to match the screenshot
            shape = RoundedCornerShape(50),  // Rounded corners to match the screenshot
            border = BorderStroke(2.dp, color = colorResource(id = R.color.light_blue))  // Using custom color
        ) {
            Text(
                "Register",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(id = R.color.light_blue)  // White text for outlined button
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen(onSignupPanelChoice = { /*TODO*/ }, onLoginPanelChoose = {})
}
