package com.mateusz.frontend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class HeartRateReceiver(private val onHeartRateReceived: (Int) -> Unit) : BroadcastReceiver() {
    companion object {
        private const val TAG = "HeartRateReceiver"
        val ACTIONS = listOf(
            "nodomain.freeyourgadget.gadgetbridge.ACTION_HEARTRATE",
            "nodomain.freeyourgadget.gadgetbridge.ACTION_REALTIME_HEARTRATE"
        )
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "=================== START HEART RATE RECEIVE ===================")
        Log.d(TAG, "Received intent action: ${intent?.action}")

        intent?.extras?.let { extras ->
            Log.d(TAG, "All extras keys: ${extras.keySet().joinToString()}")
            extras.keySet().forEach { key ->
                Log.d(TAG, "Extra '$key' = '${extras.get(key)}'")
            }

            // Try all possible heart rate keys
            val heartRate = extras.get("heart_rate")?.toString()?.toIntOrNull()
                ?: extras.get("HEART_RATE_VALUE")?.toString()?.toIntOrNull()
                ?: extras.get("heartrate")?.toString()?.toIntOrNull()
                ?: extras.get("EXTRA_HR_DATA")?.toString()?.toIntOrNull()
                ?: -1

            Log.d(TAG, "Parsed heart rate: $heartRate")

            if (heartRate > 0) {
                Log.d(TAG, "Calling onHeartRateReceived with value: $heartRate")
                onHeartRateReceived(heartRate)
                Log.d(TAG, "Successfully called onHeartRateReceived")
            }
        } ?: Log.d(TAG, "No extras in intent")

        Log.d(TAG, "=================== END HEART RATE RECEIVE ===================")
    }
}