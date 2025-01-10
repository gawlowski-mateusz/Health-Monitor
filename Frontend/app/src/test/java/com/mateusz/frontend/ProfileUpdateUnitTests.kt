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
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.SSLSocketFactory
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.security.cert.CertificateFactory
import org.json.JSONObject
import java.net.HttpURLConnection

class ProfileUpdateUnitTests {
    private lateinit var mockContext: Context
    private lateinit var mockSharedPrefs: SharedPreferences
    private lateinit var mockResources: Resources
    private lateinit var mockConnection: HttpsURLConnection
    private lateinit var mockCertFactory: CertificateFactory
    private lateinit var mockX509Certificate: X509Certificate
    private lateinit var mockKeyStore: KeyStore
    private lateinit var mockTrustManagerFactory: TrustManagerFactory
    private lateinit var mockSslContext: SSLContext
    private lateinit var mockSocketFactory: SSLSocketFactory

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockSharedPrefs = mockk(relaxed = true)
        mockResources = mockk(relaxed = true)
        mockConnection = mockk(relaxed = true)
        mockCertFactory = mockk(relaxed = true)
        mockX509Certificate = mockk(relaxed = true)
        mockKeyStore = mockk(relaxed = true)
        mockTrustManagerFactory = mockk(relaxed = true)
        mockSslContext = mockk(relaxed = true)
        mockSocketFactory = mockk(relaxed = true)

        // Mock SharedPreferences
        every { mockContext.getSharedPreferences("auth", Context.MODE_PRIVATE) } returns mockSharedPrefs
        every { mockSharedPrefs.getString("access_token", any()) } returns "test_token"

        // Mock SSL infrastructure
        mockkStatic(CertificateFactory::class)
        every { CertificateFactory.getInstance("X.509") } returns mockCertFactory
        every { mockCertFactory.generateCertificate(any()) } returns mockX509Certificate

        mockkStatic(KeyStore::class)
        every { KeyStore.getInstance(KeyStore.getDefaultType()) } returns mockKeyStore
        every { mockKeyStore.load(null, null) } just runs
        every { mockKeyStore.setCertificateEntry(any(), mockX509Certificate) } just runs

        mockkStatic(TrustManagerFactory::class)
        every { TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()) } returns mockTrustManagerFactory
        every { TrustManagerFactory.getDefaultAlgorithm() } returns "X509"
        every { mockTrustManagerFactory.init(any<KeyStore>()) } just runs
        every { mockTrustManagerFactory.trustManagers } returns arrayOf(mockk(relaxed = true))

        mockkStatic(SSLContext::class)
        every { SSLContext.getInstance("TLS") } returns mockSslContext
        every { mockSslContext.init(null, any(), any()) } just runs
        every { mockSslContext.socketFactory } returns mockSocketFactory

        // Mock Resources
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

        // Mock JSONObject
        mockkConstructor(JSONObject::class)
        every { anyConstructed<JSONObject>().put(any(), any<Int>()) } returns mockk()
        every { anyConstructed<JSONObject>().put(any(), any<Float>()) } returns mockk()
        every { anyConstructed<JSONObject>().put(any(), any<String>()) } returns mockk()
        every { anyConstructed<JSONObject>().toString() } returns "{}"
        every { anyConstructed<JSONObject>().length() } returns 1
    }

    @Test
    fun `test successful profile update with all fields`() = runBlocking {
        // Setup connection expectations
        with(mockConnection) {
            every { requestMethod = any() } just runs
            every { setRequestProperty(any(), any()) } just runs
            every { doOutput = any() } just runs
            every { outputStream } returns ByteArrayOutputStream()
            every { responseCode } returns HttpURLConnection.HTTP_OK
            every { disconnect() } just runs
            every { sslSocketFactory = any() } just runs
            every { hostnameVerifier = any() } just runs
        }

        // Execute update request
        val result = makeEditProfileRequest(
            height = 180,
            weight = 75.5f,
            password = "newPassword123",
            context = mockContext,
            testConnection = mockConnection
        )

        // Verify success
        assertTrue(result is ProfileUpdateResult.Success)
    }

    @Test
    fun `test update with no changes`() = runBlocking {
        // Mock JSONObject to return empty length
        every { anyConstructed<JSONObject>().length() } returns 0

        // Execute update request with null values
        val result = makeEditProfileRequest(
            height = null,
            weight = null,
            password = null,
            context = mockContext,
            testConnection = mockConnection
        )

        // Verify error
        assertTrue(result is ProfileUpdateResult.Error)
        assertEquals("No changes to update", (result as ProfileUpdateResult.Error).message)
    }

    @Test
    fun `test update with invalid height`() = runBlocking {
        // Setup connection to return bad request
        val errorResponse = """{"message": "Height must be a positive number"}"""

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
        every { anyConstructed<JSONObject>().getString("message") } returns "Height must be a positive number"

        // Execute update request with negative height
        val result = makeEditProfileRequest(
            height = -180,
            weight = null,
            password = null,
            context = mockContext,
            testConnection = mockConnection
        )

        // Verify error
        assertTrue(result is ProfileUpdateResult.Error)
        assertEquals("Height must be a positive number", (result as ProfileUpdateResult.Error).message)
    }

    @Test
    fun `test update with invalid weight`() = runBlocking {
        // Setup connection to return bad request
        val errorResponse = """{"message": "Weight must be a positive number"}"""

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
        every { anyConstructed<JSONObject>().getString("message") } returns "Weight must be a positive number"

        // Execute update request with negative weight
        val result = makeEditProfileRequest(
            height = null,
            weight = -75.5f,
            password = null,
            context = mockContext,
            testConnection = mockConnection
        )

        // Verify error
        assertTrue(result is ProfileUpdateResult.Error)
        assertEquals("Weight must be a positive number", (result as ProfileUpdateResult.Error).message)
    }

    @Test
    fun `test update with weak password`() = runBlocking {
        // Setup connection to return bad request
        val errorResponse = """{"message": "Password must be at least 8 characters long"}"""

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
        every { anyConstructed<JSONObject>().getString("message") } returns "Password must be at least 8 characters long"

        // Execute update request with short password
        val result = makeEditProfileRequest(
            height = null,
            weight = null,
            password = "123",
            context = mockContext,
            testConnection = mockConnection
        )

        // Verify error
        assertTrue(result is ProfileUpdateResult.Error)
        assertEquals("Password must be at least 8 characters long", (result as ProfileUpdateResult.Error).message)
    }

    @Test
    fun `test update without authentication`() = runBlocking {
        // Mock missing authentication token
        every { mockSharedPrefs.getString("access_token", any()) } returns null

        // Execute update request
        val result = makeEditProfileRequest(
            height = 180,
            weight = 75.5f,
            password = "newPassword123",
            context = mockContext,
            testConnection = mockConnection
        )

        // Verify error
        assertTrue(result is ProfileUpdateResult.Error)
        assertEquals("Please login to update your profile", (result as ProfileUpdateResult.Error).message)
    }

    @Test
    fun `test update with expired token`() = runBlocking {
        // Setup connection to return unauthorized
        with(mockConnection) {
            every { requestMethod = any() } just runs
            every { setRequestProperty(any(), any()) } just runs
            every { doOutput = any() } just runs
            every { outputStream } returns ByteArrayOutputStream()
            every { responseCode } returns HttpURLConnection.HTTP_UNAUTHORIZED
            every { disconnect() } just runs
            every { sslSocketFactory = any() } just runs
            every { hostnameVerifier = any() } just runs
        }

        // Execute update request
        val result = makeEditProfileRequest(
            height = 180,
            weight = 75.5f,
            password = "newPassword123",
            context = mockContext,
            testConnection = mockConnection
        )

        // Verify error
        assertTrue(result is ProfileUpdateResult.Error)
        assertEquals("Session expired. Please login again", (result as ProfileUpdateResult.Error).message)
    }

    @Test
    fun `test update with network error`() = runBlocking {
        // Setup connection to throw network error
        every { mockConnection.requestMethod = any() } throws java.net.UnknownHostException()

        // Execute update request
        val result = makeEditProfileRequest(
            height = 180,
            weight = 75.5f,
            password = "newPassword123",
            context = mockContext,
            testConnection = mockConnection
        )

        // Verify error
        assertTrue(result is ProfileUpdateResult.Error)
        assertEquals("No internet connection", (result as ProfileUpdateResult.Error).message)
    }

    @Test
    fun `test update with SSL error`() = runBlocking {
        // Setup connection to throw SSL error
        every { mockConnection.requestMethod = any() } throws javax.net.ssl.SSLHandshakeException("Invalid certificate")

        // Execute update request
        val result = makeEditProfileRequest(
            height = 180,
            weight = 75.5f,
            password = "newPassword123",
            context = mockContext,
            testConnection = mockConnection
        )

        // Verify error
        assertTrue(result is ProfileUpdateResult.Error)
        assertEquals("SSL certificate verification failed", (result as ProfileUpdateResult.Error).message)
    }
}