package com.mateusz.frontend

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log

object GadgetbridgeDatabase {
    private const val TAG = "GadgetbridgeDatabase"
    private val AUTHORITIES = listOf(
        "nodomain.freeyourgadget.gadgetbridge.provider",
        "nodomain.freeyourgadget.gadgetbridge"
    )
    private const val TABLE_NAME = "HEARTRATE"

    fun getLatestHeartRate(context: Context): Int {
        Log.d(TAG, "Getting latest heart rate...")

        AUTHORITIES.forEach { authority ->
            try {
                // Try different URI patterns
                val uris = listOf(
                    "content://$authority/$TABLE_NAME",
                    "content://$authority/heartrate",
                    "content://$authority/measurements"
                )

                for (uriString in uris) {
                    try {
                        val uri = Uri.parse(uriString)
                        Log.d(TAG, "Trying URI: $uri")

                        val cursor = context.contentResolver.query(
                            uri,
                            null,  // Get all columns
                            null,  // No selection
                            null,  // No selection args
                            "timestamp DESC" // Latest first
                        )

                        cursor?.use {
                            Log.d(TAG, "Got cursor for $uriString with ${it.count} rows")
                            logCursorMetadata(it)

                            if (it.moveToFirst()) {
                                // Try to find heart rate column
                                val columnNames = it.columnNames.joinToString()
                                Log.d(TAG, "Available columns: $columnNames")

                                // Try different possible column names
                                val heartRate = when {
                                    hasColumn(it, "HEART_RATE") -> it.getInt(it.getColumnIndexOrThrow("HEART_RATE"))
                                    hasColumn(it, "VALUE") -> it.getInt(it.getColumnIndexOrThrow("VALUE"))
                                    hasColumn(it, "MEASUREMENT") -> it.getInt(it.getColumnIndexOrThrow("MEASUREMENT"))
                                    else -> null
                                }

                                if (heartRate != null && heartRate > 0) {
                                    Log.d(TAG, "Found heart rate: $heartRate from $uriString")
                                    return heartRate
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error with URI $uriString: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error with authority $authority: ${e.message}")
            }
        }

        return 0
    }

    private fun logCursorMetadata(cursor: Cursor) {
        val columnNames = cursor.columnNames
        Log.d(TAG, "Cursor columns: ${columnNames.joinToString()}")
        if (cursor.moveToFirst()) {
            val rowData = columnNames.mapNotNull { col ->
                val index = cursor.getColumnIndex(col)
                if (index >= 0) {
                    try {
                        "$col: ${cursor.getString(index)}"
                    } catch (e: Exception) {
                        "$col: <error reading value>"
                    }
                } else {
                    null
                }
            }
            Log.d(TAG, "First row: ${rowData.joinToString()}")
        }
    }

    private fun hasColumn(cursor: Cursor, columnName: String): Boolean {
        return cursor.getColumnIndex(columnName) >= 0
    }
}