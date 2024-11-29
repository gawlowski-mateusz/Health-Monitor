package com.mateusz.frontend

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast

object GadgetBridgeHelper {
    const val GADGETBRIDGE_PACKAGE = "nodomain.freeyourgadget.gadgetbridge"
    private const val TAG = "GadgetBridgeHelper"

    fun isGadgetBridgeInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(GADGETBRIDGE_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun openGadgetBridgeHeartRate(context: Context) {
        try {
            // Try to open heart rate activity directly
            val intent = Intent().apply {
                component = ComponentName(
                    GADGETBRIDGE_PACKAGE,
                    "$GADGETBRIDGE_PACKAGE.activities.HeartRateActivity"
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Gadgetbridge heart rate: ${e.message}")
            // Fallback to main activity
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
                    "Could not open Gadgetbridge. Please start heart rate measurement manually.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun listAllInstalledPackages(context: Context) {
        val packageManager = context.packageManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val availableModules = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                Log.d(TAG, "=== START LISTING ALL PACKAGES (${availableModules.size} found) ===")
                availableModules.forEach { appInfo ->
                    val appName = appInfo.loadLabel(packageManager).toString()
                    Log.d(TAG, "App: $appName | Package: ${appInfo.packageName}")
                    // Also print to standard output for easier viewing
                    println("App: $appName | Package: ${appInfo.packageName}")
                }
            } else {
                val packages = packageManager.getInstalledPackages(0)
                Log.d(TAG, "=== START LISTING ALL PACKAGES (${packages.size} found) ===")
                packages.forEach { packageInfo ->
                    val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                    Log.d(TAG, "App: $appName | Package: ${packageInfo.packageName}")
                    println("App: $appName | Package: ${packageInfo.packageName}")
                }
            }
            Log.d(TAG, "=== END LISTING ALL PACKAGES ===")
        } catch (e: Exception) {
            Log.e(TAG, "Error listing packages: ${e.message}")
            Log.e(TAG, "Stack trace:", e)
        }
    }

    fun openGadgetBridge(context: Context) {
        try {
            // Try to launch Gadgetbridge
            val intent = Intent().apply {
                setPackage(GADGETBRIDGE_PACKAGE)
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching Gadgetbridge: ${e.message}")
            Toast.makeText(context, "Error launching Gadgetbridge: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}