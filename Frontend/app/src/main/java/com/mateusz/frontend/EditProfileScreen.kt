package com.mateusz.frontend

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.HostnameVerifier

sealed class ProfileUpdateResult {
    data object Success : ProfileUpdateResult()
    data class Error(val message: String) : ProfileUpdateResult()
}

@Composable
fun EditProfileScreen(onSaveChoice: () -> Unit, onCancelChoice: () -> Unit) {
    var heightText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
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
                    val password = passwordText.takeIf { it.isNotBlank() }

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

                    // Password Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Password,
                            contentDescription = "Password",
                            tint = Color(0xFF424242)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedTextField(
                            value = passwordText,
                            onValueChange = { passwordText = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(id = R.color.light_blue),
                                focusedLabelColor = colorResource(id = R.color.light_blue),
                            )
                        )
                    }
                }
            }
        }
    }
}

private suspend fun makeEditProfileRequest(
    height: Int?,
    weight: Float?,
    password: String?,
    context: Context
): ProfileUpdateResult {
    val url = URL("${NetworkConfig.getBaseUrl()}/update-profile")

    val connection = withContext(Dispatchers.IO) {
        createHttpsConnection(url, context)
    }

    return try {
        connection.requestMethod = "PATCH"
        connection.setRequestProperty("Content-Type", "application/json")

        // Retrieve the JWT token from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPreferences.getString("access_token", null)
            ?: return ProfileUpdateResult.Error("Please login to update your profile")

        // Add the JWT token to the Authorization header
        connection.setRequestProperty("Authorization", "Bearer $jwtToken")
        connection.doOutput = true

        // Create JSON object for the profile update data
        val jsonBody = JSONObject()

        // Only add non-null AND non-empty values
        if (height != null && height > 0) {
            jsonBody.put("height", height)
        }
        if (weight != null && weight > 0) {
            jsonBody.put("weight", weight)
        }
        if (!password.isNullOrBlank()) {
            jsonBody.put("password", password)
        }

        // Only proceed if there are actual changes to send
        if (jsonBody.length() == 0) {
            return ProfileUpdateResult.Error("No changes to update")
        }

        // Write the JSON data to the output stream
        withContext(Dispatchers.IO) {
            OutputStreamWriter(connection.outputStream).apply {
                write(jsonBody.toString())
                flush()
                close()
            }
        }

        when (val responseCode = connection.responseCode) {
            HttpURLConnection.HTTP_OK -> {
                ProfileUpdateResult.Success
            }
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                ProfileUpdateResult.Error("Session expired. Please login again")
            }
            HttpURLConnection.HTTP_BAD_REQUEST -> {
                // Try to get error message from response
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                val errorMessage = errorResponse?.let {
                    try {
                        val jsonError = JSONObject(it)
                        jsonError.getString("message") ?: "Invalid input data"
                    } catch (e: Exception) {
                        "Invalid input data"
                    }
                } ?: "Invalid input data"
                ProfileUpdateResult.Error(errorMessage)
            }
            else -> {
                // Try to get error message from response
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                val errorMessage = errorResponse?.let {
                    try {
                        val jsonError = JSONObject(it)
                        jsonError.getString("message") ?: "Failed to update profile: $responseCode"
                    } catch (e: Exception) {
                        "Failed to update profile: $responseCode"
                    }
                } ?: "Failed to update profile: $responseCode"
                ProfileUpdateResult.Error(errorMessage)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        val errorMessage = when (e) {
            is java.net.ConnectException -> "Could not connect to server"
            is java.net.SocketTimeoutException -> "Connection timed out"
            is java.net.UnknownHostException -> "No internet connection"
            is javax.net.ssl.SSLHandshakeException -> "SSL certificate verification failed"
            else -> "Error updating profile: ${e.localizedMessage}"
        }
        ProfileUpdateResult.Error(errorMessage)
    } finally {
        connection.disconnect()
    }
}

// Helper function to create SSL context
private fun createSSLContext(context: Context): SSLContext {
    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    keyStore.load(null, null)

    context.resources.openRawResource(R.raw.cert).use { certInputStream ->
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificate = certificateFactory.generateCertificate(certInputStream)
        keyStore.setCertificateEntry("my_cert", certificate)
    }

    trustManagerFactory.init(keyStore)
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustManagerFactory.trustManagers, SecureRandom())
    return sslContext
}

// Helper function to create HTTPS connection
private fun createHttpsConnection(url: URL, context: Context): HttpsURLConnection {
    return (url.openConnection() as HttpsURLConnection).apply {
        sslSocketFactory = createSSLContext(context).socketFactory
        hostnameVerifier = HostnameVerifier { _, _ -> true }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEditProfileScreen() {
    EditProfileScreen(onSaveChoice = {}, onCancelChoice = {})
}