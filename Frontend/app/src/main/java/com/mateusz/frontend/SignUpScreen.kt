package com.mateusz.frontend

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SignUpScreen(onSignUpSuccess: () -> Unit, onNavigateToHomeScreen: () -> Unit) {
    val context = LocalContext.current
    var signUpResult by remember { mutableStateOf("") }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var birthDateInput by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 48.dp, end = 48.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the logo at the top
        Image(
            painter = painterResource(id = R.drawable.logo_simple),  // Assuming logo.png is placed in res/drawable
            contentDescription = "App Logo",
            modifier = Modifier
                .size(150.dp)  // Adjust size accordingly
                .padding(bottom = 8.dp),
            contentScale = ContentScale.Fit
        )


        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = birthDateInput,
            onValueChange = { birthDateInput = it },
            label = { Text("Birth Date (YYYY-MM-DD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = sex,
            onValueChange = { sex = it },
            label = { Text("Sex (Male/Female)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (Optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (Optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val birthDate = SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).parse(birthDateInput)
                        val result =
                            makeSignUpRequest(name, email, password, birthDate, sex, weight, height)
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
                .height(56.dp),  // Adjust height to match the screenshot
            shape = RoundedCornerShape(50),  // Rounded corners to match the screenshot
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.light_blue),  // Custom blue background
                contentColor = colorResource(id = R.color.white)  // White text color
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
                .height(56.dp),  // Adjust height to match the screenshot
            shape = RoundedCornerShape(50),  // Rounded corners to match the screenshot
            border = BorderStroke(
                2.dp,
                color = colorResource(id = R.color.light_blue)
            )  // Using custom color
        ) {
            Text(
                "Home Screen",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(id = R.color.light_blue)  // White text for outlined button
            )
        }
    }
}

private suspend fun makeSignUpRequest(
    name: String,
    email: String,
    password: String,
    birthDate: Date?,
    sex: String,
    weight: String?,
    height: String?
): String {
    val url = URL("http://10.0.2.2:8000/register") // Replace with your actual API URL

    val connection = withContext(Dispatchers.IO) {
        url.openConnection() as HttpURLConnection
    }

    return try {
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        // Convert Date to the correct format (YYYY-MM-DD)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val birthDateStr = birthDate?.let { sdf.format(it) }

        // Create JSON object
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("name", name)
            put("birth_date", birthDateStr) // Send birthDate in the correct format
            put("sex", sex)
            weight?.let { put("weight", it.toInt()) } // Optional field, parse as Int
            height?.let { put("height", it.toInt()) }   // Optional field, parse as Int
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
            return "Success"

        } else {
            "Failed with response code $responseCode"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Error: ${e.localizedMessage}"
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
