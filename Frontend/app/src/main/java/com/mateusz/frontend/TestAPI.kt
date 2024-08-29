package com.mateusz.frontend

import java.net.HttpURLConnection
import java.net.URL

fun main() {
    // The URL to send the GET request to
    val url = URL("http://127.0.0.1:5000/test")

    // Open a connection to the URL
    val connection = url.openConnection() as HttpURLConnection

    try {
        // Set the request method to GET
        connection.requestMethod = "GET"

        // Get the response code
        val responseCode = connection.responseCode

        // If the response code is 200 (HTTP_OK), read the response
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            println("Response: $response")
        } else {
            println("GET request failed. Response Code: $responseCode")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        // Disconnect the connection
        connection.disconnect()
    }
}
