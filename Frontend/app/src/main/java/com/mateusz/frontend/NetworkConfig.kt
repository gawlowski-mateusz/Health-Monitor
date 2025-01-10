package com.mateusz.frontend

import android.os.Build

object NetworkConfig {
    private const val EMULATOR_URL = "https://10.0.2.2:443"
//    private const val DEVICE_URL = "http://192.168.0.192:8000"
//    private const val DEVICE_URL = "http://192.168.0.59:8000"
//    private const val DEVICE_URL = "http://192.168.119.41:8000"
//    private const val DEVICE_URL = "https://192.168.106.41:443"
    private const val DEVICE_URL = "https://192.168.0.193:443"

    fun getBaseUrl(): String {
        return if (isEmulator()) EMULATOR_URL else DEVICE_URL
    }

    fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
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
    }
}