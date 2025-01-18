package com.mateusz.frontend

import android.os.Build
import android.util.Log

object NetworkConfig {
    private const val TAG = "NetworkConfig"
    private const val EMULATOR_URL = "https://10.0.2.2:443"
    private const val PRODUCTION_URL = "https://health-monitor-production.up.railway.app"

    fun getBaseUrl(): String {
        val baseUrl = if (isEmulator()) EMULATOR_URL else PRODUCTION_URL
        Log.d(TAG, "Using base URL: $baseUrl")
        return baseUrl
    }

    fun isEmulator(): Boolean {
        val isEmulator = (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("sdk_gphone64_arm64")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator"))
        Log.d(TAG, "Is emulator: $isEmulator")
        return isEmulator
    }
}