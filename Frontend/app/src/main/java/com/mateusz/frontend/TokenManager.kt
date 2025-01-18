package com.mateusz.frontend

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object TokenManager {
    private const val TAG = "TokenManager"
    private const val AUTH_PREFS = "auth"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"

    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .apply()
        Log.d(TAG, "Tokens saved successfully")
    }

    fun getAccessToken(context: Context): String? {
        return context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
            .getString(ACCESS_TOKEN_KEY, null)
    }

    private fun getRefreshToken(context: Context): String? {
        return context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
            .getString(REFRESH_TOKEN_KEY, null)
    }

    private fun clearTokens(context: Context) {
        context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        Log.d(TAG, "Tokens cleared")
    }

    suspend fun refreshToken(context: Context): Boolean {
        val TAG = "TokenManager"
        val refreshToken = getRefreshToken(context)

        if (refreshToken == null) {
            Log.e(TAG, "No refresh token available")
            return false
        }

        Log.d(TAG, "Starting token refresh")

        val url = URL("${NetworkConfig.getBaseUrl()}/refresh")

        return try {
            val connection = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpsURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            // Make sure the Authorization header format matches exactly what Flask-JWT-Extended expects
            connection.setRequestProperty("Authorization", "Bearer $refreshToken")
            connection.doOutput = true
            connection.doInput = true  // Ensure we can read the response

            // Send an empty JSON body
            withContext(Dispatchers.IO) {
                connection.outputStream.use { os ->
                    val input = "{}".toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "Refresh token response code: $responseCode")

            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "Token refresh successful: ${response.take(50)}...")

                    try {
                        val jsonResponse = JSONObject(response)
                        val newAccessToken = jsonResponse.getString("access_token")
                        val newRefreshToken = jsonResponse.getString("refresh_token")

                        saveTokens(context, newAccessToken, newRefreshToken)
                        Log.d(TAG, "New tokens saved successfully")
                        true
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse token refresh response", e)
                        false
                    }
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e(TAG, "Unauthorized during refresh: $errorStream")
                    clearTokens(context)
                    false
                }
                else -> {
                    val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e(TAG, "Unexpected response during refresh: $errorStream")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during token refresh", e)
            false
        }
    }
}