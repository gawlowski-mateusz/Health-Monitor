package com.mateusz.frontend

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
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
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory


@Composable
private fun PasswordValidation(
    password: String,
    onPasswordChange: (String) -> Unit,
    onPasswordStrengthChange: (String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
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
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToHomeScreen: () -> Unit
) {
    val context = LocalContext.current
    var registerResult by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()
    var showGenderMenu by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordStrength by remember { mutableStateOf("empty") }
    var birthDateInput by remember { mutableStateOf<LocalDate?>(null) }
    var gender: String? by remember { mutableStateOf(null) }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 48.dp, end = 48.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.30f),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.health_monitor_logo_simple),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(150.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.60f)
                .padding(start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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

                PasswordValidation(
                    password = password,
                    onPasswordChange = { password = it },
                    onPasswordStrengthChange = { passwordStrength = it }
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
                        text = birthDateInput?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            ?: "Birth Date (YYYY-MM-DD)",
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
                    if (gender == null) {
                        Text(
                            text = "Gender (Male/Female)",
                            color = Color.Black
                        )
                    } else {
                        Text(
                            text = gender!!,
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
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.20f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        // Only allow registration if password is strong enough
                        if (passwordStrength == "weak") {
                            Toast.makeText(
                                context,
                                "Please ensure password meets all requirements",
                                Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }

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
                                    gender?.let {
                                        makeRegisterRequest(
                                            name,
                                            email,
                                            password,
                                            birthDate,
                                            it,
                                            weight,
                                            height,
                                            context
                                        )
                                    }
                                withContext(Dispatchers.Main) {
                                    if (result != null) {
                                        registerResult = result
                                    }
                                    if (result == "Success") {
                                        onRegisterSuccess()
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
                        "Register",
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
    }
}

suspend fun makeRegisterRequest(
    name: String,
    email: String,
    password: String,
    birthDate: Date?,
    gender: String,
    weight: String?,
    height: String?,
    context: Context,
    testConnection: HttpsURLConnection? = null
): String {
    val url = URL("${NetworkConfig.getBaseUrl()}/register")

    // Create SSL context with the custom certificate
    val trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    withContext(Dispatchers.IO) {
        keyStore.load(null, null)
    }

    // Load the certificate from raw resources
    context.resources.openRawResource(R.raw.cert).use { certInputStream ->
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificate = certificateFactory.generateCertificate(certInputStream)
        keyStore.setCertificateEntry("my_cert", certificate)
    }

    trustManagerFactory.init(keyStore)

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustManagerFactory.trustManagers, SecureRandom())

    // Cast to HttpsURLConnection and configure SSL
    val connection = testConnection ?: withContext(Dispatchers.IO) {
        (URL("${NetworkConfig.getBaseUrl()}/register").openConnection() as HttpsURLConnection).apply {
            sslSocketFactory = sslContext.socketFactory
            hostnameVerifier = HostnameVerifier { _, _ -> true }
        }
    }

    return try {
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val birthDateStr = birthDate?.let { sdf.format(it) }

        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("name", name)
            put("birth_date", birthDateStr)
            put("sex", gender)
            weight?.toIntOrNull()?.let { put("weight", it) }
            height?.toIntOrNull()?.let { put("height", it) }
        }

        println("Sending registration data: $jsonBody")

        withContext(Dispatchers.IO) {
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }
        }

        val responseCode = connection.responseCode
        println("Response code: $responseCode")

        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            println("Success response: $response")
            "Success"
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() }?.let { errorResponse ->
                println("Error response: $errorResponse")
                try {
                    JSONObject(errorResponse).getString("message")
                        ?: "Failed with response code $responseCode"
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
            is javax.net.ssl.SSLHandshakeException -> "Error: SSL certificate verification failed"
            else -> "Error: ${e.localizedMessage}"
        }
    } finally {
        connection.disconnect()
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    RegisterScreen(onRegisterSuccess = { }) {
    }
}
