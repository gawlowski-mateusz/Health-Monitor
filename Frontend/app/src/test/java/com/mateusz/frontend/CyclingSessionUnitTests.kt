package com.mateusz.frontend

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.net.ssl.HttpsURLConnection
import org.json.JSONObject
import java.net.HttpURLConnection

class CyclingSessionUnitTests {
    private lateinit var mockContext: Context
    private lateinit var mockSharedPrefs: SharedPreferences
    private lateinit var mockResources: Resources
    private lateinit var mockConnection: HttpsURLConnection

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockSharedPrefs = mockk(relaxed = true)
        mockResources = mockk(relaxed = true)
        mockConnection = mockk(relaxed = true)

        // Mock SharedPreferences
        every { mockContext.getSharedPreferences("auth", Context.MODE_PRIVATE) } returns mockSharedPrefs
        every { mockSharedPrefs.getString("access_token", any()) } returns "test_token"

        // Mock Resources with certificate input stream
        val dummyCertData = """
            -----BEGIN CERTIFICATE-----
            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
            rQwq8SeFySdNBJ0pEl4lwqQ=
            -----END CERTIFICATE-----
        """.trimIndent().toByteArray()

        every { mockContext.resources } returns mockResources
        every { mockResources.openRawResource(any()) } returns ByteArrayInputStream(dummyCertData)

        // Mock NetworkConfig
        mockkObject(NetworkConfig)
        every { NetworkConfig.getBaseUrl() } returns "https://10.0.2.2:443"
        every { NetworkConfig.isEmulator() } returns true
    }

    @Test
    fun `test successful cycling session addition`() = runBlocking {
        // Setup connection expectations
        with(mockConnection) {
            every { requestMethod = any() } just runs
            every { setRequestProperty(any(), any()) } just runs
            every { doOutput = any() } just runs
            every { outputStream } returns ByteArrayOutputStream()
            every { responseCode } returns HttpURLConnection.HTTP_CREATED
            every { disconnect() } just runs
            every { sslSocketFactory = any() } just runs
            every { hostnameVerifier = any() } just runs
        }

        // Mock JSONObject for success case
        mockkConstructor(JSONObject::class)
        every { anyConstructed<JSONObject>().put(any(), any<Int>()) } returns mockk()
        every { anyConstructed<JSONObject>().toString() } returns "{\"duration\":30,\"average_pulse\":140}"

        // Execute request
        val result = makeAddNewCyclingSessionRequest(
            duration = 30,
            averagePulse = 140,
            password = null,
            context = mockContext,
            testConnection = mockConnection
        )

        // Verify success
        assertTrue(result is CyclingSessionResult.Success)
    }

    @Test
    fun `test network error cycling session addition`() = runBlocking {
        // Setup connection to throw network error immediately
        every { mockConnection.setRequestProperty(any(), any()) } throws java.net.UnknownHostException()

        // Execute request
        val result = makeAddNewCyclingSessionRequest(
            duration = 30,
            averagePulse = 140,
            password = null,
            context = mockContext,
            testConnection = mockConnection
        )

        // Verify error
        assertTrue(result is CyclingSessionResult.Error)
        assertEquals("No internet connection", (result as CyclingSessionResult.Error).message)
    }

    @Test
    fun `test SSL error cycling session addition`() = runBlocking {
        // Setup connection to throw SSL error immediately
        every { mockConnection.setRequestProperty(any(), any()) } throws javax.net.ssl.SSLHandshakeException("Invalid certificate")

        // Execute request
        val result = makeAddNewCyclingSessionRequest(
            duration = 30,
            averagePulse = 140,
            password = null,
            context = mockContext,
            testConnection = mockConnection
        )

        // Verify error
        assertTrue(result is CyclingSessionResult.Error)
        assertEquals("SSL certificate verification failed", (result as CyclingSessionResult.Error).message)
    }

    @Test
    fun `test invalid number format`() = runBlocking {
        // Setup connection to throw number format error
        every { mockConnection.setRequestProperty(any(), any()) } throws NumberFormatException("Invalid number")

        // Execute request
        val result = makeAddNewCyclingSessionRequest(
            duration = -1,
            averagePulse = 140,
            password = null,
            context = mockContext,
            testConnection = mockConnection
        )

        // Verify error
        assertTrue(result is CyclingSessionResult.Error)
        assertEquals("Invalid number format for duration or pulse", (result as CyclingSessionResult.Error).message)
    }

    @Test
    fun `test invalid cycling session data`() = runBlocking {
        // Prepare mock error response
        val errorResponse = """{"message": "Invalid pulse rate"}"""

        with(mockConnection) {
            every { requestMethod = any() } just runs
            every { setRequestProperty(any(), any()) } just runs
            every { doOutput = any() } just runs
            every { outputStream } returns ByteArrayOutputStream()
            every { responseCode } returns HttpURLConnection.HTTP_BAD_REQUEST
            every { errorStream } returns ByteArrayInputStream(errorResponse.toByteArray())
            every { disconnect() } just runs
            every { sslSocketFactory = any() } just runs
            every { hostnameVerifier = any() } just runs
        }

        // Mock JSONObject for error response
        mockkConstructor(JSONObject::class)
        every { anyConstructed<JSONObject>().getString("message") } returns "Invalid pulse rate"

        // Execute request
        val result = makeAddNewCyclingSessionRequest(
            duration = 30,
            averagePulse = 300,
            password = null,
            context = mockContext,
            testConnection = mockConnection
        )

        // Verify error
        assertTrue(result is CyclingSessionResult.Error)
        assertEquals("Invalid pulse rate", (result as CyclingSessionResult.Error).message)
    }
}