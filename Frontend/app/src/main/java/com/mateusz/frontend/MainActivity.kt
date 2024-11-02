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
                onWalkingSessionsChoice = {navController.navigate("walking_sessions")},
                onRunningSessionsChoice = {navController.navigate("running_sessions")},
                null, null, null, null, null,
                null, null, null, null )
                onCyclingSessionsChoice = {navController.navigate("cycling_sessions")},
                )
        }
        composable("edit_profile") {
            EditProfileScreen(
                onSaveChoice = {navController.navigate("overview")},
                onCancelChoice = {navController.navigate("overview")}
            )
        }
        composable("walking_sessions") {
            WalkingSessionsScreen(
                onOverviewChoice = {navController.navigate("overview")},
                onAddNewWalkingSessionChoice = {navController.navigate("new_walking_session")}
            )
        }
        composable("new_walking_session") {
            NewWalkingSessionScreen(
                onSaveChoice = {navController.navigate("walking_sessions")},
                onCancelChoice = {navController.navigate("walking_sessions")}
            )
        }
        composable("running_sessions") {
            RunningSessionsScreen(
                onOverviewChoice = {navController.navigate("overview")},
                onAddNewRunningSessionChoice = {navController.navigate("new_running_session")}
            )
        }
        composable("new_running_session") {
            NewRunningSessionScreen(
                onSaveChoice = {navController.navigate("running_sessions")},
                onCancelChoice = {navController.navigate("running_sessions")}
            )
        }
        composable("cycling_sessions") {
            CyclingSessionsScreen(
                onOverviewChoice = {navController.navigate("overview")},
                onAddNewCyclingSessionChoice = {navController.navigate("new_cycling_session")}
            )
        }
        composable("new_cycling_session") {
            NewCyclingSessionScreen(
                onSaveChoice = {navController.navigate("cycling_sessions")},
                onCancelChoice = {navController.navigate("cycling_sessions")}
            )
        }
    }
}
