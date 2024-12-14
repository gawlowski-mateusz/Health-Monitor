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
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.util.UUID

object HeartRateManager {
    private const val TAG = "HeartRateManager"
    private const val HEART_RATE_SERVICE_UUID = "0000180d-0000-1000-8000-00805f9b34fb"
    private const val HEART_RATE_MEASUREMENT_CHAR_UUID = "00002a37-0000-1000-8000-00805f9b34fb"
    private const val RECONNECTION_DELAY = 5000L // 5 seconds

    private var bluetoothGatt: BluetoothGatt? = null
    private var scanner: android.bluetooth.le.BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null
    private var isConnecting = false
    private var shouldReconnect = true
    private val handler = Handler(Looper.getMainLooper())
    private var lastDeviceAddress: String? = null
    private var currentCallback: ((Int) -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    fun startHeartRateMonitoring(context: Context, onHeartRateReceived: (Int) -> Unit) {
        if (!checkPermissions(context)) {
            Log.e(TAG, "Missing required permissions")
            return
        }

        currentCallback = onHeartRateReceived
        shouldReconnect = true

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        // If we have a last known device, try to connect to it first
        lastDeviceAddress?.let { address ->
            try {
                val device = bluetoothAdapter.getRemoteDevice(address)
                connectToDevice(context, device, onHeartRateReceived)
                return
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reconnect to last known device: ${e.message}")
            }
        }

        // If no last known device or reconnection failed, start scanning
        scanner = bluetoothAdapter.bluetoothLeScanner
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (result.device?.name?.contains("InfiniTime", ignoreCase = true) == true) {
                    Log.d(TAG, "Found PineTime device: ${result.device.name}")
                    stopScanning()
                    connectToDevice(context, result.device, onHeartRateReceived)
                    lastDeviceAddress = result.device.address
                }
            }
        }

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner?.startScan(null, scanSettings, scanCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception while scanning: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopScanning() {
        try {
            scanCallback?.let { scanner?.stopScan(it) }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception stopping scan: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    private fun connectToDevice(context: Context, device: BluetoothDevice, onHeartRateReceived: (Int) -> Unit) {
        if (!checkPermissions(context) || isConnecting) return

        isConnecting = true

        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d(TAG, "Connected to GATT server")
                        isConnecting = false
                        try {
                            gatt.discoverServices()
                        } catch (e: SecurityException) {
                            Log.e(TAG, "Security exception discovering services: ${e.message}")
                            disconnect()
                            scheduleReconnect(context)
                        }
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d(TAG, "Disconnected from GATT server")
                        isConnecting = false
                        disconnect()
                        if (shouldReconnect) {
                            scheduleReconnect(context)
                        }
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
                            disconnect()
                            scheduleReconnect(context)
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
            isConnecting = false
            scheduleReconnect(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun scheduleReconnect(context: Context) {
        if (shouldReconnect) {
            handler.postDelayed({
                currentCallback?.let { callback ->
                    startHeartRateMonitoring(context, callback)
                }
            }, RECONNECTION_DELAY)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopHeartRateMonitoring() {
        shouldReconnect = false
        handler.removeCallbacksAndMessages(null)
        stopScanning()

        try {
            bluetoothGatt?.let { gatt ->
                gatt.disconnect()
                gatt.close()
            }
            bluetoothGatt = null
            isConnecting = false
            currentCallback = null
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception closing GATT: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    private fun disconnect() {
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during disconnect: ${e.message}")
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