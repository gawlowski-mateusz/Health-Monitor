package com.mateusz.frontend

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.util.Base64

@Composable
fun ProfileViewScreen(onGoBackChoice: () -> Unit) {
    var name by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var birthDate by remember { mutableStateOf<String?>(null) }
    var gender by remember { mutableStateOf<String?>(null) }
    var weight by remember { mutableStateOf<Float?>(null) }
    var height by remember { mutableStateOf<Int?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 48.dp, end = 48.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profile overview",
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // name TextField
            OutlinedTextField(
                value = name ?: "",
                onValueChange = { /* read only */ },
                label = { Text("Name") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color.Black,
                    disabledTextColor = Color.Black,
                    disabledLabelColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // email TextField
            OutlinedTextField(
                value = email ?: "",
                onValueChange = { /* read only */ },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color.Black,
                    disabledTextColor = Color.Black,
                    disabledLabelColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Birth date TextField
            OutlinedTextField(
                value = birthDate ?: "",
                onValueChange = { /* read only */ },
                label = { Text("Birth Date") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color.Black,
                    disabledTextColor = Color.Black,
                    disabledLabelColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Gender TextField
            OutlinedTextField(
                value = gender ?: "",
                onValueChange = { /* read only */ },
                label = { Text("Gender") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color.Black,
                    disabledTextColor = Color.Black,
                    disabledLabelColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Weight TextField
            OutlinedTextField(
                value = weight?.toString() ?: "",
                onValueChange = { /* read only */ },
                label = { Text("Weight") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color.Black,
                    disabledTextColor = Color.Black,
                    disabledLabelColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Height TextField
            OutlinedTextField(
                value = height?.toString() ?: "",
                onValueChange = { /* read only */ },
                label = { Text("Height") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color.Black,
                    disabledTextColor = Color.Black,
                    disabledLabelColor = Color.Black
                )
            )

            // Refresh Button
            Button(
                onClick = {
                    scope.launch {
                        val userData = fetchUserData(context)
                        userData?.let { user ->
                            // Update all state variables with fetched data
                            name = user["name"] as? String
                            email = user["email"] as? String
                            birthDate = user["birth_date"] as? String
                            gender = user["sex"] as? String
                            weight = (user["weight"] as? Double)?.toFloat()
                            height = user["height"] as? Int
                        } ?: run {
                            Toast.makeText(
                                context,
                                "Failed to fetch user data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 42.dp, start = 32.dp, end = 32.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.light_blue),
                    contentColor = colorResource(id = R.color.white)
                )
            ) {
                Text(
                    "Refresh",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cancel Button
            OutlinedButton(
                onClick = { onGoBackChoice() },
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
                    "Go back",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.light_blue)
                )
            }
        }
    }
}

fun getUserIdFromJwt(jwt: String): String? {
    try {
        // Split the JWT into its parts
        val parts = jwt.split(".")
        if (parts.size != 3) return null // Not a valid JWT

        // Decode the payload (the second part of the JWT)
        val payload = parts[1]
        val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
        val decodedString = String(decodedBytes)

        // Parse the JSON payload
        val jsonObject = JSONObject(decodedString)
        return jsonObject.optString("sub").takeIf { it.isNotEmpty() }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

private suspend fun fetchUserData(context: Context): Map<String, Any>? {
    return withContext(Dispatchers.IO) {
        // Retrieve JWT token from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPreferences.getString("access_token", null)
            ?: return@withContext null

        // Get user_id from JWT token
        val userId = getUserIdFromJwt(jwtToken)
            ?: return@withContext null

        val url = URL("http://10.0.2.2:8000/user/$userId")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $jwtToken")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                parseUserData(responseText)
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to fetch user data", Toast.LENGTH_SHORT)
                        .show()
                }
                null
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            null
        } finally {
            connection.disconnect()
        }
    }
}

private fun parseUserData(response: String): Map<String, Any>? {
    return try {
        val jsonResponse = JSONObject(response)
        mapOf(
            "birth_date" to jsonResponse.getString("birth_date"),
            "email" to jsonResponse.getString("email"),
            "height" to jsonResponse.getInt("height"),
            "name" to jsonResponse.getString("name"),
            "sex" to jsonResponse.getString("sex"),
            "weight" to jsonResponse.getDouble("weight")
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileViewScreen() {
    ProfileViewScreen(onGoBackChoice = {})
}