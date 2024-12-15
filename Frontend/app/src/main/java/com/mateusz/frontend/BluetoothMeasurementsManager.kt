package com.mateusz.frontend

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
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

object BluetoothMeasurementsManager {
    private const val TAG = "BluetoothMeasurementsManager"

    // Heart Rate UUIDs
    private const val HEART_RATE_SERVICE_UUID = "0000180d-0000-1000-8000-00805f9b34fb"
    private const val HEART_RATE_MEASUREMENT_CHAR_UUID = "00002a37-0000-1000-8000-00805f9b34fb"

    // InfiniTime Step Counter UUIDs
    private const val MOTION_SERVICE_UUID = "00030000-78fc-48fe-8e23-433b3a1942d0"
    private const val STEP_COUNT_CHAR_UUID = "00030001-78fc-48fe-8e23-433b3a1942d0"

    private const val CCCD_UUID = "00002902-0000-1000-8000-00805f9b34fb"
    private const val RECONNECTION_DELAY = 5000L // 5 seconds

    private var bluetoothGatt: BluetoothGatt? = null
    private var scanner: android.bluetooth.le.BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null
    private var isConnecting = false
    private var shouldReconnect = true
    private val handler = Handler(Looper.getMainLooper())
    private var lastDeviceAddress: String? = null
    private var currentHeartRateCallback: ((Int) -> Unit)? = null
    private var currentStepCountCallback: ((Int) -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    fun startMonitoring(
        context: Context,
        onHeartRateReceived: (Int) -> Unit,
        onStepCountReceived: (Int) -> Unit
    ) {
        if (!checkPermissions(context)) {
            Log.e(TAG, "Missing required permissions")
            return
        }

        currentHeartRateCallback = onHeartRateReceived
        currentStepCountCallback = onStepCountReceived
        shouldReconnect = true

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        lastDeviceAddress?.let { address ->
            try {
                val device = bluetoothAdapter.getRemoteDevice(address)
                connectToDevice(context, device)
                return
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reconnect to last known device: ${e.message}")
            }
        }

        scanner = bluetoothAdapter.bluetoothLeScanner
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (result.device?.name?.contains("InfiniTime", ignoreCase = true) == true) {
                    Log.d(TAG, "Found PineTime device: ${result.device.name}")
                    stopScanning()
                    connectToDevice(context, result.device)
                    lastDeviceAddress = result.device.address
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "Scan failed with error code: $errorCode")
            }
        }

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner?.startScan(null, scanSettings, scanCallback)
            Log.d(TAG, "Started scanning for devices")
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
    private fun connectToDevice(context: Context, device: BluetoothDevice) {
        if (!checkPermissions(context) || isConnecting) return

        isConnecting = true

        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d(TAG, "Connected to GATT server")
                        isConnecting = false
                        try {
                            gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
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
                    setupHeartRateNotification(gatt)
                    setupStepCountNotification(gatt)
                } else {
                    Log.e(TAG, "Service discovery failed with status: $status")
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                when (characteristic.uuid.toString()) {
                    HEART_RATE_MEASUREMENT_CHAR_UUID -> handleHeartRateData(value)
                    STEP_COUNT_CHAR_UUID -> {
                        Log.d(TAG, "Step count notification received")
                        handleStepCountData(value)
                    }
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Characteristic read successful for ${characteristic.uuid}")
                    when (characteristic.uuid.toString()) {
                        STEP_COUNT_CHAR_UUID -> {
                            Log.d(TAG, "Step count read value: ${value.contentToString()}")
                            handleStepCountData(value)
                        }
                    }
                } else {
                    Log.e(TAG, "Characteristic read failed for ${characteristic.uuid} with status: $status")
                }
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt,
                descriptor: BluetoothGattDescriptor,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "CCCD write successful for ${descriptor.characteristic.uuid}")
                } else {
                    Log.e(TAG, "CCCD write failed with status: $status for ${descriptor.characteristic.uuid}")
                }
            }
        }

        try {
            bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception connecting to device: ${e.message}")
            isConnecting = false
            scheduleReconnect(context)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupHeartRateNotification(gatt: BluetoothGatt) {
        val service = gatt.getService(UUID.fromString(HEART_RATE_SERVICE_UUID))
        val characteristic = service?.getCharacteristic(UUID.fromString(HEART_RATE_MEASUREMENT_CHAR_UUID))

        if (characteristic == null) {
            Log.e(TAG, "Heart Rate characteristic not found!")
            return
        }

        enableNotification(gatt, characteristic)
    }

    @SuppressLint("MissingPermission")
    private fun setupStepCountNotification(gatt: BluetoothGatt) {
        val service = gatt.getService(UUID.fromString(MOTION_SERVICE_UUID))
        if (service == null) {
            Log.d(TAG, "Motion service not found")
            return
        }

        val characteristic = service.getCharacteristic(UUID.fromString(STEP_COUNT_CHAR_UUID))
        if (characteristic == null) {
            Log.d(TAG, "Step count characteristic not found")
            return
        }

        Log.d(TAG, "Found step count characteristic with properties: ${characteristic.properties}")

        // Enable notifications
        gatt.setCharacteristicNotification(characteristic, true)

        // Wait for heart rate descriptor write to complete before writing step count descriptor
        handler.postDelayed({
            val descriptor = characteristic.getDescriptor(UUID.fromString(CCCD_UUID))
            if (descriptor != null) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)

                // Read initial value after enabling notifications
                handler.postDelayed({
                    Log.d(TAG, "Attempting to read initial step count")
                    gatt.readCharacteristic(characteristic)
                }, 1000)
            }
        }, 1000) // Wait for heart rate setup to complete
    }

    @SuppressLint("MissingPermission")
    private fun enableNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        try {
            // Check if the characteristic supports notifications
            if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY == 0) {
                Log.e(TAG, "Characteristic ${characteristic.uuid} doesn't support notifications")
                return
            }

            gatt.setCharacteristicNotification(characteristic, true)

            val descriptor = characteristic.getDescriptor(
                UUID.fromString(CCCD_UUID)
            )
            if (descriptor != null) {
                Log.d(TAG, "Writing to CCCD for ${characteristic.uuid}")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            } else {
                Log.e(TAG, "CCCD not found for ${characteristic.uuid}!")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception setting notification: ${e.message}")
        }
    }

    private fun handleHeartRateData(value: ByteArray) {
        try {
            val flag = value[0].toInt()
            val heartRate = if (flag and 0x1 != 0) {
                (value[1].toInt() and 0xFF) + (value[2].toInt() shl 8)
            } else {
                value[1].toInt() and 0xFF
            }
            Log.d(TAG, "Heart rate received: $heartRate")
            currentHeartRateCallback?.invoke(heartRate)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing heart rate: ${e.message}")
        }
    }

    private fun handleStepCountData(value: ByteArray) {
        try {
            Log.d(TAG, "Parsing step count data: ${value.contentToString()}")

            val steps = when (value.size) {
                2 -> {
                    val result = (value[0].toInt() and 0xFF) or ((value[1].toInt() and 0xFF) shl 8)
                    Log.d(TAG, "Parsed 2-byte step count: $result")
                    result
                }
                4 -> {
                    val result = (value[0].toInt() and 0xFF) or
                            ((value[1].toInt() and 0xFF) shl 8) or
                            ((value[2].toInt() and 0xFF) shl 16) or
                            ((value[3].toInt() and 0xFF) shl 24)
                    Log.d(TAG, "Parsed 4-byte step count: $result")
                    result
                }
                else -> {
                    Log.e(TAG, "Unexpected step count data length: ${value.size}")
                    null
                }
            }

            steps?.let {
                Log.d(TAG, "Step count value to be sent: $it")
                currentStepCountCallback?.invoke(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing step count: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun scheduleReconnect(context: Context) {
        if (shouldReconnect) {
            handler.postDelayed({
                currentHeartRateCallback?.let { heartRateCallback ->
                    currentStepCountCallback?.let { stepCountCallback ->
                        startMonitoring(context, heartRateCallback, stepCountCallback)
                    }
                }
            }, RECONNECTION_DELAY)
        }
    }

    fun stopMonitoring() {
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
            currentHeartRateCallback = null
            currentStepCountCallback = null
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