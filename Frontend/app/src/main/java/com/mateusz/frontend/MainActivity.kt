package com.mateusz.frontend

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            // All permissions granted
            Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            // Some permissions denied
            Toast.makeText(
                this,
                "Bluetooth permissions required for heart rate monitoring",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkBlePermissions()
        setContent {
            MyApp()
        }
    }

    private fun checkBlePermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
