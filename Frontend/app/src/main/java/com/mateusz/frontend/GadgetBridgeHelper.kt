package com.mateusz.frontend

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast

object GadgetBridgeHelper {
    const val GADGETBRIDGE_PACKAGE = "nodomain.freeyourgadget.gadgetbridge"
    private const val TAG = "GadgetBridgeHelper"

    fun isGadgetBridgeInstalled(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            packageManager.getPackageInfo(GADGETBRIDGE_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getLatestHeartRate(context: Context): Int {
        try {
            val uri = Uri.parse("content://nodomain.freeyourgadget.gadgetbridge.provider/measurements")
            Log.d(TAG, "Querying content provider: $uri")

            context.contentResolver.query(
                uri,
                arrayOf("MEASUREMENT"),  // only get the measurement column
                "KIND = ?",              // where KIND equals
                arrayOf("HEART_RATE"),   // HEART_RATE
                "TIMESTAMP DESC"         // ordered by timestamp, newest first
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val measurementIndex = cursor.getColumnIndex("MEASUREMENT")
                    if (measurementIndex != -1) {
                        val heartRate = cursor.getInt(measurementIndex)
                        Log.d(TAG, "Found heart rate value: $heartRate")
                        return heartRate
                    } else {
                        Log.d(TAG, "MEASUREMENT column not found")
                    }
                } else {
                    Log.d(TAG, "No heart rate records found")
                }
            } ?: Log.d(TAG, "Cursor is null")
        } catch (e: Exception) {
            Log.e(TAG, "Error reading heart rate: ${e.message}", e)
        }
        return 0
    }

    fun openGadgetBridge(context: Context) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(GADGETBRIDGE_PACKAGE)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Gadgetbridge: ${e.message}")
            Toast.makeText(
                context,
                "Could not open Gadgetbridge",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}