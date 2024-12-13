package com.mateusz.frontend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class HeartRateReceiver(private val onHeartRateReceived: (Int) -> Unit) : BroadcastReceiver() {
    companion object {
        private const val TAG = "HeartRateReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "!!!! RECEIVED BROADCAST !!!!")
        Log.d(TAG, "Action: ${intent?.action}")
        Log.d(TAG, "Extras: ${intent?.extras?.keySet()?.joinToString()}")

        try {
            // Log ALL data from intent
            intent?.extras?.keySet()?.forEach { key ->
                val value = intent.extras?.get(key)
                Log.d(TAG, "Extra '$key' = '$value' (${value?.javaClass?.simpleName})")
            }

            // Notify the user we received something (for debugging)
            val toast = Toast.makeText(context, "Received heart rate broadcast!", Toast.LENGTH_SHORT)
            toast.show()

            // Try both ways to get heart rate
            val heartRate = when {
                intent?.hasExtra("heartrate") == true -> intent.getIntExtra("heartrate", -1)
                intent?.getStringExtra("value")?.contains("HeartRate=") == true -> {
                    intent.getStringExtra("value")?.substringAfter("HeartRate=")?.toIntOrNull() ?: -1
                }
                else -> -1
            }

            Log.d(TAG, "Parsed heart rate: $heartRate")

            if (heartRate > 0) {
                Log.d(TAG, "Valid heart rate received: $heartRate")
                onHeartRateReceived(heartRate)
                Toast.makeText(context, "Heart Rate: $heartRate", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing heart rate: ${e.message}", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}