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
                onSignUpSuccess = { navController.navigate("login") },
                onNavigateToHomeScreen = { navController.navigate("home") }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("overview") },
                onNavigateToHomeScreen = { navController.navigate("home") }
            )
        }
        composable("home") {
            HomeScreen(
                onLoginPanelChoice = { navController.navigate("login") },
                onSignupPanelChoice = { navController.navigate("signup") }
            )
        }
        composable("overview") {
            OverviewScreen(
                onEditProfileChoice = {navController.navigate("edit_profile")},
                onLogOutChoice = {navController.navigate("home")},
                null, null, null, null, null,
                null, null, null, null )
        }
        composable("edit_profile") {
            EditProfileScreen(
                onSaveChoice = {navController.navigate("overview")},
                onCancelChoice = {navController.navigate("overview")}
            )
        }
    }
}
