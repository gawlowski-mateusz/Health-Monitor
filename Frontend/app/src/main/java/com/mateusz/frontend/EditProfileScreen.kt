package com.mateusz.frontend

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

sealed class ProfileUpdateResult {
    data object Success : ProfileUpdateResult()
    data class Error(val message: String) : ProfileUpdateResult()
}

@Composable
private fun ValidationItem(
    text: String,
    isValid: Boolean
) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isValid) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isValid) Color.Green else Color.Red,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp),
            fontSize = 12.sp,
            color = if (isValid) Color.Green else Color.Red
        )
    }
}

@Composable
private fun PasswordValidation(
    password: String,
    onPasswordChange: (String) -> Unit,
    onPasswordStrengthChange: (String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    // Password validation checks
    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasNumber = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }

    val passwordStrength = when {
        password.isEmpty() -> "empty"
        !hasMinLength || !hasUppercase || !hasLowercase || !hasNumber || !hasSpecialChar -> "weak"
        password.length >= 14 -> "strong"
        else -> "medium"
    }

    LaunchedEffect(passwordStrength) {
        onPasswordStrengthChange(passwordStrength)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = password,
            onValueChange = { onPasswordChange(it) },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.light_blue),
                focusedLabelColor = colorResource(id = R.color.light_blue),
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        if (password.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                ValidationItem(
                    text = "At least 8 characters",
                    isValid = hasMinLength
                )
                ValidationItem(
                    text = "At least 1 uppercase letter",
                    isValid = hasUppercase
                )
                ValidationItem(
                    text = "At least 1 lowercase letter",
                    isValid = hasLowercase
                )
                ValidationItem(
                    text = "At least 1 number",
                    isValid = hasNumber
                )
                ValidationItem(
                    text = "At least 1 special character",
                    isValid = hasSpecialChar
                )
                Text(
                    text = "Password strength: ${passwordStrength.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }}",
                    color = when (passwordStrength) {
                        "weak" -> Color.Red
                        "medium" -> Color.Yellow
                        "strong" -> Color.Green
                        else -> Color.Gray
                    },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EditProfileScreen(onSaveChoice: () -> Unit, onCancelChoice: () -> Unit) {
    var heightText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var passwordStrength by remember { mutableStateOf("empty") }
    var heightError by remember { mutableStateOf<String?>(null) }
    var weightError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onCancelChoice() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    val height = heightText.toIntOrNull()
                    val weight = weightText.toFloatOrNull()

                    // Only allow non-empty password if it's strong enough
                    val password = when {
                        passwordText.isEmpty() -> null
                        passwordStrength == "weak" -> {
                            Toast.makeText(context, "Please ensure password meets all requirements", Toast.LENGTH_LONG).show()
                            return@IconButton
                        }
                        else -> passwordText
                    }

                    if (heightText.isNotEmpty() && height == null ||
                        weightText.isNotEmpty() && weight == null ||
                        heightError != null || weightError != null) {
                        Toast.makeText(context, "Please correct the errors before saving", Toast.LENGTH_LONG).show()
                        return@IconButton
                    }

                    if (heightText.isEmpty() && weightText.isEmpty() && passwordText.isEmpty()) {
                        Toast.makeText(context, "No changes to update", Toast.LENGTH_LONG).show()
                        return@IconButton
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        val result = makeEditProfileRequest(height, weight, password, context)
                        withContext(Dispatchers.Main) {
                            when (result) {
                                is ProfileUpdateResult.Success -> {
                                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                    onSaveChoice()
                                }
                                is ProfileUpdateResult.Error -> {
                                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFCAF0F8)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Height Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Height,
                            contentDescription = "Height",
                            tint = Color(0xFF424242)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedTextField(
                            value = heightText,
                            onValueChange = {
                                heightText = it
                                heightError = when {
                                    it.isNotEmpty() && it.toIntOrNull() == null -> "Please enter a valid number"
                                    it.isNotEmpty() && it.toIntOrNull() != null && it.toInt() <= 0 -> "Height must be greater than 0"
                                    else -> null
                                }
                            },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(id = R.color.light_blue),
                                focusedLabelColor = colorResource(id = R.color.light_blue),
                                errorBorderColor = Color.Red,
                                errorLabelColor = Color.Red
                            ),
                            isError = heightError != null,
                            supportingText = {
                                heightError?.let {
                                    Text(
                                        text = it,
                                        color = Color.Red
                                    )
                                }
                            }
                        )
                    }

                    Divider(color = Color(0xFF424242), thickness = 1.dp)

                    // Weight Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonitorWeight,
                            contentDescription = "Weight",
                            tint = Color(0xFF424242)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedTextField(
                            value = weightText,
                            onValueChange = {
                                weightText = it
                                weightError = when {
                                    it.isNotEmpty() && it.toFloatOrNull() == null -> "Please enter a valid number"
                                    it.isNotEmpty() && it.toFloatOrNull() != null && it.toFloat() <= 0 -> "Weight must be greater than 0"
                                    else -> null
                                }
                            },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(id = R.color.light_blue),
                                focusedLabelColor = colorResource(id = R.color.light_blue),
                                errorBorderColor = Color.Red,
                                errorLabelColor = Color.Red
                            ),
                            isError = weightError != null,
                            supportingText = {
                                weightError?.let {
                                    Text(
                                        text = it,
                                        color = Color.Red
                                    )
                                }
                            }
                        )
                    }

                    Divider(color = Color(0xFF424242), thickness = 1.dp)

                    // Password Row with Validation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Password,
                            contentDescription = "Password",
                            tint = Color(0xFF424242)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        PasswordValidation(
                            password = passwordText,
                            onPasswordChange = { passwordText = it },
                            onPasswordStrengthChange = { passwordStrength = it }
                        )
                    }
                }
            }
        }
    }
}

suspend fun makeEditProfileRequest(
    height: Int?,
    weight: Float?,
    password: String?,
    context: Context,
    testConnection: HttpsURLConnection? = null
): ProfileUpdateResult {
    val TAG = "ProfileAPI"

    // Add debug logging at the start
    Log.d(TAG, "Starting profile update request")

    // Check if context is valid
    if (context == null) {
        Log.e(TAG, "Context is null")
        return ProfileUpdateResult.Error("Internal error: Invalid context")
    }

    val url = URL("${NetworkConfig.getBaseUrl()}/update-profile")
    Log.d(TAG, "Making profile update request to: $url")

    val connection = testConnection ?: try {
        url.openConnection() as HttpsURLConnection
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create connection", e)
        return ProfileUpdateResult.Error("Could not connect to server")
    }

    return try {
        connection.requestMethod = "PATCH"
        connection.setRequestProperty("Content-Type", "application/json")

        // Get and verify token
        val jwtToken = TokenManager.getAccessToken(context)
        Log.d(TAG, "JWT token retrieved: ${jwtToken?.take(10) ?: "null"}...")

        if (jwtToken == null) {
            Log.w(TAG, "No JWT token found")
            return ProfileUpdateResult.Error("Please login to update your profile")
        }

        // Add JWT token to headers
        connection.setRequestProperty("Authorization", "Bearer $jwtToken")
        connection.doOutput = true

        // Create request body
        val jsonBody = JSONObject().apply {
            if (height != null && height > 0) {
                put("height", height)
            }
            if (weight != null && weight > 0) {
                put("weight", weight)
            }
            if (!password.isNullOrBlank()) {
                put("password", password)
            }
        }

        if (jsonBody.length() == 0) {
            Log.w(TAG, "No changes to update")
            return ProfileUpdateResult.Error("No changes to update")
        }

        Log.d(TAG, "Sending update data: ${jsonBody.toString().replace("password", "*****")}")

        // Send request
        withContext(Dispatchers.IO) {
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }
        }

        val responseCode = connection.responseCode
        Log.d(TAG, "Response code: $responseCode")

        when (responseCode) {
            HttpURLConnection.HTTP_OK -> {
                Log.d(TAG, "Profile updated successfully")
                ProfileUpdateResult.Success
            }

            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                Log.w(TAG, "Unauthorized - attempting token refresh")
                if (TokenManager.refreshToken(context)) {
                    // Retry the request with new token
                    Log.d(TAG, "Token refreshed, retrying request")
                    makeEditProfileRequest(height, weight, password, context)
                } else {
                    Log.w(TAG, "Token refresh failed")
                    ProfileUpdateResult.Error("Session expired. Please login again")
                }
            }

            HttpURLConnection.HTTP_BAD_REQUEST -> {
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "Bad request error: $errorResponse")

                val errorMessage = errorResponse?.let {
                    try {
                        JSONObject(it).getString("message") ?: "Invalid input data"
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse error response", e)
                        "Invalid input data"
                    }
                } ?: "Invalid input data"

                ProfileUpdateResult.Error(errorMessage)
            }

            else -> {
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "Unexpected error response: $errorResponse")

                val errorMessage = errorResponse?.let {
                    try {
                        JSONObject(it).getString("message")
                            ?: "Failed to update profile: $responseCode"
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse error response", e)
                        "Failed to update profile: $responseCode"
                    }
                } ?: "Failed to update profile: $responseCode"

                ProfileUpdateResult.Error(errorMessage)
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
                Log.e(TAG, "Unexpected error", e)
                "Error updating profile: ${e.localizedMessage}"
            }
        }
        ProfileUpdateResult.Error(errorMessage)
    } finally {
        connection.disconnect()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEditProfileScreen() {
    EditProfileScreen(onSaveChoice = {}, onCancelChoice = {})
}