package com.mateusz.frontend

import android.content.Context
import android.util.Base64
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.HostnameVerifier

@Composable
fun ProfileViewScreen(
    onGoBackChoice: () -> Unit,
    onEditProfileChoice: () -> Unit,
) {
    var name by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var birthDate by remember { mutableStateOf<String?>(null) }
    var gender by remember { mutableStateOf<String?>(null) }
    var weight by remember { mutableStateOf<Float?>(null) }
    var height by remember { mutableStateOf<Int?>(null) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val userData = fetchUserData(context)
        userData?.let { user ->
            name = user["name"]?.toString()
            email = user["email"]?.toString()
            birthDate = user["birth_date"]?.toString()
            gender = user["sex"]?.toString()
            weight = user["weight"]?.toString()?.toFloatOrNull()
            height = user["height"]?.toString()?.toIntOrNull()
        }
    }

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onGoBackChoice() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onEditProfileChoice() }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Info Card
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
                    // Name Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Name",
                            tint = Color(0xFF424242)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = name ?: "",
                            color = Color(0xFF424242),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Divider(color = Color(0xFF424242), thickness = 1.dp)

                    // Email Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = Color(0xFF424242)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = email ?: "",
                            color = Color(0xFF424242),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Divider(color = Color(0xFF424242), thickness = 1.dp)

                    // Birth Date Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cake,
                            contentDescription = "Birth Date",
                            tint = Color(0xFF424242)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = birthDate ?: "",
                            color = Color(0xFF424242),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Divider(color = Color(0xFF424242), thickness = 1.dp)

                    // Gender Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Gender",
                            tint = Color(0xFF424242)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = gender ?: "",
                            color = Color(0xFF424242),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Divider(color = Color(0xFF424242), thickness = 1.dp)

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
                        Text(
                            text = height?.let { "$it cm" } ?: "-",
                            color = Color(0xFF424242),
                            style = MaterialTheme.typography.bodyLarge
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
                        Text(
                            text = when {
                                weight == null || weight!!.isNaN() -> "-"
                                else -> "$weight kg"
                            },
                            color = Color(0xFF424242),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

fun getUserIdFromJwt(jwt: String): String? {
    return try {
        // Split the JWT into its parts
        val parts = jwt.split(".")
        if (parts.size != 3) return null

        // Decode the payload (the second part of the JWT)
        val payload = parts[1]
        val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
        val decodedString = String(decodedBytes)

        // Parse the JSON payload
        val jsonObject = JSONObject(decodedString)
        return jsonObject.optString("sub").takeIf { it.isNotEmpty() }
            ?: throw Exception("JWT does not contain user ID")
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private suspend fun fetchUserData(context: Context): Map<String, Any?>? {
    return withContext(Dispatchers.IO) {
        // Retrieve JWT token from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPreferences.getString("access_token", null)
            ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Please login to view profile", Toast.LENGTH_LONG).show()
                }
                return@withContext null
            }

        // Get user_id from JWT token
        val userId = getUserIdFromJwt(jwtToken)
            ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Invalid session. Please login again", Toast.LENGTH_LONG).show()
                }
                return@withContext null
            }

        val url = URL("${NetworkConfig.getBaseUrl()}/user/$userId")
        val connection = createHttpsConnection(url, context)

        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $jwtToken")

            when (val responseCode = connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    parseUserData(responseText)?.also {
                        // Cache the user data in SharedPreferences if needed
                        cacheUserData(context, it)
                    }
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Session expired. Please login again",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    null
                }
                HttpURLConnection.HTTP_NOT_FOUND -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "User profile not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    null
                }
                else -> {
                    // Try to get error message from response
                    val errorStream = connection.errorStream
                    val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                    val errorMessage = errorResponse?.let {
                        try {
                            val jsonError = JSONObject(it)
                            jsonError.getString("message")
                        } catch (e: Exception) {
                            "Failed to fetch user data: $responseCode"
                        }
                    } ?: "Failed to fetch user data: $responseCode"

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                    null
                }
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is java.net.ConnectException -> "Could not connect to server"
                is java.net.SocketTimeoutException -> "Connection timed out"
                is java.net.UnknownHostException -> "No internet connection"
                is javax.net.ssl.SSLHandshakeException -> "SSL certificate verification failed"
                else -> "Error fetching user data: ${e.localizedMessage}"
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
            e.printStackTrace()
            null
        } finally {
            connection.disconnect()
        }
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

private fun parseUserData(response: String): Map<String, Any?>? {
    return try {
        val jsonResponse = JSONObject(response)
        mapOf(
            "birth_date" to jsonResponse.getString("birth_date"),
            "email" to jsonResponse.getString("email"),
            "height" to jsonResponse.optInt("height").takeIf { it != 0 },
            "name" to jsonResponse.getString("name"),
            "sex" to jsonResponse.getString("sex"),
            "weight" to jsonResponse.optDouble("weight").takeIf { it != 0.0 }
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Optional: Cache user data in SharedPreferences
private fun cacheUserData(context: Context, userData: Map<String, Any?>) {
    context.getSharedPreferences("user_data", Context.MODE_PRIVATE).edit().apply {
        userData.forEach { (key, value) ->
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Double -> putFloat(key, value.toFloat())
            }
        }
        apply()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileViewScreen() {
    ProfileViewScreen(
        onGoBackChoice = {},
        onEditProfileChoice = {}
    )
}