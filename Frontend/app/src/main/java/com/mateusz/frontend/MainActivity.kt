package com.mateusz.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
                onViewProfileChoice = {navController.navigate("profile_view")},
                onLogOutChoice = {navController.navigate("home")},
                onEditStepsChoice = {navController.navigate("edit_steps")},
                onWalkingSessionsChoice = { date ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("selected_date", date?.format(DateTimeFormatter.ISO_DATE))
                    navController.navigate("walking_sessions") },
                onRunningSessionsChoice = { date ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("selected_date", date?.format(DateTimeFormatter.ISO_DATE))
                    navController.navigate("running_sessions") },
                onCyclingSessionsChoice = {date ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("selected_date", date?.format(DateTimeFormatter.ISO_DATE))
                    navController.navigate("cycling_sessions") },
                )
        }

        composable("profile_view") {
            ProfileViewScreen(
                onGoBackChoice = {navController.navigate("overview")},
                onEditProfileChoice = {navController.navigate("edit_profile")},
                )
        }

        composable("edit_profile") {
            EditProfileScreen(
                onSaveChoice = {navController.navigate("profile_view")},
                onCancelChoice = {navController.navigate("profile_view")}
            )
        }

        composable("edit_steps") {
            EditStepsScreen(
                onSaveChoice = {navController.navigate("overview")},
                onCancelChoice = {navController.navigate("overview")}
            )
        }

        composable("walking_sessions") {
            val dateStr = navController.previousBackStackEntry?.savedStateHandle?.get<String>("selected_date")
            val selectedDate = if (!dateStr.isNullOrEmpty()) {
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)
            } else null

            WalkingSessionsScreen(
                selectedDate = selectedDate,
                onOverviewChoice = { navController.navigate("overview") },
                onAddNewWalkingSessionChoice = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("selected_date", dateStr)
                    navController.navigate("new_walking_session")
                }
            )
        }

        composable("new_walking_session") {
            val dateStr = navController.previousBackStackEntry?.savedStateHandle?.get<String>("selected_date")
            val selectedDate = if (!dateStr.isNullOrEmpty()) {
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)
            } else LocalDate.now()

            NewWalkingSessionScreen(
                onSaveChoice = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "selected_date",
                        selectedDate.format(DateTimeFormatter.ISO_DATE)
                    )
                    navController.navigateUp()
                }
            ) {
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "selected_date",
                    selectedDate.format(DateTimeFormatter.ISO_DATE)
                )
                navController.navigateUp()
            }
        }

        composable("running_sessions") {
            val dateStr = navController.previousBackStackEntry?.savedStateHandle?.get<String>("selected_date")
            val selectedDate = if (!dateStr.isNullOrEmpty()) {
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)
            } else null

            RunningSessionsScreen(
                selectedDate = selectedDate,
                onOverviewChoice = { navController.navigate("overview") },
                onAddNewRunningSessionChoice = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("selected_date", dateStr)
                    navController.navigate("new_running_session")
                }
            )
        }

        composable("new_running_session") {
            val dateStr = navController.previousBackStackEntry?.savedStateHandle?.get<String>("selected_date")
            val selectedDate = if (!dateStr.isNullOrEmpty()) {
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)
            } else LocalDate.now()

            NewRunningSessionScreen(
                onSaveChoice = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "selected_date",
                        selectedDate.format(DateTimeFormatter.ISO_DATE)
                    )
                    navController.navigateUp()
                }
            ) {
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "selected_date",
                    selectedDate.format(DateTimeFormatter.ISO_DATE)
                )
                navController.navigateUp()
            }
        }

        composable("cycling_sessions") {
            val dateStr = navController.previousBackStackEntry?.savedStateHandle?.get<String>("selected_date")
            val selectedDate = if (!dateStr.isNullOrEmpty()) {
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)
            } else null

            CyclingSessionsScreen(
                selectedDate = selectedDate,
                onOverviewChoice = { navController.navigate("overview") },
                onAddNewCyclingSessionChoice = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("selected_date", dateStr)
                    navController.navigate("new_cycling_session")
                }
            )
        }

        composable("new_cycling_session") {
            val dateStr = navController.previousBackStackEntry?.savedStateHandle?.get<String>("selected_date")
            val selectedDate = if (!dateStr.isNullOrEmpty()) {
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)
            } else LocalDate.now()

            NewCyclingSessionScreen(
                onSaveChoice = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "selected_date",
                        selectedDate.format(DateTimeFormatter.ISO_DATE)
                    )
                    navController.navigateUp()
                }
            ) {
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "selected_date",
                    selectedDate.format(DateTimeFormatter.ISO_DATE)
                )
                navController.navigateUp()
            }
        }
    }
}
