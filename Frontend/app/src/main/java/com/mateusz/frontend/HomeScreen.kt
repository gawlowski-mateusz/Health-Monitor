package com.mateusz.frontend

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(onLoginPanelChoice: () -> Unit, onRegisterPanelChoice: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.health_monitor_logo_full),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(300.dp)
                .padding(top = 32.dp, bottom = 24.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "The Smart Platform for Activity Tracking",
            fontSize = 16.sp,
            color = colorResource(id = R.color.light_blue),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
                .padding(horizontal = 32.dp)
        )

        Button(
            onClick = { onLoginPanelChoice() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.light_blue),
                contentColor = colorResource(id = R.color.white)
            )
        ) {
            Text(
                "Login",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { onRegisterPanelChoice() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp),
            shape = RoundedCornerShape(50),
            border = BorderStroke(2.dp, color = colorResource(id = R.color.light_blue))
        ) {
            Text(
                "Register",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(id = R.color.light_blue)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen(onRegisterPanelChoice = { /*TODO*/ }, onLoginPanelChoice = {})
}
