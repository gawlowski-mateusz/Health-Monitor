package com.mateusz.frontend

import android.content.Context
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import javax.net.ssl.HttpsURLConnection

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToHomeScreen: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    var loginResult by remember { mutableStateOf("") }

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
                .size(200.dp),
            contentScale = ContentScale.Fit
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
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
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.light_blue),
                focusedLabelColor = colorResource(id = R.color.light_blue),
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = makeLoginRequest(email, password, context)
                    withContext(Dispatchers.Main) {
                        loginResult = result
                        if (result == "Success") {
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 32.dp, end = 32.dp)
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

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { onNavigateToHomeScreen() },
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

suspend fun makeLoginRequest(
    email: String,
    password: String,
    context: Context,
    testConnection: HttpsURLConnection? = null
): String {
    val TAG = "LoginAPI"

    val url = URL("${NetworkConfig.getBaseUrl()}/login")
    Log.d(TAG, "Making login request to: $url")

    val connection = testConnection ?: try {
        withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpsURLConnection
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create connection", e)
        return "Could not connect to server"
    }

    return try {
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        // Create login request body
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        Log.d(TAG, "Sending login request for email: $email")

        // Send request
        withContext(Dispatchers.IO) {
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }
        }

        val responseCode = connection.responseCode
        Log.d(TAG, "Login response code: $responseCode")

        when (responseCode) {
            HttpURLConnection.HTTP_OK -> {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Login successful")

                try {
                    val jsonResponse = JSONObject(response)
                    val accessToken = jsonResponse.getString("access_token")
                    val refreshToken = jsonResponse.getString("refresh_token")

                    Log.d(TAG, "Access token received: ${accessToken.take(10)}...")
                    Log.d(TAG, "Refresh token received: ${refreshToken.take(10)}...")

                    TokenManager.saveTokens(context, accessToken, refreshToken)

                    // Verify tokens were saved
                    val savedAccessToken = TokenManager.getAccessToken(context)
                    Log.d(TAG, "Verified saved access token: ${savedAccessToken?.take(10) ?: "null"}...")

                    "Success"
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing login response", e)
                    "Error: Failed to process login response"
                }
            }
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                Log.w(TAG, "Invalid credentials")
                "Invalid email or password"
            }
            HttpURLConnection.HTTP_BAD_REQUEST -> {
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "Bad request error: $errorResponse")

                errorResponse?.let {
                    try {
                        JSONObject(it).getString("message") ?: "Invalid login data"
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse error response", e)
                        "Invalid login data"
                    }
                } ?: "Invalid login data"
            }
            else -> {
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "Unexpected error response: $errorResponse")

                errorResponse?.let {
                    try {
                        JSONObject(it).getString("message")
                            ?: "Login failed with response code $responseCode"
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse error response", e)
                        "Login failed with response code $responseCode"
                    }
                } ?: "Login failed with response code $responseCode"
            }
        }
    } catch (e: Exception) {
        val errorMessage = when (e) {
            is java.net.ConnectException -> {
                Log.e(TAG, "Connection error", e)
                "Could not connect to server"
            }
            is java.net.SocketTimeoutException -> {
                Log.e(TAG, "Timeout error", e)
                "Connection timed out"
            }
            is java.net.UnknownHostException -> {
                Log.e(TAG, "No internet connection", e)
                "No internet connection"
            }
            is javax.net.ssl.SSLHandshakeException -> {
                Log.e(TAG, "SSL error", e)
                "SSL certificate verification failed"
            }
            else -> {
                Log.e(TAG, "Unexpected error during login", e)
                "Login error: ${e.localizedMessage}"
            }
        }
        errorMessage
    } finally {
        connection.disconnect()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(
        onLoginSuccess = {},
        onNavigateToHomeScreen = {}
    )
}