package com.mateusz.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = { navController.navigate("home") },
                onNavigateToHomeScreen = { navController.navigate("home") }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") },
                onNavigateToHomeScreen = { navController.navigate("home") }
            )
        }
        composable("home") {
            HomeScreen(
                onLoginPanelChoose = { navController.navigate("login") },
                onSignupPanelChoice = { navController.navigate("signup") }
            )
        }
    }
}
