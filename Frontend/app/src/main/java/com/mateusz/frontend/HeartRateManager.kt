package com.mateusz.frontend

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.util.UUID

object HeartRateManager {
    private const val TAG = "HeartRateManager"
    private const val HEART_RATE_SERVICE_UUID = "0000180d-0000-1000-8000-00805f9b34fb"
    private const val HEART_RATE_MEASUREMENT_CHAR_UUID = "00002a37-0000-1000-8000-00805f9b34fb"

    private var bluetoothGatt: BluetoothGatt? = null

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    fun startHeartRateMonitoring(context: Context, onHeartRateReceived: (Int) -> Unit) {
        if (!checkPermissions(context)) {
            Log.e(TAG, "Missing required permissions")
            return
        }

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        val scanner = bluetoothAdapter.bluetoothLeScanner

        val scanCallback = object : ScanCallback() {
            @RequiresApi(Build.VERSION_CODES.S)
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (result.device?.name?.contains("InfiniTime", ignoreCase = true) == true) {
                    Log.d(TAG, "Found PineTime device: ${result.device.name}")
                    connectToDevice(context, result.device, onHeartRateReceived)
                }
            }
        }

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner.startScan(null, scanSettings, scanCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception while scanning: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    private fun connectToDevice(context: Context, device: BluetoothDevice, onHeartRateReceived: (Int) -> Unit) {
        if (!checkPermissions(context)) return

        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "Connected to GATT server")
                    try {
                        gatt.discoverServices()
                    } catch (e: SecurityException) {
                        Log.e(TAG, "Security exception discovering services: ${e.message}")
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt.getService(UUID.fromString(HEART_RATE_SERVICE_UUID))
                    val characteristic = service?.getCharacteristic(UUID.fromString(HEART_RATE_MEASUREMENT_CHAR_UUID))

                    if (characteristic != null) {
                        try {
                            gatt.setCharacteristicNotification(characteristic, true)
                        } catch (e: SecurityException) {
                            Log.e(TAG, "Security exception setting notification: ${e.message}")
                        }
                    }
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                if (characteristic.uuid == UUID.fromString(HEART_RATE_MEASUREMENT_CHAR_UUID)) {
                    val flag = value[0].toInt()
                    val heartRate = if (flag and 0x1 != 0) {
                        (value[1].toInt() and 0xFF) + (value[2].toInt() shl 8)
                    } else {
                        value[1].toInt() and 0xFF
                    }
                    Log.d(TAG, "Heart rate received: $heartRate")
                    onHeartRateReceived(heartRate)
                }
            }
        }

        try {
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception connecting to device: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopHeartRateMonitoring() {
        try {
            bluetoothGatt?.close()
            bluetoothGatt = null
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception closing GATT: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPermissions(context: Context): Boolean {
        val requiredPermissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )

        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}