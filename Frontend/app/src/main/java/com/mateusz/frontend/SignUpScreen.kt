package com.mateusz.frontend

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToHomeScreen: () -> Unit
) {
    val context = LocalContext.current
    var signUpResult by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()
    var showGenderMenu by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var birthDateInput by remember { mutableStateOf<LocalDate?>(null) }
    var gender by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 48.dp, end = 48.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.health_monitor_logo_simple),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 8.dp),
            contentScale = ContentScale.Fit
        )
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.light_blue),
                focusedLabelColor = colorResource(id = R.color.light_blue),
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.light_blue),
                focusedLabelColor = colorResource(id = R.color.light_blue),
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.light_blue),
                focusedLabelColor = colorResource(id = R.color.light_blue),
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        val datePickerDialog = remember {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    birthDateInput = LocalDate.of(year, month + 1, dayOfMonth)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp)
                )
                .border(BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(4.dp))
                .padding(16.dp)
                .clickable { datePickerDialog.show() }
        ) {
            Text(
                text = birthDateInput?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: "Birth Date (YYYY-MM-DD)",
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        DropdownMenu(
            expanded = showGenderMenu,
            onDismissRequest = { showGenderMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Male") },
                onClick = {
                    gender = "Male"
                    showGenderMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Female") },
                onClick = {
                    gender = "Female"
                    showGenderMenu = false
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp)
                )
                .border(BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(4.dp))
                .padding(16.dp)
                .clickable { showGenderMenu = true }
        ) {
            if (gender == "") {
                Text(
                    text = "Gender (Male/Female)",
                    color = Color.Black
                )
            } else {
                Text(
                    text = gender,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (Optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.light_blue),
                focusedLabelColor = colorResource(id = R.color.light_blue),
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (Optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.light_blue),
                focusedLabelColor = colorResource(id = R.color.light_blue),
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val birthDate = SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).parse(
                            birthDateInput?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                .toString()
                        )
                        val result =
                            makeSignUpRequest(name, email, password, birthDate, gender, weight, height)
                        withContext(Dispatchers.Main) {
                            signUpResult = result
                            if (result == "Success") {
                                onSignUpSuccess()
                            } else {
                                Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Invalid Input. Check Date format or values.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            },
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
                "Sign Up",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                onNavigateToHomeScreen()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp),
            shape = RoundedCornerShape(50),
            border = BorderStroke(
                2.dp,
                color = colorResource(id = R.color.light_blue)
            )
        ) {
            Text(
                "Home Screen",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(id = R.color.light_blue)
            )
        }
    }
}

private suspend fun makeSignUpRequest(
    name: String,
    email: String,
    password: String,
    birthDate: Date?,
    gender: String,
    weight: String?,
    height: String?
): String {
    val url = URL("${NetworkConfig.getBaseUrl()}/register")

    val connection = withContext(Dispatchers.IO) {
        url.openConnection() as HttpURLConnection
    }

    return try {
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val birthDateStr = birthDate?.let { sdf.format(it) }

        // Create JSON object
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("name", name)
            put("birth_date", birthDateStr)
            put("sex", gender)
            weight?.toIntOrNull()?.let { put("weight", it) }
            height?.toIntOrNull()?.let { put("height", it) }
        }

        // Write the JSON data to the output stream
        withContext(Dispatchers.IO) {
            OutputStreamWriter(connection.outputStream).apply {
                write(jsonBody.toString())
                flush()
                close()
            }
        }

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            connection.inputStream.bufferedReader().use { it.readText() }
            "Success"
        } else {
            // Try to get error message from response
            val errorStream = connection.errorStream
            val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
            errorResponse?.let {
                try {
                    val jsonError = JSONObject(it)
                    jsonError.getString("message") ?: "Failed with response code $responseCode"
                } catch (e: Exception) {
                    "Failed with response code $responseCode"
                }
            } ?: "Failed with response code $responseCode"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        when (e) {
            is java.net.ConnectException -> "Error: Could not connect to server"
            is java.net.SocketTimeoutException -> "Error: Connection timed out"
            is java.net.UnknownHostException -> "Error: No internet connection"
            else -> "Error: ${e.localizedMessage}"
        }
    } finally {
        connection.disconnect()
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewSignUpScreen() {
    SignUpScreen(onSignUpSuccess = { /*TODO*/ }) {

    }
}
